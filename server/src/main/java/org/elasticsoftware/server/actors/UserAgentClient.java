/*
 * Copyright 2013 Leonard Wolters, Joost van de Wijgerd
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
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.elasticsoftware.elasticactors.ActorRef;
import org.elasticsoftware.elasticactors.UntypedActor;
import org.elasticsoftware.server.messages.SipRegister;
import org.elasticsoftware.sip.codec.SipHeader;
import org.elasticsoftware.sip.codec.SipResponseStatus;
import org.springframework.util.StringUtils;

/**
 * User Agent Client (UAC)<br>
 * Considered a device
 * 
 * @author Leonard Wolters
 */
public final class UserAgentClient extends UntypedActor {
	private static final Logger log = Logger.getLogger(UserAgentClient.class);

	@Override
	public void onReceive(ActorRef sipService, Object message) throws Exception {
		log.info(String.format("onReceive. Message[%s]", message));

		State state = getState(null).getAsObject(State.class);
		if(message instanceof SipRegister) {
			doRegister(sipService, (SipRegister) message, state);
		} else {
			log.warn(String.format("onReceive. Unsupported message[%s]", 
					message.getClass().getSimpleName()));
			unhandled(message);
		}
	}

	protected void doRegister(ActorRef sender, SipRegister message, State state) {
		if(log.isDebugEnabled()) log.debug(String.format("doRegister. [%s]",
				message));
		
		ActorRef sipService = getSystem().serviceActorFor("sipService");

		// set expiration. (seconds)
		Long expires = message.getHeaderAsLong(SipHeader.EXPIRES);
		if(expires != null) {
			state.setExpires(expires);
		}
		
		// check for contact header
		String contact = message.getHeader(SipHeader.CONTACT);
		if(!StringUtils.hasLength(contact)) {
			log.warn(String.format("doRegister. No contact header found"));
			sipService.tell(message.setSipResponseStatus(SipResponseStatus.BAD_REQUEST,
					"No CONTACT header found"), getSelf());
		}
		
		message.addHeader(SipHeader.DATE, new Date(state.getExpiration()).toString());
		sipService.tell(message.setSipResponseStatus(SipResponseStatus.OK, null), getSelf());
	}
	
	/**
	 * State belonging to User Agent Client
	 */
	public static final class State {
		private final String uid;
		private long expires = 0;
		private long expiration = 0;

		@JsonCreator
		public State(@JsonProperty("uid") String uid) {
			this.uid = uid;
		}

		@JsonProperty("uid")
		public String getUid() {
			return uid;
		}

		@JsonProperty("expires")
		public long getExpires() {
			return expires;
		}
		
		@JsonProperty("expiration")
		public long getExpiration() {
			return expiration;
		}
		
		protected void setExpires(long expires) {
			this.expires = expires;
			this.expiration = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expires);
		}
	}
}
