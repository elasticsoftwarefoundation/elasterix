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
import org.elasterix.elasticactors.ActorRef;
import org.elasterix.elasticactors.UntypedActor;
import org.elasterix.server.messages.SipRegister;
import org.elasterix.sip.codec.SipMessage;

/**
 * Device Actor
 * 
 * @author Leonard Wolters
 */
public class Device extends UntypedActor {
	private static final Logger log = Logger.getLogger(Device.class);

	@Override
	public void onReceive(ActorRef sender,Object message) throws Exception {
		log.info(String.format("onReceive. Message[%s]", message));

		if(message instanceof SipRegister) {
			doRegister(((SipRegister) message));
		} else {
			log.warn(String.format("onReceive. Unsupported message[%s]", 
					message.getClass().getSimpleName()));
		}
	}
	
	protected void doRegister(SipRegister message) {
		if(log.isDebugEnabled()) log.debug(String.format("doRegister. [%s]",
				message));
	}
}
