package org.elasticsoftware.elasterix.server.web;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsoftware.elasterix.server.actors.User;
import org.elasticsoftware.elasterix.server.messages.ApiHttpMessage;
import org.elasticsoftware.elasticactors.ActorRef;
import org.elasticsoftware.elasticactors.ActorSystem;
import org.elasticsoftware.elasticactors.TypedActor;
import org.elasticsoftware.elasticactors.http.messages.HttpRequest;
import org.elasticsoftware.elasticactors.http.messages.HttpResponse;
import org.elasticsoftware.elasticactors.http.messages.RegisterRouteMessage;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import com.biasedbit.efflux.logging.Logger;

/**
 * User Controller<br>
 * 
 * Overview of registered methods (urls):<br>
 * <ul>
 * 	<li>.../api/2.0/users/lwolters/create</li>
 * 	<li>.../api/2.0/users/lwolters/get</li>
 * 	<li>.../api/2.0/users/lwolters/remove</li>
 * 	<li>.../api/2.0/users/lwolters/update</li>
 * </ul>
 * 
 * @author Leonard Wolters
 */
public class UserController extends TypedActor<HttpRequest> {
	private static final Logger log = Logger.getLogger(UserController.class);
	//private ApiMethodMatcher apiMatcher = new ApiMethodMatcher();
	
	@Override
	public void postActivate(String previousVersion) throws Exception {
		log.info("Registering user controller");
		
		// register ourselves with the http server
		ActorSystem httpSystem = getSystem().getParent().get("Http");
        if(httpSystem != null) {
            ActorRef httpServer = httpSystem.serviceActorFor("httpServer");
    		httpServer.tell(new RegisterRouteMessage("/api/*/users/**", getSelf()), getSelf());
        } else {
            log.warn("Http ActorSystem not available");
        }
		
		// register api method for this controller
		//apiMatcher.registerMethod("update", method)
	}

	@Override
	public void onReceive(ActorRef httpService, HttpRequest message) throws Exception {
		if(log.isDebugEnabled()) {
			log.info("onReceive");
		}
		
		ApiHttpMessage apiMessage = ApiHttpMessage.parse(message);
		if(apiMessage == null) {
			log.warn(String.format("Invalid url path[%s]", message.getUrl()));
			sendHttpResponse(httpService, HttpResponseStatus.BAD_REQUEST, null);
			return;
		}

		// double check domain
		if(!("users".equalsIgnoreCase(apiMessage.getDomain()))) {
			log.warn(String.format("Not registered for this controller[%s != users]", 
					apiMessage.getDomain()));
			sendHttpResponse(httpService, HttpResponseStatus.BAD_REQUEST, null);
			return;
		}
		
		// If action is create ... 
		if("create".equalsIgnoreCase(apiMessage.getAction())) {
			if("get".equalsIgnoreCase(message.getMethod())) {
				log.warn(String.format("GET method not accepted for create user"));
				sendHttpResponse(httpService, HttpResponseStatus.NOT_ACCEPTABLE, null);
				return;
			}
				
			// check if content is present
			User.State state = apiMessage.getContent(User.State.class);
			if(state == null) {
				sendHttpResponse(httpService, HttpResponseStatus.NO_CONTENT, null);
			} else {
				// check if state is 'complete'
				ActorRef actorRef = getSystem().actorOf(apiMessage.getActorId(), User.class, state);
				actorRef.tell(apiMessage, httpService);
			}
			return;
		} 
		
		// dispatch message to user
		ActorRef user = getSystem().actorFor(String.format("users/%s", apiMessage.getActorId()));
		user.tell(apiMessage, getSelf());
	}
	
	private void sendHttpResponse(ActorRef sender, HttpResponseStatus status, byte[] response) {
		
		// headers
		Map<String,List<String>> headers = new HashMap<String,List<String>>();
        headers.put(HttpHeaders.Names.CONTENT_TYPE, Arrays.asList("text/plain"));
        // response
        if(response == null) {
        	response = status.getReasonPhrase().getBytes(Charset.forName("UTF-8"));
        }
		sender.tell(new HttpResponse(status.getCode(), headers, response), getSelf());
	}
	
	@Override
	public void onUndeliverable(ActorRef receiver, Object message) throws Exception {
		log.info(String.format("onUndeliverable. User[%s] not found", receiver.getActorId()));
		//receiver.tell(String.format("User[%s] does not exist", receiver.getActorId()), getSelf());
	}
}
