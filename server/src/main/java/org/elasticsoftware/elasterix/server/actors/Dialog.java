package org.elasticsoftware.elasterix.server.actors;

import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.elasticsoftware.elasterix.server.messages.SipRequestMessage;
import org.elasticsoftware.elasterix.server.messages.SipResponseMessage;
import org.elasticsoftware.elasterix.server.messages.TimeoutMessage;
import org.elasticsoftware.elasticactors.ActorRef;
import org.elasticsoftware.elasticactors.UntypedActor;
import org.elasticsoftware.sip.codec.SipHeader;
import org.elasticsoftware.sip.codec.SipMethod;
import org.elasticsoftware.sip.codec.SipResponseStatus;
import org.elasticsoftware.sip.codec.SipUser;
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
	
	public static int TIMEOUT_DESTRUCTION_REGISTER = 120;
	public static int TIMEOUT_DESTRUCTION_OTHER = 3;
	
	@Override
    public void postActivate(String previousVersion) throws Exception {
    }
	
	@Override
	public void onReceive(ActorRef sender, Object message) throws Exception {
	
		// check if we need to 'destruct' this dialog
		State state = getState(null).getAsObject(State.class);
		if(checkForDestruction(state, message)) {
			// dialog is removed / destructed
			return;
		}
		
		if(message instanceof SipRequestMessage) {
			SipRequestMessage sipMessage = (SipRequestMessage) message;
			
			ActorRef sipService = getSystem().serviceActorFor("sipService");
			switch(sipMessage.getSipMethod()) {
			case REGISTER:
				if(checkAuthentication(sipMessage, state)) return;
				if(checkCSeq(sipService, sipMessage, state)) return;
				getSystem().actorFor("user/" + state.getUsername()).tell(message, getSelf());
				break;
			case INVITE:
				if(checkAuthentication(sipMessage, state)) return;
				if(checkCSeq(sipService, sipMessage, state)) return;
				SipUser toUser = sipMessage.getSipUser(SipHeader.TO);
				getSystem().actorFor("user/" + toUser.getUsername()).tell(message, getSelf());
				break;
			case SUBSCRIBE:
				sipService.tell(sipMessage.toSipResponseMessage(SipResponseStatus.NOT_FOUND
						.setOptionalMessage("no mailbox")), getSelf());
				break;
			default:
				sipService.tell(sipMessage.toSipResponseMessage(SipResponseStatus.NOT_IMPLEMENTED), 
						getSelf());
				// remove actor immediately
				getSystem().stop(getSelf());
			}
		} else if (message instanceof SipResponseMessage) {
			// unsupported
		} else {
			unhandled(message);
		}
	}
	
	private boolean checkForDestruction(Dialog.State state, Object message) 
	throws Exception {
		
		SipMethod method = null;
		if(message instanceof TimeoutMessage) {
			TimeoutMessage m = (TimeoutMessage) message;
			if(m.isExpired(state.lastUpdate)) {
				if(log.isDebugEnabled()) {
					log.debug(String.format("Destructing [%s, %s]", state.method, 
							state.getCallId()));
				}
				getSystem().stop(getSelf());
				return true;
			} else {
				// reschedule message
				method = SipMethod.lookup(m.getMethod());
			}
		} else if(message instanceof SipRequestMessage) {
			if(state.lastUpdate == -1) {
				if(log.isDebugEnabled()) {
					log.debug(String.format("Start scheduling destructing [%s, %s]", 
							state.method, state.getCallId()));
				}
				method = ((SipRequestMessage) message).getSipMethod();
			}
		}

		// update lastUpdate and (re)schedule destruction
		state.lastUpdate = System.currentTimeMillis();
		if(method != null) {
			switch(method) {
			case REGISTER:
				scheduleForDestruction(state, TIMEOUT_DESTRUCTION_REGISTER);
				break;
			default:
				// most dialogs do not need to be persistent. Kill them
				// immediately
				scheduleForDestruction(state, TIMEOUT_DESTRUCTION_OTHER);
			}
		}
		
		return false;
	}
    private void scheduleForDestruction(Dialog.State state, long seconds) {
    	if(log.isDebugEnabled()) {
    		log.debug(String.format("scheduleForDestruction. [%s, %s] in %d seconds", 
    				state.method, state.callId, seconds));
    	}
		TimeoutMessage message = new TimeoutMessage();
		message.setMethod(state.getMethod());
		message.setTimeoutInMilliSeconds(TimeUnit.SECONDS.toMillis(seconds));
		getSystem().getScheduler().scheduleOnce(getSelf(), message, getSelf(), 
				seconds, TimeUnit.SECONDS);
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
		} else if(message instanceof TimeoutMessage) {
			// Dialog already removed. Fall through...
		} else {
			unhandled(message);
		}
	}
	
	/**
	 * Checks authentication of user / dialog.<br> The first time when user has not authenticated
	 * himself, this message is forwarded to the FROM user and told to authenticate him/her self. 
	 * When successful, the original message is sent back to this dialog, but this time with the 
	 * property authenticated set to true.
	 * 
	 * @param sipMessage
	 * @param state
	 * @return
	 */
	private boolean checkAuthentication(SipRequestMessage sipMessage, State state) {
		if(sipMessage.isAuthenticated()) {
			if(log.isDebugEnabled()) {
				log.debug(String.format("checkAuthentication. User[%s] just authenticated",
						state.getUsername()));
			}

			// append TO user with UID tag
			String tag = UUID.randomUUID().toString();
			sipMessage.appendHeader(SipHeader.TO, "tag", tag);
			return false;
		} else {
			if(log.isDebugEnabled()) {
				log.debug(String.format("checkAuthentication. Telling User[%s] to authenticate",
						state.getUsername()));
			}
			
			// increase count here, since the authentication of user might fail, and a response
			// is directly sent back to user
			state.incrementCount();
		
			ActorRef user = getSystem().actorFor("user/" + state.getUsername());
			user.tell(sipMessage, getSelf());	
			return true;
		}
	}
	
    /**
     * Check and update CSeq
     * <p/>
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

        String cSeq = sipRequest.getHeader(SipHeader.CSEQ);
        if (!StringUtils.hasLength(cSeq)) {
            // the specs indicate that cseq might be empty. If so, add a new entry
            log.warn(String.format("checkCSeq. No CSEQ set for Dialog[%s,%s]. Creating new one",
                    state.getCallId(), state.getUsername()));
            sipRequest.addHeader(SipHeader.CSEQ, String.format("%d %s", state.getCount(), state.getMethod()));
            return false;
        } else {
            int cSeqCount = -1;
            String cSeqMethod = null;

            // CSeq: 1 REGISTER || 304 INVITE .....
            StringTokenizer st = new StringTokenizer(cSeq, " ", false);
            while (st.countTokens() >= 2) {
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
            if (!state.getMethod().equalsIgnoreCase(cSeqMethod)) {
                log.warn(String.format("checkCSeq. CSEQ message method[%s] doens't equal state method[%s]",
                        cSeqMethod, state.getMethod()));
                sipService.tell(sipRequest.toSipResponseMessage(SipResponseStatus.UNAUTHORIZED.setOptionalMessage(
                        String.format("CSEQ method[%s] doens't equals message type[%s]", cSeqMethod,
                                state.getMethod()))), getSelf());
                return true;
            }

            // check count
            if (state.getCount() != cSeqCount) {
                log.warn(String.format("checkCSeq. CSEQ message count[%d] doens't equal state count[%d]",
                        cSeqCount, state.getCount()));
                sipService.tell(sipRequest.toSipResponseMessage(SipResponseStatus.UNAUTHORIZED.setOptionalMessage(
                        String.format("CSEQ count[%d] doens't equals message count[%d]",
                                cSeqCount, state.getCount()))), getSelf());
                return true;
            }
        }
        return false;
    }

    /**
     * State belonging to Dialog
     */
    public static final class State {
        private int count = 0;
        private final String username;
        private final String callId;
        private final String method;
        private long lastUpdate = -1L;

        @JsonCreator
        public State(@JsonProperty("username") String username,
                     @JsonProperty("callId") String callId,
                     @JsonProperty("method") String method) {
            this.username = username;
            this.callId = callId;
            this.method = method;
        }

        @JsonProperty("username")
        public String getUsername() {
            return username;
        }

        @JsonProperty("callId")
        public String getCallId() {
            return callId;
        }

        @JsonProperty("method")
        public String getMethod() {
            return method;
        }

        @JsonProperty("count")
        public long getCount() {
            return count;
        }
        
        @JsonProperty("lastUpdate")
        public long getLastUpdate() {
            return lastUpdate;
        }

        protected void incrementCount() {
            count++;
        }
    }
}
