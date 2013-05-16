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

package org.elasticsoftware.server.sip;

import org.apache.log4j.Logger;
import org.elasticsoftware.elasticactors.ActorRef;
import org.elasticsoftware.elasticactors.ActorSystem;
import org.elasticsoftware.elasticactors.UntypedActor;
import org.elasticsoftware.server.ServerConfig;
import org.elasticsoftware.server.actors.Dialog;
import org.elasticsoftware.server.messages.AbstractSipMessage;
import org.elasticsoftware.server.messages.SipRequestMessage;
import org.elasticsoftware.server.messages.SipResponseMessage;
import org.elasticsoftware.server.messages.SipUser;
import org.elasticsoftware.sip.SipMessageHandler;
import org.elasticsoftware.sip.SipMessageSender;
import org.elasticsoftware.sip.codec.SipHeader;
import org.elasticsoftware.sip.codec.SipRequest;
import org.elasticsoftware.sip.codec.SipResponse;
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

	/** Can't be autowired. Is automatically set be ElasterixServer */
	private ActorSystem actorSystem;
	
	private final SipMessageCallbackImpl dummyCallback = new SipMessageCallbackImpl();
	
	/**
	 * Constructor
	 */
	public SipService() {
	}

    @Override
    public void onReceive(ActorRef actorRef, Object message) throws Exception {
		if(log.isDebugEnabled() && message instanceof AbstractSipMessage) {
			log.debug(String.format("onReceive. Status[%d]", ((AbstractSipMessage) message).getResponse()));
		}

    	if(message instanceof SipRequestMessage) {
    		sendResponse((SipRequestMessage) message);
		} else if (message instanceof SipResponseMessage) {
    	} else {
    		log.warn(String.format("onReceive. Unsupported message[%s]", 
					message.getClass().getSimpleName()));
			unhandled(message);
    	}
    }
    
    @Override
	public void onUndeliverable(ActorRef receiver, Object message) throws Exception {
		log.info(String.format("onUndeliverable. Message[%s]", message));
		if(message instanceof SipRequestMessage) {
			SipRequestMessage m = (SipRequestMessage) message;

			// Create new dialog actor
			String uid = m.getUser(SipHeader.FROM).getUsername();
			String uacId = m.getUserAgentClient();
			ActorRef actor = getSystem().actorOf(String.format("dialog/%s_%s", uid, uacId), 
					Dialog.class, new Dialog.State(uid, uacId));
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
		if(log.isDebugEnabled()) log.debug(String.format("onRequest\n%s", request));
        tellDialog(new SipRequestMessage(request));
	}
    
    @Override
	public void onResponse(SipResponse response) {
		if(log.isDebugEnabled()) log.debug(String.format("onResponse\n%s", response));
	}
	
	private void tellDialog(AbstractSipMessage message) {
		
		// get user ID (must be present, check done #SipServerHandler)
        String userId = message.getUser(SipHeader.FROM).getUsername();
        
        // get UAC ID (must be present, check done #SipServerHandler)
        String uacId = message.getUserAgentClient();
        
        // redirect to dialog actor. Order of actorId matters: 
        // userId -> uacId == incoming (from uac to server)
        // uacId -> userId == outgoing (from server to uac)
        ActorRef dialog = actorSystem.actorFor(String.format("dialog/%s_%s", userId, uacId));
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
	private void sendRequest(SipRequestMessage message) {
	}
	
	/**
	 * SendResponse sends back a <b>acknowledge</b> response on incoming
	 * <code>SipRequest</code>. 
	 * 
	 * @param message The message to communicate back to sip client
	 */
	private void sendResponse(AbstractSipMessage message) {
		
		//
		// set required headers 		
		// 
		message.setHeader(SipHeader.ALLOW, ServerConfig.getAllow());
		message.setHeader(SipHeader.DATE, ServerConfig.getDateNow());
		message.setHeader(SipHeader.SERVER, ServerConfig.getServerName());
		message.setHeader(SipHeader.SUPPORTED, ServerConfig.getSupported());
		
		// 
		// alter existing headers
		//
		SipUser contact = message.getUser(SipHeader.CONTACT);
		message.appendHeader(SipHeader.VIA, "received", contact.getDomain());
		message.appendHeader(SipHeader.VIA, "rport", Long.toString(contact.getPort()));
		
		//
		// Optionally, remove headers
		//
		message.removeHeader(SipHeader.MAX_FORWARDS);
		message.removeHeader(SipHeader.USER_AGENT);
				
		if(message.getContent() == null || message.getContent().length == 0) {
			message.setHeader(SipHeader.CONTENT_LENGTH, 0);
		}		
		sipMessageSender.sendResponse(message.toSipResponse(), dummyCallback);
	}

    public void setActorSystem(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }
    
    public void setSipMessageSender(SipMessageSender sipMessageSender) {
		this.sipMessageSender = sipMessageSender;
	}
}
