package org.elasticsoftware.elasterix.server.web;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.elasticsoftware.elasterix.server.ApiConfig;
import org.elasticsoftware.elasterix.server.actors.User;
import org.elasticsoftware.elasterix.server.messages.ApiHttpMessage;
import org.elasticsoftware.elasticactors.ActorRef;
import org.elasticsoftware.elasticactors.ActorSystem;
import org.elasticsoftware.elasticactors.TypedActor;
import org.elasticsoftware.elasticactors.http.messages.HttpRequest;
import org.elasticsoftware.elasticactors.http.messages.HttpResponse;
import org.elasticsoftware.elasticactors.http.messages.RegisterRouteMessage;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.springframework.util.StringUtils;


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
	private static final boolean POST_CREATE_ACTOR = true;
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
		if(log.isDebugEnabled()) {
			log.debug(String.format("%s %s", apiMessage.getActorId(), apiMessage.getMethod()));
		}
		
		// create / update user ?
		ActorRef user = null;
		if(HttpMethod.POST == apiMessage.getMethod()) {
			user = createActor(httpService, apiMessage);
			if(user == null) {
				// user not created, whilst it should. Response have been sent
				return;
			}
		} else {
			user = getSystem().actorFor(String.format("user/%s", apiMessage.getActorId()));
		}
		
		if(HttpMethod.DELETE == apiMessage.getMethod()) {
			if(ApiConfig.checkForExistenceBeforeDelete()) {
				user.tell(apiMessage, httpService);
			} else {
				// remove user directly (without checking if it exist)
				getSystem().stop(user);
				sendHttpResponse(httpService, HttpResponseStatus.OK, null);
			}
		} else {
			// ok, dispatch message to user, but use httpService as sender, since the onUndelivered 
			// doesn't have a handle to the temporarily httpResponseActor
			user.tell(apiMessage, httpService);
		}
	}
	
	private ActorRef createActor(ActorRef httpService, ApiHttpMessage message) {		
		
		if(!message.hasContent()) {
			sendHttpResponse(httpService, HttpResponseStatus.NO_CONTENT, null);
			return null;
		}
		if(message.getMethod() != HttpMethod.POST) {
			sendHttpResponse(httpService, HttpResponseStatus.NOT_ACCEPTABLE, 
				"Post method required");
			return null;
		}
		if(!message.hasJsonContentType()) {
			sendHttpResponse(httpService, HttpResponseStatus.NOT_ACCEPTABLE, 
				"No JSon content type");
			return null;
		}
		User.State state = message.getContent(User.State.class);
		if(state == null) {
			sendHttpResponse(httpService, HttpResponseStatus.NOT_ACCEPTABLE, 
				"JSon content not of type User.State");
			return null;
		}
		
		// uid
		String uid = state.getUsername();
		if(!StringUtils.hasLength(uid)) {
			sendHttpResponse(httpService, HttpResponseStatus.NOT_ACCEPTABLE, 
					"No username found in User.State");
			return null;
		}
		if(StringUtils.hasLength(message.getActorId()) && !uid.equals(message.getActorId())) {
			log.warn(String.format("Username of state[%s] does not equal the UID found in url[%s]. "
					+ "Using [%s]", uid, message.getActorId(), uid));
		}
		try {
			return getSystem().actorOf(String.format("user/%s", uid), 
					User.class, state);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			sendHttpResponse(httpService, HttpResponseStatus.INTERNAL_SERVER_ERROR, 
					e.getMessage());
		}
		return null;
	}
	
	private void sendHttpResponse(ActorRef sender, HttpResponseStatus status, String response) {
		
		// headers
		Map<String,List<String>> headers = new HashMap<String,List<String>>();
        headers.put(HttpHeaders.Names.CONTENT_TYPE, Arrays.asList("text/plain"));

        // response
        if(StringUtils.isEmpty(response)) {
        	response = status.getReasonPhrase();
        }
        
		sender.tell(new HttpResponse(status.getCode(), headers, 
				response.getBytes(Charset.forName("UTF-8"))), getSelf());
	}
}
