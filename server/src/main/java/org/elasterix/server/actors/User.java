package org.elasterix.server.actors;

import org.apache.log4j.Logger;
import org.elasterix.elasticactors.ActorRef;
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
	public void onReceive(Object message, ActorRef sender) throws Exception {
		log.info(String.format("onReceive. Message[%s]", message));

		if(message instanceof SipRegister) {
			doRegister((SipRegister) message);
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
