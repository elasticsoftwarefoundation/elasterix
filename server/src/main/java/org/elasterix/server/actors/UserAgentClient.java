/*
 * Copyright 2013 Joost van de Wijgerd
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

import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.elasterix.elasticactors.ActorRef;
import org.elasterix.elasticactors.UntypedActor;
import org.elasterix.server.messages.SipRegister;
import org.elasterix.sip.codec.SipHeader;
import org.elasterix.sip.codec.SipResponseStatus;

/**
 * User Agent Client (UAC)<br>
 * Considered a device
 * 
 * @author Leonard Wolters
 */
public class UserAgentClient extends UntypedActor {
	private static final Logger log = Logger.getLogger(UserAgentClient.class);

	@Override
	public void onReceive(ActorRef sender, Object message) throws Exception {
		log.info(String.format("onReceive. Message[%s]", message));

		State state = getState(null).getAsObject(State.class);
		if(message instanceof SipRegister) {
			doRegister(sender, (SipRegister) message, state);
		} else {
			log.warn(String.format("onReceive. Unsupported message[%s]", 
					message.getClass().getSimpleName()));
			unhandled(message);
		}
	}

	protected void doRegister(ActorRef sender, SipRegister message, State state) {
		if(log.isDebugEnabled()) log.debug(String.format("doRegister. [%s]",
				message));

		// check CSEQ
		// http://tools.ietf.org/html/rfc3261#section-8.1.1.5
		// http://tools.ietf.org/html/rfc3261#section-12.2.1.1
		
		// set expiration. (seconds)
		Long expires = message.getHeaderAsLong(SipHeader.EXPIRES);
		if(expires != null) {
			state.setExpires(expires);
		}

		// Register OK
		sender.tell(message.setSipResponseStatus(SipResponseStatus.OK), getSelf());
	}
	
	/**
	 * State belonging to User
	 */
	public static final class State {
		private final String uid;
		private long expires = 0;

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
		
		protected void setExpires(long expires) {
			this.expires = expires;
		}
	}
}
