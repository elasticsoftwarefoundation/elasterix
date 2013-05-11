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
import org.elasticsoftware.server.actors.Dialog;
import org.elasticsoftware.server.messages.SipInvite;
import org.elasticsoftware.server.messages.SipMessage;
import org.elasticsoftware.server.messages.SipRegister;
import org.elasticsoftware.sip.SipMessageHandler;
import org.elasticsoftware.sip.SipMessageSender;
import org.elasticsoftware.sip.codec.SipHeader;
import org.elasticsoftware.sip.codec.SipRequest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of the Sip Message Handler.<br>
 * <br>
 * This is a Service Actor based implemenation.
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
		if(log.isDebugEnabled() && message instanceof SipMessage) {
			log.debug(String.format("onReceive. Status[%d]", ((SipMessage) message).getResponse()));
		}

    	if(message instanceof SipRegister) {
    		sendResponse((SipRegister) message);
		} else if (message instanceof SipInvite) {
    		sendResponse((SipInvite) message);
    	} else {
    		log.warn(String.format("onReceive. Unsupported message[%s]", 
					message.getClass().getSimpleName()));
			unhandled(message);
    	}
    }
    
    @Override
	public void onUndeliverable(ActorRef receiver, Object message) throws Exception {
		log.info(String.format("onUndeliverable. Message[%s]", message));
		if(message instanceof SipInvite|| message instanceof SipRegister) {
			SipMessage m = (SipMessage) message;

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
	public void onAck(SipRequest request) {
	}

	@Override
	public void onBye(SipRequest request) {
	}

	@Override
	public void onCancel(SipRequest request) {
	}

	@Override
	public void onInvite(SipRequest request) {
		if(log.isDebugEnabled()) log.debug(String.format("onInvite\n%s", request));
        tellDialog(new SipInvite(request));
	}

	@Override
	public void onOptions(SipRequest request) {
	}

	@Override
	public void onRegister(SipRequest request) {
		if(log.isDebugEnabled()) log.debug(String.format("onRegister\n%s", request));
        tellDialog(new SipRegister(request));
	}
	
	private void tellDialog(SipMessage message) {
		
		// get user ID (must be present, check done #SipServerHandler)
        String userId = message.getUser(SipHeader.FROM).getUsername();
        
        // get UAC ID (must be present, check done #SipServerHandler)
        String uacId = message.getUserAgentClient();
        
        // redirect to dialog actor
        ActorRef dialog = actorSystem.actorFor(String.format("dialog/%s_%s", userId, uacId));
        dialog.tell(message, actorSystem.serviceActorFor("sipService"));
	}
	
	//////////////////////////////////////////////////////////////////////////
	//
	//					Convenient
	//
	//////////////////////////////////////////////////////////////////////////
	
	/**
	 * SendResponse sends back a <b>acknowledge</b> response on incoming
	 * <code>SipRequest</code>. 
	 * 
	 * @param message The message to communicate back to sip client
	 */
	private void sendResponse(SipMessage message) {
		// set headers that must be always set
		message.setHeader(SipHeader.SERVER, "Server-001");
		message.setHeader(SipHeader.SUPPORTED, "replaces, timer");
		message.setHeader(SipHeader.ALLOW, "INVITE, ACK, CANCEL, OPTIONS, BYE, "
				+ "REFER, SUBSCRIBE, NOTIFY, INFO, PUBLISH");
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
