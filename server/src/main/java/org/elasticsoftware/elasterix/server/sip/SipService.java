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

package org.elasticsoftware.elasterix.server.sip;

import org.apache.log4j.Logger;
import org.elasticsoftware.elasterix.server.actors.Dialog;
import org.elasticsoftware.elasterix.server.messages.AbstractSipMessage;
import org.elasticsoftware.elasterix.server.messages.SipRequestMessage;
import org.elasticsoftware.elasterix.server.messages.SipResponseMessage;
import org.elasticsoftware.elasticactors.ActorRef;
import org.elasticsoftware.elasticactors.ActorSystem;
import org.elasticsoftware.elasticactors.UntypedActor;
import org.elasticsoftware.sip.SipMessageHandler;
import org.elasticsoftware.sip.SipMessageSender;
import org.elasticsoftware.sip.codec.SipHeader;
import org.elasticsoftware.sip.codec.SipRequest;
import org.elasticsoftware.sip.codec.SipResponse;
import org.elasticsoftware.sip.codec.SipUser;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of the Sip Message Handler.<br>
 * <br>
 * This is a Service Actor based implementation.
 *
 * @author Leonard Wolters
 */
public final class SipService extends UntypedActor implements SipMessageHandler {
    private static final Logger log = Logger.getLogger(SipService.class);

    @Autowired
    private SipMessageSender sipMessageSender;

    /**
     * Can't be autowired. Is automatically set be ElasterixServer
     */
    private ActorSystem actorSystem;

    private final SipMessageCallbackImpl dummyCallback = new SipMessageCallbackImpl();

    /**
     * Constructor
     */
    public SipService() {
    }

    @Override
    public void onReceive(ActorRef actorRef, Object message) throws Exception {

        if (message instanceof SipRequestMessage) {
            SipRequestMessage m = (SipRequestMessage) message;
            if (log.isDebugEnabled()) log.debug(String.format("onReceive. Sending request[%s]", m.toShortString()));
            sendRequest(m);
        } else if (message instanceof SipResponseMessage) {
            SipResponseMessage m = (SipResponseMessage) message;
            if (log.isDebugEnabled()) log.debug(String.format("onReceive. Sending response[%s]", m.toShortString()));
            sendResponse(m);
        } else {
            log.warn(String.format("onReceive. Unsupported message[%s]",
                    message.getClass().getSimpleName()));
            unhandled(message);
        }
    }

    @Override
    public void onUndeliverable(ActorRef receiver, Object message) throws Exception {
        if (message instanceof SipRequestMessage) {
            SipRequestMessage m = (SipRequestMessage) message;
            
            // Create new dialog actor
            SipUser user = m.getSipUser(SipHeader.FROM);
            String callId = m.getCallDialog();
            if (log.isDebugEnabled()) {
                log.debug(String.format("onUndeliverable. Creating new dialog[%s]", callId));
            }
            ActorRef actor = getSystem().actorOf(String.format("dialog/%s", callId),
                    Dialog.class, new Dialog.State(user.getUsername(), callId, m.getMethod()));
            actor.tell(message, getSelf());
        } else {
            unhandled(message);
        }
    }

    //////////////////////////////////////////////////////////////////////////
    //
    //					SipMessageHandler
    //
    //////////////////////////////////////////////////////////////////////////

    @Override
    public void onRequest(SipRequest request) {
        SipRequestMessage m = new SipRequestMessage(request);
        if (log.isDebugEnabled()) log.debug(String.format("onRequest: %s", m.toShortString()));
        tellDialog(m);
    }

    @Override
    public void onResponse(SipResponse response) {
        SipResponseMessage m = new SipResponseMessage(response);
        if (log.isDebugEnabled()) log.debug(String.format("onResponse: %s", m.toShortString()));
    }

    private void tellDialog(AbstractSipMessage message) {

        // get call ID (must be present, check done #SipServerHandler)
        String callId = message.getCallDialog();

        // redirect to dialog actor. 
        ActorRef dialog = actorSystem.actorFor(String.format("dialog/%s", callId));
        dialog.tell(message, actorSystem.serviceActorFor("sipService"));
    }
    
	//////////////////////////////////////////////////////////////////////////
	//
	//					Convenient
	//
	//////////////////////////////////////////////////////////////////////////
	
	/**
	 * sendRequest sends forth a <b>SIP</b> request to the Contact
	 * header
	 * 
	 * @param message The message to send along
	 */
	private void sendResponse(SipResponseMessage message) {
		SipMessageHelper.checkResponse(message);
		sipMessageSender.sendResponse(message.toSipResponse(), dummyCallback);
	}
	
	/**
	 * SendResponse sends back a <b>acknowledge</b> response on incoming
	 * <code>SipRequest</code>. 
	 * 
	 * @param message The message to communicate back to sip client
	 */
	private void sendRequest(SipRequestMessage message) {
		SipMessageHelper.checkRequest(message);
		sipMessageSender.sendRequest(message.toSipRequest(), dummyCallback);
	}

    public void setActorSystem(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    public void setSipMessageSender(SipMessageSender sipMessageSender) {
        this.sipMessageSender = sipMessageSender;
    }
}
