/*
 * Copyright 2013 eBuddy BV
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

package org.elasterix.server.actors;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.elasterix.elasticactors.ActorRef;
import org.elasterix.elasticactors.UntypedActor;
import org.elasterix.server.messages.SipRegister;
import org.elasterix.sip.codec.SipHeader;
import org.elasterix.sip.codec.SipResponseStatus;
import org.springframework.util.StringUtils;

/**
 * User Actor
 * * 
 * @author Leonard Wolters
 */
public class User extends UntypedActor {
	private static final Logger log = Logger.getLogger(User.class);

	@Override
	public void onUndeliverable(ActorRef receiver, Object message) throws Exception {
		log.info(String.format("onUndeliverable. Message[%s]", message));
	}

	@Override
	public void onReceive(ActorRef sender, Object message) throws Exception {
		log.info(String.format("onReceive. Message[%s]", message));

		State state = getState(null).getAsObject(State.class);
		if(message instanceof SipRegister) {
			onRegister(sender, (SipRegister) message, state);
		} else {
			log.warn(String.format("onReceive. Unsupported message[%s]", 
					message.getClass().getSimpleName()));
			unhandled(message);
		}
	}

	protected void onRegister(ActorRef sender, SipRegister message, State state) {
		if(log.isDebugEnabled()) log.debug(String.format("onRegister. [%s]", message));

		// check if authentication is present...
		String authorization = message.getHeader(SipHeader.AUTHORIZATION);
		if(!StringUtils.hasLength(authorization)) {
			if(log.isDebugEnabled()) log.debug("onRegister. No authorization set");
			sendUnauthorized(sender, message, state);
			return;
		}
		
		Map<String,String> map = tokenize(authorization);
		
		// check username
		String val = map.get("username");
		if(!state.getUsername().equalsIgnoreCase(val)) {
			if(log.isDebugEnabled()) log.debug(String.format("onRegister. Provided username[%s] "
					+ "!= given username[%s]", val, state.getUsername()));
//			sender.tell(message.setSipResponseStatus(SipResponseStatus.BAD_REQUEST), getSelf());
			sendUnauthorized(sender, message, state);
			return;
		}

		// check nonce
		val = map.get("nonce");
		if(!Long.toString(state.getNonce()).equalsIgnoreCase(val)) {
			if(log.isDebugEnabled()) log.debug(String.format("onRegister. Provided nonce[%s] "
					+ "!= given nonce[%d]", val, state.getNonce()));
//			sender.tell(message.setSipResponseStatus(SipResponseStatus.BAD_REQUEST), getSelf());
			sendUnauthorized(sender, message, state);
			return;
		}
		
		// check hash
		val = map.get("response"); 
		if(!state.getSecretHash().equals(val)) {
			if(log.isDebugEnabled()) log.debug(String.format("onRegister. Provided hash[%s] "
					+ "!= given hash[%s]", val, state.getSecretHash()));
//			sender.tell(message.setSipResponseStatus(SipResponseStatus.BAD_REQUEST), getSelf());
			sendUnauthorized(sender, message, state);
			return;
		}
		
		// get uac 
		ActorRef userAgentClient = getUserAgentClient(message);
		if(userAgentClient == null) {
			sender.tell(message.setSipResponseStatus(SipResponseStatus.SERVER_INTERNAL_ERROR), 
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
				// update binding (with new expiration)
				state.addUserAgentClient(userAgentClient.getActorId(), expires);
			}
		}

		// send register message to device.
		userAgentClient.tell(message, sender);
	}
	
	private ActorRef getUserAgentClient(SipRegister message) {
		String uac = message.getUserAgentClient();
		if(!StringUtils.hasLength(uac)) {
			// should be impossible; SipServerHandler already checks for this
			// header and if not found, rejects the message
			log.warn("onRegister. No UAC header[CALL_ID] set in message");
			return null;
		}
		uac = String.format("uac/%s", uac);
		
		try {
			return getSystem().actorOf(uac, UserAgentClient.class,
					new UserAgentClient.State(uac));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}				
		return null;
	}
	
	private void sendUnauthorized(ActorRef sender, SipRegister message, State state) {
		// add extra header info and set nonce
		long nonce = (10000000 + ((long) (Math.random() * 90000000.0))); 
		if(log.isDebugEnabled()) log.debug(String.format("Generated nonce[%d, %,8d]", nonce, nonce));
		state.setNonce(nonce);
		message.addHeader(SipHeader.WWW_AUTHENTICATE, String.format("Digest algorithm=MD5, "
				+ "realm=\"elasterix\", nonce=\"%d\"", nonce));
		sender.tell(message.setSipResponseStatus(SipResponseStatus.UNAUTHORIZED), getSelf());
	}

	private Map<String, String> tokenize(String value) {
		// Authorization: Digest username="124",realm="elasterix",nonce="24855234",
		// uri="sip:sip.outerteams.com:5060",response="749c35e9fe30d6ba46cc801bdfe535a0",algorithm=MD5
		Map<String, String> map = new HashMap<String, String>();
		StringTokenizer st = new StringTokenizer(value, " ,", false);
		while(st.hasMoreTokens()) {
			String token = st.nextToken();
			int idx = token.indexOf("=");
			if(idx != -1) {
				map.put(token.substring(0, idx).toLowerCase(), 
						token.substring(idx+1).replace('\"', ' ').trim());
			} else {
				map.put(token.toLowerCase(), token);
			}
		}
		return map;
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
		private long nonce = -1;

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

		//
		//
		//

		public boolean removeUserAgentClient(String uac) {
			return userAgentClients.remove(uac) != null;
		}

		public void addUserAgentClient(String uac, long expiration) {
			removeUserAgentClient(uac);
			userAgentClients.put(uac, 
					System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expiration));
		}

		protected void setNonce(long nonce) {
			this.nonce = nonce;
		}
	}
}
