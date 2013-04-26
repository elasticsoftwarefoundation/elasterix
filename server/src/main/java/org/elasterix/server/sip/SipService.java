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
import org.elasterix.server.messages.SipMessage;
import org.elasterix.server.messages.SipRegister;
import org.elasterix.sip.SipMessageHandler;
import org.elasterix.sip.SipMessageSender;
import org.elasterix.sip.codec.SipRequest;
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

    	if(message instanceof SipRegister) {
    		onRegister(actorRef, (SipRegister) message);
    	} else {
    		log.warn(String.format("onReceive. Unsupported message[%s]", 
					message.getClass().getSimpleName()));
			unhandled(message);
    	}
    }
    
    protected void onRegister(ActorRef sender, SipRegister message) {
		if(log.isDebugEnabled()) log.debug(String.format("onRegister. Status[%d]",
				message.getResponse()));
		sendResponse(message);
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
	}

	@Override
	public void onOptions(SipRequest request) {
	}

	@Override
	public void onRegister(SipRequest request) {
		if(log.isDebugEnabled()) log.debug(String.format("onRegister\n%s", request));

		// Create SipRegister message (one that is suitable / serializable for
		// actor framework
        SipRegister message = new SipRegister(request);
		
		// Registering is a 'duplex' operation; i.e.
		// both user and device register each other. The User actor redirects
        // the register message to UserAgentClient actor automatically.
        ActorRef user = actorSystem.actorFor("user/" + message.getUser());
		user.tell(message, actorSystem.serviceActorFor("sipService"));		
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
		sipMessageSender.sendResponse(message.toSipResponse(), dummyCallback);
	}

    public void setActorSystem(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }
    
    public void setSipMessageSender(SipMessageSender sipMessageSender) {
		this.sipMessageSender = sipMessageSender;
	}
}
