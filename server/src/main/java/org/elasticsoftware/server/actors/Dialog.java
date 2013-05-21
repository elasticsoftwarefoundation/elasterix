package org.elasticsoftware.server.actors;

import java.util.StringTokenizer;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.elasticsoftware.elasticactors.ActorRef;
import org.elasticsoftware.elasticactors.UntypedActor;
import org.elasticsoftware.server.messages.SipRequestMessage;
import org.elasticsoftware.server.messages.SipResponseMessage;
import org.elasticsoftware.sip.codec.SipHeader;
import org.elasticsoftware.sip.codec.SipResponseStatus;
import org.elasticsoftware.sip.codec.SipUser;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;
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
	private PasswordEncoder passwordEncoder = new Md5PasswordEncoder();

	@Override
	public void onReceive(ActorRef sender, Object message) throws Exception {
		if(log.isDebugEnabled()) log.debug(String.format("onReceive. Message[%s]", message));

		// check message type
		State state = getState(null).getAsObject(State.class);
		if(message instanceof SipRequestMessage) {
			SipRequestMessage sipMessage = (SipRequestMessage) message;

			// check if authenticated
			if(checkAuthentication(sipMessage, state)) {
				return;
			}
			
			// CSeq must be updated for each request within a single dialog.
			// A dialog involves a user, a single UAC and the server
			ActorRef sipService = getSystem().serviceActorFor("sipService");
			if(checkCSeq(sipService, sipMessage, state)) {
				return;
			}
			
			// CSEQ OK, pass message to user
			switch(sipMessage.getSipMethod()) {
			case REGISTER:
				ActorRef user = getSystem().actorFor("user/" + state.getUsername());
				user.tell(message, getSelf());		
				return;
			case INVITE:
				SipUser toUser = sipMessage.getUser(SipHeader.TO);
				user = getSystem().actorFor("user/" + toUser.getUsername());
				user.tell(message, getSelf());	
				return;
			case BYE:
			case ACK:
			case CANCEL:
			case OPTIONS:
			default:
			}
		} else if (message instanceof SipResponseMessage) {
			
		} else {
			log.warn(String.format("onReceive. Unsupported message[%s]", 
					message.getClass().getSimpleName()));
			unhandled(message);
			return;
		}
	}

	@Override
	public void onUndeliverable(ActorRef sender, Object message) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(String.format("onUndeliverable. Message[%s]", message));
		}
		
		ActorRef sipService = getSystem().serviceActorFor("sipService");
		State state = getState(null).getAsObject(State.class);
		if(message instanceof SipRequestMessage) {
			SipRequestMessage m = (SipRequestMessage) message;
			switch(m.getSipMethod()) {
			case REGISTER:
				sipService.tell(m.toSipResponseMessage(SipResponseStatus.NOT_FOUND.setOptionalMessage(
						String.format("User[%s] (From) not found", state.getUsername()))), getSelf());
				break;
			case INVITE:
				sipService.tell(m.toSipResponseMessage(SipResponseStatus.NOT_FOUND.setOptionalMessage(
						String.format("User[%s] (TO) not found", state.getUsername()))), getSelf());
				break;
			}
		} else {
			unhandled(message);
		}
	}
	
	/**
	 * Checks authentication of user / dialog.<br>
	 * First a check is done if message (user/dialog) was previously authenticated by
	 * using a secret key match (based on a SIPHeader). In case of no match, the FROM
	 * user is told to authenticate him/her self. When succesfull, the original message 
	 * is sent back but this time with the property authenticated set to true. Now this
	 * dialog will set the secret key so that from now on new message belonging to this
	 * user / dialog are automatically authenticated.
	 * 
	 * @param sipMessage
	 * @param state
	 * @return
	 */
	private boolean checkAuthentication(SipRequestMessage sipMessage, State state) {
		
		// check secret key
		String value = sipMessage.getHeader(SipHeader.CONTACT);
		if(StringUtils.hasLength(value)) {
			if(passwordEncoder.encodePassword(value, null).equals(state.getSecretKey())) {
				if(log.isDebugEnabled()) {
					log.debug(String.format("checkAuthentication. User[%s] previously authenticated",
							state.getUsername()));
				}
				sipMessage.setAuthenticated(true);
				sipMessage.appendHeader(SipHeader.TO, "tag", state.getUserTag());
				return false;
			}
		}
		
		// reset CSEQ counters
		state.reset(); 
		
		if(sipMessage.isAuthenticated()) {
			if(log.isDebugEnabled()) {
				log.debug(String.format("checkAuthentication. User[%s] just authenticated",
						state.getUsername()));
			}
			state.setSecretKey(passwordEncoder.encodePassword(
					sipMessage.getHeader(SipHeader.CONTACT), null));
			// append TO user with UID tag
			String tag = UUID.randomUUID().toString();
			state.setUserTag(tag);
			sipMessage.appendHeader(SipHeader.TO, "tag", state.getUserTag());
			return false;
		} else {
			if(log.isDebugEnabled()) {
				log.debug(String.format("checkAuthentication. Telling User[%s] to authenticate",
						state.getUsername()));
			}
			ActorRef user = getSystem().actorFor("user/" + state.getUsername());
			user.tell(sipMessage, getSelf());	
			return true;
		}
	}
	
	/**
	 * Check and update CSeq
     *
     * http://tools.ietf.org/html/rfc3261#section-8.1.1.5
     * http://tools.ietf.org/html/rfc3261#section-12.2.1.1
	 * 
	 * @param sipService
	 * @param sipMessage
	 * @param state
	 * @return
	 */
	private boolean checkCSeq(ActorRef sipService, SipRequestMessage sipRequest, State state) {
		// CSeq must be updated for each request within a single dialog.
		// A dialog involves a user, a single UAC and the server
		
		// get current (message) count 
		String messageType = null;
		int messageCount = -1;
		switch(sipRequest.getSipMethod()) {
		case INVITE:
			messageType = "INVITE";
			messageCount = state.incrementAndGetInvite();
			break;
		case REGISTER:
			messageType = "REGISTER";
			messageCount = state.incrementAndGetRegister();
			break;
//		case OPTIONS:
//			  CSEQ is always 102 OPTIONS
		default:
			log.info(String.format("checkCSeq. Unsupported method[%s]", 
					sipRequest.getSipMethod().name()));
			return false;
		}
		
		String cSeq = sipRequest.getHeader(SipHeader.CSEQ);
		int cSeqCount = -1;
		String cSeqMethod = null;
		if(!StringUtils.hasLength(cSeq)) {
			// the specs indicate that cseq might be empty. If so, add a new entry
			log.warn(String.format("checkCSeq. No CSEQ set for Dialog[%s,%s]. Creating new one",
					state.getUsername(), state.getUserAgentClient()));
			state.reset();
			sipRequest.addHeader(SipHeader.CSEQ, String.format("1 %s", messageType));
		} else {
			// CSeq: 1 REGISTER || 304 INVITE .....
			StringTokenizer st = new StringTokenizer(cSeq, " ", false);
			while(st.countTokens() >= 2) {
				try {
					cSeqCount = Integer.parseInt(st.nextToken());
				} catch (Exception e) {
					sipService.tell(sipRequest.toSipResponseMessage(SipResponseStatus.BAD_REQUEST.setOptionalMessage( 
							String.format("Invalid CSEQ[%s]", cSeq))), getSelf());
					return true;
				}
				cSeqMethod = st.nextToken();
			}
			
			// check method
			if(!messageType.equalsIgnoreCase(cSeqMethod)) {
				log.warn(String.format("checkCSeq. CSEQ method[%s] doens't equals message type[%s]",
						cSeqMethod, messageType));
				sipService.tell(sipRequest.toSipResponseMessage(SipResponseStatus.UNAUTHORIZED.setOptionalMessage(
						String.format("CSEQ method[%s] doens't equals message type[%s]", cSeqMethod, messageType))), 
						getSelf());
				return true;
			}
			
			// check count 
			if(messageCount != cSeqCount) {
				log.warn(String.format("checkCSeq. CSEQ count[%d] doens't equals message count[%d]",
						cSeqCount, messageCount));
				// TODO: do we need to reset state?
				sipService.tell(sipRequest.toSipResponseMessage(SipResponseStatus.UNAUTHORIZED.setOptionalMessage( 
						String.format("CSEQ count[%d] doens't equals message count[%d]", 
								cSeqCount, messageCount))), getSelf());
				return true;
			}
		}
		return false;
	}
	
	/**
	 * State belonging to Dialog
	 */
	public static final class State {
		private int registerCount = 0;
		private int inviteCount = 0;
		private final String username;
		private final String userAgentClient;
		private String userTag;
		private String secretKey;

		@JsonCreator
		public State(@JsonProperty("username") String username,
				@JsonProperty("userAgentClient") String userAgentClient) {
			this.username = username;
			this.userAgentClient = userAgentClient;
		}

		@JsonProperty("username")
		public String getUsername() {
			return username;
		}

		@JsonProperty("userAgentClient")
		public String getUserAgentClient() {
			return userAgentClient;
		}

		@JsonProperty("registerCount")
		public int getRegisterCount() {
			return registerCount;
		}
		
		@JsonProperty("userTag")
		public String getUserTag() {
			return userTag;
		}

		protected void setUserTag(String userTag) {
			this.userTag = userTag;
		}
		
		@JsonProperty("secretKey")
		public String getSecretKey() {
			return secretKey;
		}

		protected void setSecretKey(String secretKey) {
			this.secretKey = secretKey;
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
			registerCount = 1;
			inviteCount = 1;
		}
	}
}
