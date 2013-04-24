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

import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.elasterix.elasticactors.ActorRef;
import org.elasterix.elasticactors.ActorState;
import org.elasterix.elasticactors.ActorStateFactory;
import org.elasterix.elasticactors.UntypedActor;
import org.elasterix.server.messages.SipRegister;

/**
 * User Actor
 * 
 * @author Leonard Wolters
 */
public class User extends UntypedActor {
	private static final Logger log = Logger.getLogger(User.class);

	@Override
	public void onReceive(ActorRef sender, Object message) throws Exception {
		log.info(String.format("onReceive. Message[%s]", message));

		State state = getState(null).getAsObject(State.class);
		if(message instanceof SipRegister) {
			doRegister((SipRegister) message, state);
		} else {
			log.warn(String.format("onReceive. Unsupported message[%s]", 
					message.getClass().getSimpleName()));
			unhandled(message);
		}
	}

	protected void doRegister(SipRegister message, State state) {
		if(log.isDebugEnabled()) log.debug(String.format("doRegister. [%s]",
				message));
	}

	@Override
	public void postCreate(ActorRef creator) throws Exception {
		State state = getState(null).getAsObject(State.class);
	}

	@Override
	public void postActivate(String previousVersion) throws Exception {
		State state = getState(null).getAsObject(State.class);
	}

	/**
	 * State belonging to User
	 */
	public static final class State {
		private final String email;
		private final String username;
		private final String secretHash;

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


	}
}
