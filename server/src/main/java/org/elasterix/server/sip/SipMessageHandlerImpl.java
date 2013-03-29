package org.elasterix.server.sip;

import org.apache.log4j.Logger;
import org.elasterix.elasticactors.ActorRef;
import org.elasterix.elasticactors.ActorSystem;
import org.elasterix.server.messages.SipRegister;
import org.elasterix.sip.SipMessageHandler;
import org.elasterix.sip.SipMessageSender;
import org.elasterix.sip.codec.SipRequest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of the Sip Message Handler
 * 
 * @author Leonard Wolters
 */
public class SipMessageHandlerImpl implements SipMessageHandler {
	private static final Logger log = Logger.getLogger(SipMessageHandlerImpl.class);
	
	@Autowired
	private SipMessageSender messageSender;
	
	private final ActorSystem actorSystem;
	
	/**
	 * Constructor
	 * @param actorSystem
	 */
	public SipMessageHandlerImpl(ActorSystem actorSystem) {
		this.actorSystem = actorSystem;
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
		SipRegister message = new SipRegister(request);
		
		// Registering is a 'duplex' operation; i.e.
		// both user and device registers both each other
		ActorRef user = actorSystem.actorFor("user");
        ActorRef device = actorSystem.actorFor("device");
		user.tell(message, device);
		device.tell(message, user);
        
		// send response to recipient reflecting
		// current state of this SIP request
		messageSender.sendRequest(request, null);
	}
}
