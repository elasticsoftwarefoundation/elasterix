package org.elasticsoftware.server.actors;

import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.elasticsoftware.elasticactors.ActorRef;
import org.elasticsoftware.elasticactors.UntypedActor;
import org.elasticsoftware.server.messages.SipMessage;
import org.elasticsoftware.server.messages.SipRegister;
import org.elasticsoftware.sip.codec.SipHeader;
import org.elasticsoftware.sip.codec.SipResponseStatus;
import org.springframework.util.StringUtils;

/**
 * A dialog is a temporary dialog between a user / uac and a server.<br>
 * <br>
 * This class is primarily used for keeping track of CSeq's
 * 
 * @author Leonard Wolters
 */
public class Dialog extends UntypedActor {
	private static final Logger log = Logger.getLogger(Dialog.class);

	@Override
	public void onReceive(ActorRef sipService, Object message) throws Exception {
		log.info(String.format("onReceive. Message[%s]", message));

		State state = getState(null).getAsObject(State.class);

		// get current (message) count 
		String messageType = null;
		int messageCount = -1;
		if(message instanceof SipRegister) {
			messageType = "REGISTER";
			messageCount = state.incrementAndGetRegister();
		} else {
			log.warn(String.format("onReceive. Unsupported message[%s]", 
					message.getClass().getSimpleName()));
			unhandled(message);
			return;
		}
		SipMessage sipMessage = (SipMessage) message;

		// update CSEQ
		// http://tools.ietf.org/html/rfc3261#section-8.1.1.5
		// http://tools.ietf.org/html/rfc3261#section-12.2.1.1
		//
		// CSeq must be updated for each request within a single dialog.
		// A dialog involves a user, a single UAC and the server
		
		String cSeq = sipMessage.getHeader(SipHeader.CSEQ);
		int cSeqCount = -1;
		String cSeqMethod = null;
		if(!StringUtils.hasLength(cSeq)) {
			// the specs indicate that cseq might be empty. If so, add a new 
			// entry!
			log.warn(String.format("onReceive. No CSEQ set for Dialog[%s,%s]. Creating new one",
					state.getUser(), state.getUserAgentClient()));
			state.reset();
			sipMessage.addHeader(SipHeader.CSEQ, String.format("%d %s", 
					state.incrementAndGetRegister(), messageType));
		} else {
			// CSeq: 1 REGISTER
			StringTokenizer st = new StringTokenizer(((SipMessage) message).getHeader(SipHeader.CSEQ), " ", false);
			while(st.countTokens() > 2) {
				try {
					cSeqCount = Integer.parseInt(st.nextToken());
				} catch (Exception e) {
					sipMessage.setSipResponseStatus(SipResponseStatus.BAD_REQUEST, 
							String.format("Invalid CSEQ[%s]", cSeq));
					sipService.tell(sipMessage, getSelf());
					return;
				}
				cSeqMethod = st.nextToken();
			}
			
			// check method
			if(!messageType.equalsIgnoreCase(cSeqMethod)) {
				log.warn(String.format("onReceive. CSEQ method[%s] doens't equals message type[%s]",
						cSeqMethod, messageType));
				sipMessage.setSipResponseStatus(SipResponseStatus.UNAUTHORIZED,
						String.format("CSEQ method[%s] doens't equals message type[%s]", cSeqMethod, messageType));
				sipService.tell(sipMessage, getSelf());
				return;
			}
			
			// check count 
			if(messageCount != cSeqCount) {
				log.warn(String.format("onReceive. CSEQ count[%d] doens't equals message count[%d]",
						cSeqCount, messageCount));
				// TODO: do we need to reset state?
				sipMessage.setSipResponseStatus(SipResponseStatus.UNAUTHORIZED, 
						String.format("CSEQ count[%d] doens't equals message count[%d]",
								cSeqCount, messageCount));
				sipService.tell(sipMessage, getSelf());
				return;
			}
		}

		// CSEQ OK, pass message to user
		ActorRef user = getSystem().actorFor("user/" + state.getUser());
		user.tell(message, getSelf());		
	}

	@Override
	public void onUndeliverable(ActorRef sender, Object message) throws Exception {
		log.info(String.format("onUndeliverable. Message[%s]", message));
		
		ActorRef sipService = getSystem().serviceActorFor("sipService");
		if(message instanceof SipRegister) {
			SipRegister m = (SipRegister) message;
			sipService.tell(m.setSipResponseStatus(SipResponseStatus.NOT_FOUND,
					String.format("User[%s] not found", m.getUser())), getSelf());
		} else {
			unhandled(message);
		}
	}

	/**
	 * State belonging to Dialog
	 */
	public static final class State {
		private int registerCount = 0;
		private int inviteCount = 0;
		private final String user;
		private final String userAgentClient;

		@JsonCreator
		public State(@JsonProperty("user") String user,
				@JsonProperty("userAgentClient") String userAgentClient) {
			this.user = user;
			this.userAgentClient = userAgentClient;
		}

		@JsonProperty("user")
		public String getUser() {
			return user;
		}

		@JsonProperty("userAgentClient")
		public String getUserAgentClient() {
			return userAgentClient;
		}

		@JsonProperty("registerCount")
		public int getRegisterCount() {
			return registerCount;
		}

		protected int incrementAndGetRegister() {
			return ++registerCount;
		}
		
		@JsonProperty("inviteCount")
		public int getInviteCount() {
			return inviteCount;
		}

		protected int incrementAndGetInvite() {
			return ++inviteCount;
		}

		protected void reset() {
			registerCount = 0;
			inviteCount = 0;
		}
	}
}
