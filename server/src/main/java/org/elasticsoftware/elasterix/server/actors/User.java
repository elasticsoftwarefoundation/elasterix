/*
 * Copyright 2013 Leonard Wolters
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.elasticsoftware.elasterix.server.actors;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.elasticsoftware.elasterix.server.ServerConfig;
import org.elasticsoftware.elasterix.server.messages.ApiHttpMessage;
import org.elasticsoftware.elasterix.server.messages.SipRequestMessage;
import org.elasticsoftware.elasticactors.ActorRef;
import org.elasticsoftware.elasticactors.UntypedActor;
import org.elasticsoftware.sip.codec.SipHeader;
import org.elasticsoftware.sip.codec.SipMethod;
import org.elasticsoftware.sip.codec.SipResponseStatus;
import org.elasticsoftware.sip.codec.SipUser;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.springframework.util.StringUtils;

/**
 * User Actor
 * 
 * @author Leonard Wolters
 */
public final class User extends UntypedActor {
	private static final Logger log = Logger.getLogger(User.class);
	private static final boolean STRICT_UAC = true;
	/** To be used for generating alphanumeric nonce */
	private static final char[] CHARACTERS = new char[] {'a','b','c','d','e','f','g','h',
		'i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};

	@Override
	public void onUndeliverable(ActorRef receiver, Object message) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(String.format("onUndeliverable. Message[%s]", message));
		}
		
