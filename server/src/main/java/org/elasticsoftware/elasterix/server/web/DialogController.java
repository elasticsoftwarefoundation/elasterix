package org.elasticsoftware.elasterix.server.web;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.elasticsoftware.elasterix.server.ApiConfig;
import org.elasticsoftware.elasterix.server.actors.Dialog;
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
 * Dialog Controller<br>
 * <p/>
 * Overview of registered methods (urls):<br>
 * <ul>
 * <li>.../api/2.0/dialog/lwolters/create</li>
 * </ul>
 *
 * @author Leonard Wolters
 */
public class DialogController extends TypedActor<HttpRequest> {
    private static final Logger log = Logger.getLogger(DialogController.class);

    @Override
    public void postActivate(String previousVersion) throws Exception {
        log.info("Registering dailog controller");

        // register ourselves with the http server
        ActorSystem httpSystem = getSystem().getParent().get("Http");
        if (httpSystem != null) {
            ActorRef httpServer = httpSystem.serviceActorFor("httpServer");
            httpServer.tell(new RegisterRouteMessage("/api/*/dialog*/**", getSelf()), getSelf());
        } else {
            log.warn("Http ActorSystem not available");
        }
    }

    @Override
    public void onReceive(ActorRef httpService, HttpRequest message) throws Exception {
        ApiHttpMessage apiMessage = ApiHttpMessage.parse(message);
        if (apiMessage == null) {
            log.warn(String.format("Invalid url path[%s]", message.getUrl()));
            sendHttpResponse(httpService, HttpResponseStatus.BAD_REQUEST, null);
            return;
        }

        // double check domain
        if (!apiMessage.getDomain().toLowerCase().startsWith("dialog")) {
            log.warn(String.format("Not registered for this controller[%s != dialog]",
                    apiMessage.getDomain()));
            sendHttpResponse(httpService, HttpResponseStatus.BAD_REQUEST, null);
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s %s", apiMessage.getActorId(), apiMessage.getMethod()));
        }

        // create / update dialog ?
        ActorRef dialog = null;
        if (HttpMethod.POST == apiMessage.getMethod()) {
        	dialog = createActor(httpService, apiMessage);
            if (dialog == null) {
                // dialog not created, whilst it should. Response have been sent
                return;
            }
        } else {
            dialog = getSystem().actorFor(String.format("dialog/%s", apiMessage.getActorId()));
        }

        if (HttpMethod.DELETE == apiMessage.getMethod()) {
            if (ApiConfig.checkForExistenceBeforeDelete()) {
                dialog.tell(apiMessage, httpService);
            } else {
                // remove dialog directly (without checking if it exist)
                getSystem().stop(dialog);
                sendHttpResponse(httpService, HttpResponseStatus.OK, null);
            }
        } else {
            // ok, dispatch message to dialog, but use httpService as sender, since the onUndelivered
            // doesn't have a handle to the temporarily httpResponseActor
            dialog.tell(apiMessage, httpService);
        }
    }

    private ActorRef createActor(ActorRef httpService, ApiHttpMessage message) {

        if (!message.hasContent()) {
            sendHttpResponse(httpService, HttpResponseStatus.NO_CONTENT, null);
            return null;
        }
        if (message.getMethod() != HttpMethod.POST) {
            sendHttpResponse(httpService, HttpResponseStatus.NOT_ACCEPTABLE,
                    "Post method required");
            return null;
        }
        if (!message.hasJsonContentType()) {
            sendHttpResponse(httpService, HttpResponseStatus.NOT_ACCEPTABLE,
                    "No JSon content type");
            return null;
        }
        Dialog.State state = message.getContent(Dialog.State.class);
        if (state == null) {
            sendHttpResponse(httpService, HttpResponseStatus.NOT_ACCEPTABLE,
                    "JSon content not of type Dialog.State");
            return null;
        }

        // call-id
        String callId = state.getCallId();
        if (!StringUtils.hasLength(callId)) {
            sendHttpResponse(httpService, HttpResponseStatus.NOT_ACCEPTABLE,
                    "No Call-ID found in Dialog.State");
            return null;
        }
        if (StringUtils.hasLength(message.getActorId()) && !callId.equals(message.getActorId())) {
            log.warn(String.format("Call-ID of state[%s] does not equal the Call-ID found in url[%s]. "
                    + "Using [%s]", callId, message.getActorId(), callId));
        }
        try {
            return getSystem().actorOf(String.format("dialog/%s", callId), Dialog.class, state);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            sendHttpResponse(httpService, HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    e.getMessage());
        }
        return null;
    }

    private void sendHttpResponse(ActorRef sender, HttpResponseStatus status, String response) {

        // headers
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        headers.put(HttpHeaders.Names.CONTENT_TYPE, Arrays.asList("text/plain"));

        // response
        if (StringUtils.isEmpty(response)) {
            response = status.getReasonPhrase();
        }

        sender.tell(new HttpResponse(status.getCode(), headers,
                response.getBytes(Charset.forName("UTF-8"))), getSelf());
    }
}
