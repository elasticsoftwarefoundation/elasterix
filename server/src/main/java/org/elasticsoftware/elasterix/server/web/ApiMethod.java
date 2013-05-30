package org.elasticsoftware.elasterix.server.web;

import org.elasticsoftware.elasticactors.ActorRef;
import org.elasticsoftware.elasticactors.http.messages.HttpMessage;

/**
 * API Method 
 * 
 * @author Leonard Wolters
 */
public interface ApiMethod {
	
	/**
	 * @param message
	 */
	void handleMethod(ActorRef controller, ActorRef actor, HttpMessage message);
}
