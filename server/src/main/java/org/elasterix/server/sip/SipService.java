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

package org.elasterix.server.sip;

import org.apache.log4j.Logger;
import org.elasterix.elasticactors.ActorRef;
import org.elasterix.elasticactors.ActorSystem;
import org.elasterix.elasticactors.UntypedActor;
import org.elasterix.server.actors.UserAgentClient;
import org.elasterix.server.messages.SipMessage;
import org.elasterix.server.messages.SipRegister;
import org.elasterix.sip.SipMessageHandler;
import org.elasterix.sip.SipMessageSender;
import org.elasterix.sip.codec.SipRequest;
import org.elasterix.sip.codec.SipResponseImpl;
import org.elasterix.sip.codec.SipResponseStatus;
import org.elasterix.sip.codec.SipVersion;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of the Sip Message Handler.<br>
 * <br>
 * This is a Service Actor based implemenation.
 * 
 * @author Leonard Wolters
 */
public class SipService extends UntypedActor implements SipMessageHandler {
	private static final Logger log = Logger.getLogger(SipService.class);

	@Autowired
	private SipMessageSender messageSender;
	
	/** Can't be autowired. Is automatically set be ElasterixServer */
	private ActorSystem actorSystem;
	
	private final SipMessageCallbackImpl dummyCallback = new SipMessageCallbackImpl();
	
	/**
	 * Constructor
	 */
	public SipService() {
	}

    @Override
    public void onReceive(ActorRef actorRef, Object o) throws Exception {

    }

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
	}

	@Override
	public void onOptions(SipRequest request) {
	}

	@Override
	public void onRegister(SipRequest request) {
		if(log.isDebugEnabled()) log.debug(String.format("onRegister\n%s", request));

		// Create SipRegister message (one that is suitable / serializable for
		// actor framework
        SipRegister message = new SipRegister(request.getUri(), request.getHeaders());
		
		// Registering is a 'duplex' operation; i.e.
		// both user and device registers both each other
		ActorRef user = actorSystem.actorFor("user/" + message.getUser());
		// always create a new user agent client (uac)
        ActorRef uac = null;
		try {
			if(log.isDebugEnabled()) log.debug(String.format(
					"onRegister. Creating UAC[%s]", message.getUAC()));
			uac = actorSystem.actorOf("uac/" + message.getUAC(), UserAgentClient.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			sendResponse(SipResponseStatus.SERVER_INTERNAL_ERROR, message);
			return;
		}

		// send register message to both actors
		user.tell(message, actorSystem.serviceActorFor("sipService"));
		uac.tell(message, actorSystem.serviceActorFor("sipService"));
        
		// send response to recipient reflecting
		// current state of this SIP request
		sendResponse(SipResponseStatus.OK, message);
	}
	
	/**
	 * SendResponse sends back a <b>acknowledge</b> response on incoming
	 * <code>SipRequest</code>. 
	 * 
	 * @param status The status to communicate back
	 */
	private void sendResponse(SipResponseStatus status, SipMessage message) {
		// convert sip message to one that can be used by sip protocol
		messageSender.sendResponse(new SipResponseImpl(SipVersion.SIP_2_0, 
				status), dummyCallback);
	}

    public void setActorSystem(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    public void setMessageSender(SipMessageSender messageSender) {
        this.messageSender = messageSender;
    }
}