		ActorRef sipService = getSystem().serviceActorFor("sipService");
		if(message instanceof SipRequestMessage) {
			SipRequestMessage m = (SipRequestMessage) message;
			switch(m.getSipMethod()) {
			case INVITE:
				if(log.isDebugEnabled()) {
					log.debug(String.format("onUndeliverable. UAC[%s] does not exist", 
							receiver.getActorId()));
				}

				if(STRICT_UAC) {
					sipService.tell(m.toSipResponseMessage(SipResponseStatus.GONE.setOptionalMessage(
						String.format("UAC[%s] not found", receiver.getActorId()))), getSelf());
				} else {
					// create UAC and resent message
					StringTokenizer st = new StringTokenizer(receiver.getActorId(), "/_", false);
					if(st.countTokens() == 4) {
						st.nextToken(); // skip "uac"
						UserAgentClient.State state = new UserAgentClient.State(st.nextToken(), 
								st.nextToken(), Integer.parseInt(st.nextToken()));
						ActorRef ref = getSystem().actorOf(receiver.getActorId(), UserAgentClient.class, state);
						ref.tell(message, getSelf());
					}						
				}
			}
		}
	}

	@Override
	public void onReceive(ActorRef sender, Object message) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(String.format("onReceive. Message[%s]", message));
		}

		ActorRef sipService = getSystem().serviceActorFor("sipService");
		State state = getState(null).getAsObject(State.class);
		
		if(message instanceof SipRequestMessage) {
			SipRequestMessage request = (SipRequestMessage) message;
			
			// check if request authenticated
			if(request.isAuthenticated()) {
				// previously authenticated. Continue
			} else {
				if(authenticate(sender, request, state)) {
					request.setAuthenticated(true);
					if(request.getSipMethod() == SipMethod.REGISTER) {
						// OK, Update nonce so that next requests are automatically rejected. 
						state.setNonce(generateNonce());
					}
					sender.tell(request, getSelf());
				} else {					
					if(request.getSipMethod() == SipMethod.REGISTER) {
						state.setNonce(generateNonce());
						request.addHeader(SipHeader.WWW_AUTHENTICATE, String.format("Digest algorithm=MD5, "
								+ "realm=\"%s\", nonce=\"%s\"", ServerConfig.getRealm(), state.getNonce()));
					}
					sipService.tell(request.toSipResponseMessage(SipResponseStatus.UNAUTHORIZED), getSelf());
				}
				return;
			}

			switch (request.getSipMethod()) {
			case REGISTER:
				register(sipService, request, state);	
				return;
			case INVITE:
				invite(sipService, request, state);
				return;
			default:
				log.warn(String.format("onReceive. Unsupported message[%s]", 
						message.getClass().getSimpleName()));
				unhandled(message);
			}
		} else if (message instanceof ApiHttpMessage) {
			ApiHttpMessage apiMessage = (ApiHttpMessage) message;
			sender.tell(apiMessage.toHttpResponse(HttpResponseStatus.OK, state), getSelf());
		}
	}

	protected void register(ActorRef sipService, SipRequestMessage message, State state) {
		if(log.isDebugEnabled()) log.debug(String.format("register. [%s]", message));

		// get uac 
		SipUser user = message.getSipUser(SipHeader.CONTACT);
		
		// check expiration...
		long expires = message.getExpires();
		if(expires == 0) {
			// remove current binding with UAC set in message (if exist)
			state.removeUserAgentClient(user);
		} else {
			if(log.isDebugEnabled()) {
				log.debug(String.format("register. Registering UAC[%s:%d] for User[%s]",
						user.getDomain(), user.getPort(), user.getUsername()));
			}
			//message.appendHeader(SipHeader.CONTACT, "expires", Long.toString(expires));
	
			if(STRICT_UAC) {
				try {
					UserAgentClient.State uacState = new UserAgentClient.State(user.getUsername(), 
							user.getDomain(), user.getPort());
					getSystem().actorOf(state.key(user), UserAgentClient.class, uacState);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
			
			// update binding (with new expiration)
			state.addUserAgentClient(user, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expires));
			
			// schedule timeout for destruction...
			getSystem().getScheduler().scheduleOnce(getSelf(), 
					message, getSelf(), expires, TimeUnit.SECONDS);
		}

		// send OK back to client
		sipService.tell(message.toSipResponseMessage(SipResponseStatus.OK), getSelf());
	}
	
	protected void invite(ActorRef sipService, SipRequestMessage message, State state) {
		if(log.isDebugEnabled()) {
			log.debug(String.format("invite: notify UAC's of callee[%s]", 
				state.getUsername()));
		}
		
		// forward message to all registered UAC's of callee
		long now = System.currentTimeMillis();
		boolean ringing = false;
		for(Map.Entry<String,Long> uacEntry : state.getUserAgentClients().entrySet()) {
			// check is UAC is expired
			if(uacEntry.getValue() < now) {
				log.info(String.format("invite. User[%s] -> UAC[%s, %s] expired",
						state.getUsername(), uacEntry.getKey(), new Date(uacEntry.getValue())));
			} else {
				if(log.isDebugEnabled()) {
					log.debug(String.format("invite. User[%s], ringing UAC[%s]",
							state.getUsername(), uacEntry.getKey()));
				}
				// sent message to UAC
				ActorRef actor = getSystem().actorFor(uacEntry.getKey());
				actor.tell(message, getSelf());
				ringing = true;
			}
		}
		
		// did we rang a device (or at least notified a single UAC)?
		if(!ringing) {
			log.info(String.format("invite. No registered UAC for user[%s]", state.getUsername()));
			sipService.tell(message.toSipResponseMessage(SipResponseStatus.GONE.setOptionalMessage( 
					String.format("No registered UAC for user[%s]", state.getUsername()))), getSelf());				
		} else {
			// OK, the message is sent to at least one UAC. Wait for the response
			// to be sent back by this UAC. For now, return a 'trying' which is a
			// decent message
			sipService.tell(message.toSipResponseMessage(SipResponseStatus.TRYING), getSelf());				
		}
	}
	
	private final boolean authenticate(ActorRef dialog, SipRequestMessage request, State state) {
		if(log.isDebugEnabled()) {
			log.debug(String.format("onReceive. Authenticating %s[%s]", request.getMethod(), 
					state.getUsername()));
		}
		
		switch(request.getSipMethod()) {
		case REGISTER:
			// check if authentication is present...
			String authorization = request.getHeader(SipHeader.AUTHORIZATION);
			if(!StringUtils.hasLength(authorization)) {
				if(log.isDebugEnabled()) log.debug("authenticate. No authorization set");
				return false;
			}
			
			Map<String,String> map = request.tokenize(SipHeader.AUTHORIZATION);
			
			// check username
			String val = map.get("username");
			if(!state.getUsername().equalsIgnoreCase(val)) {
				if(log.isDebugEnabled()) log.debug(String.format("authenticate. Provided username[%s] "
						+ "!= given username[%s]", val, state.getUsername()));
				return false;
			}

			// check nonce
			val = map.get("nonce");
			if(!state.getNonce().equalsIgnoreCase(val)) {
				if(log.isDebugEnabled()) log.debug(String.format("authenticate. Provided nonce[%s] "
						+ "!= given nonce[%s]", val, state.getNonce()));
				return false;
			}
			
			// check hash
			val = map.get("response"); 
			if(!state.getSecretHash().equals(val)) {
				if(log.isDebugEnabled()) log.debug(String.format("authenticate. Provided hash[%s] "
						+ "!= given hash[%s]", val, state.getSecretHash()));
				return false;
			}
			return true;
		case INVITE:
			// check if we have a UAC registered for user
			Long expires = state.getUserAgentClient(request.getSipUser(SipHeader.CONTACT)); 
			if(expires == null || expires.longValue() < System.currentTimeMillis()) {
				return false;
			} else {
				return true;
			}
		}
		return false;
	}
	
	private String generateNonce() {
		// a value between 10M and 100M 
		long nonce = (10000000 + ((long) (Math.random() * 90000000.0)));
		// add 5 random characters at random places...
		StringBuffer sb = new StringBuffer(Long.toString(nonce));
		for(int i = 0; i < 5; i++) {
			char c = CHARACTERS[(int) ((double) (CHARACTERS.length - 1) * Math.random())];
			int idx = (int) (Math.random() * (double) (sb.length() - 1));
			sb.insert(idx, c);
		}
		return sb.toString();
	}

	/**
	 * State belonging to User
	 */
	public static final class State {
		private final String email;
		private final String username;
		private final String secretHash;
		/** UID of User Agent Client (key) and expires (seconds) as value */
		private Map<String, Long> userAgentClients = new HashMap<String, Long>();
		private String nonce;
		private String tag;

		@JsonCreator
		public State(@JsonProperty("email") String email,
				@JsonProperty("username") String username,
				@JsonProperty("secretHash") String secretHash) {
			this.email = email;
			this.username = username;
			this.secretHash = secretHash;
		}

		@JsonProperty("email")
		public String getEmail() {
			return email;
		}

		@JsonProperty("username")
		public String getUsername() {
			return username;
		}

		@JsonProperty("secretHash")
		public String getSecretHash() {
			return secretHash;
		}

		@JsonProperty("userAgentClients")
		public Map<String, Long> getUserAgentClients() {
			return userAgentClients;
		}

		@JsonProperty("nonce")
		public String getNonce() {
			return nonce;
		}
		
		@JsonProperty("tag")
		public String getTag() {
			return tag;
		}

		public boolean removeUserAgentClient(SipUser user) {
			return userAgentClients.remove(key(user)) != null;
		}

		/**
		 * ExpirationDate is the actual Date in ms when timeout occurs
		 * 
		 * @param uac
		 * @param expiration
		 */
		public void addUserAgentClient(SipUser user, long expirationDate) {
			removeUserAgentClient(user);
			userAgentClients.put(key(user), expirationDate);
		}
		
		public Long getUserAgentClient(SipUser user) {
			return userAgentClients.get(key(user));
		}

		protected void setNonce(String nonce) {
			this.nonce = nonce;
		}
		
		protected String key(SipUser user) {
			return String.format("uac/%s_%s_%d", user.getUsername(), user.getDomain(), 
					user.getPort());
		}
	}
}
