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

package org.elasticsoftware.server.actors;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.elasticsoftware.elasticactors.ActorRef;
import org.elasticsoftware.elasticactors.UntypedActor;
import org.elasticsoftware.server.ServerConfig;
import org.elasticsoftware.server.messages.SipRequestMessage;
import org.elasticsoftware.sip.codec.SipHeader;
import org.elasticsoftware.sip.codec.SipResponseStatus;
import org.elasticsoftware.sip.codec.SipUser;
import org.springframework.util.StringUtils;

/**
 * User Actor
 * 
 * @author Leonard Wolters
 */
public final class User extends UntypedActor {
	private static final Logger log = Logger.getLogger(User.class);
//	/** To be used for generating alphanumeric nonce */
//	private static final char[] CHARACTERS = new char[] {'a','b','c','d','e','f','g','h',
//		'i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','1','2',
//		'3','4','5','6','7','8','9','0'};

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
				sipService.tell(m.toSipResponseMessage(SipResponseStatus.NOT_FOUND,
						String.format("User[%s] (To) not found", m.getUser(SipHeader.TO).getUsername())), 
						getSelf());
				sipService.tell(message, getSelf());
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

			// authenticated?
			if(!request.isAuthenticated()) {
				if(log.isDebugEnabled()) {
					log.debug(String.format("onReceive. Authenticating user[%s]", state.getUsername()));
				}
				if(authenticate(sipService, request, state)) {
					// Successfully authenticated. Inform sender (dialog) and continue
					request.setAuthenticated(true);
					sender.tell(request, getSelf());
				} else {
					// not authenticated. Response already sent. Voiding
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
		} 
	}

	protected void register(ActorRef sipService, SipRequestMessage message, State state) {
		if(log.isDebugEnabled()) log.debug(String.format("register. [%s]", message));

		// get uac 
		ActorRef userAgentClient = null;
		try {
			String uac = String.format("uac/%s", message.getUserAgentClient());
			SipUser user = message.getUser(SipHeader.CONTACT);
			userAgentClient = getSystem().actorOf(uac, UserAgentClient.class,
					new UserAgentClient.State(uac, user.getDomain(), user.getPort()));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			sipService.tell(message.toSipResponseMessage(SipResponseStatus.SERVER_INTERNAL_ERROR,
					String.format("User Agent Client[%s] not found", message.getUserAgentClient())), 
					getSelf());
			return;
		}		

		// check expiration...
		Long expires = message.getHeaderAsLong(SipHeader.EXPIRES);
		if(expires != null) {
			if(expires.longValue() == 0) {
				// remove current binding with UAC set in message
				state.removeUserAgentClient(userAgentClient.getActorId());
			} else {
				if(log.isDebugEnabled()) {
					log.debug(String.format("register. Registering UAC[%s] for User[%s]",
							userAgentClient.getActorId(), state.getUsername()));
				}
				message.appendHeader(SipHeader.CONTACT, "expires", Long.toString(expires));

				// update binding (with new expiration)
				state.addUserAgentClient(userAgentClient.getActorId(), 
						System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expires));
				
				// schedule timeout...
				getSystem().getScheduler().scheduleOnce(getSelf(), 
						message, getSelf(), expires, TimeUnit.SECONDS);
			}
		}

		// send register message to device.
		userAgentClient.tell(message, getSelf());
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
				state.removeUserAgentClient(uacEntry.getKey());
			} else {
				if(log.isDebugEnabled()) {
					log.debug(String.format("invite. User[%s], ringing UAC[%s]",
							state.getUsername(), uacEntry.getKey()));
				}
				ActorRef actor = getSystem().actorFor(uacEntry.getKey());
				actor.tell(message, getSelf());
				ringing = true;
			}
		}
		
		// did we rang a device (or at least notified a single UAC)?
		if(!ringing) {
			log.info(String.format("invite. No registered UAC for user[%s]", state.getUsername()));
			sipService.tell(message.toSipResponseMessage(SipResponseStatus.GONE, 
					String.format("No registered UAC for user[%s]", state.getUsername())), getSelf());				
		} else {
			// OK, the message is sent to at least one UAC. Wait for the response
			// to be sent back by this UAC. For now, return a 'trying' which is a
			// decent message
			sipService.tell(message.toSipResponseMessage(SipResponseStatus.TRYING, null), getSelf());				
		}
	}
	
	/**
	 * Authenticates this user (actor)
	 * 
	 * @param sender
	 * @param message
	 * @param state
	 * @return
	 */
	private final boolean authenticate(ActorRef sipService, SipRequestMessage message, State state) {

		// check if authentication is present...
		String authorization = message.getHeader(SipHeader.AUTHORIZATION);
		if(!StringUtils.hasLength(authorization)) {
			if(log.isDebugEnabled()) log.debug("authenticate. No authorization set");
			sendUnauthorized(sipService, message, state, "No authorization header found");
			return false;
		}
		
		Map<String,String> map = message.tokenize(SipHeader.AUTHORIZATION);
		
		// check username
		String val = map.get("username");
		if(!state.getUsername().equalsIgnoreCase(val)) {
			if(log.isDebugEnabled()) log.debug(String.format("authenticate. Provided username[%s] "
					+ "!= given username[%s]", val, state.getUsername()));
			sendUnauthorized(sipService, message, state, "Invalid username");
			return false;
		}

		// check nonce
		val = map.get("nonce");
		if(!Long.toString(state.getNonce()).equalsIgnoreCase(val)) {
			if(log.isDebugEnabled()) log.debug(String.format("authenticate. Provided nonce[%s] "
					+ "!= given nonce[%d]", val, state.getNonce()));
			sendUnauthorized(sipService, message, state, "Nonce does not match");
			return false;
		}
		
		// check hash
		val = map.get("response"); 
		if(!state.getSecretHash().equals(val)) {
			if(log.isDebugEnabled()) log.debug(String.format("authenticate. Provided hash[%s] "
					+ "!= given hash[%s]", val, state.getSecretHash()));
			sendUnauthorized(sipService, message, state, "Hash incorrect");
			return false;
		}
		
		return true;
	}
	
	/**
	 * Sends unauthorized response back to sender. Also accordingly set nonce and 
	 * WWW_AUTHENTICATE header
	 * 
	 * @param sender
	 * @param message
	 * @param state
	 * @param description
	 */
	private void sendUnauthorized(ActorRef sipService, SipRequestMessage message, State state, String description) {
		// Generate nonce and set WWW-AUTHENTICATE header
		long nonce = (10000000 + ((long) (Math.random() * 90000000.0))); 
		//long nonce = state.getNonce() + 1;
		if(log.isDebugEnabled()) log.debug(String.format("Generated nonce[%d, %,8d]", nonce, nonce));
		state.setNonce(nonce);
		message.addHeader(SipHeader.WWW_AUTHENTICATE, String.format("Digest algorithm=MD5, "
				+ "realm=\"%s\", nonce=\"%d\"", ServerConfig.getRealm(), nonce));
		
		// send message back
		sipService.tell(message.toSipResponseMessage(SipResponseStatus.UNAUTHORIZED, description), getSelf());
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
		private long nonce = 1;
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
		public long getNonce() {
			return nonce;
		}
		
		@JsonProperty("tag")
		public String getTag() {
			return tag;
		}

		//
		//
		//

		public boolean removeUserAgentClient(String uac) {
			return userAgentClients.remove(uac) != null;
		}

		public void addUserAgentClient(String uac, long expiration) {
			removeUserAgentClient(uac);
			userAgentClients.put(uac, expiration);
		}

		protected void setNonce(long nonce) {
			this.nonce = nonce;
		}
	}
}
