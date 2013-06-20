package org.elasticsoftware.elasterix.server.actors;

import org.apache.log4j.Logger;
import org.elasticsoftware.elasticactors.ActorRef;
import org.elasticsoftware.elasticactors.UntypedActor;

/**
 * @author Leonard Wolters
 */
public class DummyActor extends UntypedActor {
	private static final Logger log = Logger.getLogger(DummyActor.class);
	
	public static Object onUndeliverable;
	public static Object onReceive;

	@Override
	public void onUndeliverable(ActorRef receiver, Object message) throws Exception {
		log.warn("onUndeliverable. " + message);
		onUndeliverable = message;
	}
	
	@Override
	public void onReceive(ActorRef sender, Object message) throws Exception {
		log.warn("onReceive. " + message);
		onReceive = message;
	}
}
