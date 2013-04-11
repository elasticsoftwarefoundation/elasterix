package org.elasterix.server.sip;

import org.apache.log4j.Logger;
import org.elasterix.elasticactors.ActorRef;
import org.elasterix.elasticactors.ActorSystem;
import org.elasterix.elasticactors.TypedActor;
import org.elasterix.elasticactors.UntypedActor;
import org.elasterix.server.messages.SipRegister;
import org.elasterix.sip.SipMessageHandler;
import org.elasterix.sip.SipMessageSender;
import org.elasterix.sip.codec.SipHeader;
import org.elasterix.sip.codec.SipRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of the Sip Message Handler
 * 
 * @author Leonard Wolters
 */
public class SipService extends UntypedActor implements SipMessageHandler {
	private static final Logger log = Logger.getLogger(SipService.class);

	private SipMessageSender messageSender;
	
	private ActorSystem actorSystem;
	
	/**
	 * Constructor
	 */
	public SipService() {
	}

    @Override
    public void onReceive(Object o, ActorRef actorRef) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
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
        Map<String,List<String>> headers = new HashMap<String,List<String>>();
        for (String headerName : request.getHeaderNames()) {
            headers.put(headerName,request.getHeaderValues(SipHeader.valueOf(headerName)));
        }
        // make the SipRegister message
        SipRegister message = new SipRegister(request.getUri(),headers);

		
		// Registering is a 'duplex' operation; i.e.
		// both user and device registers both each other
		ActorRef user = actorSystem.actorFor(message.getUser());
        // @todo: figure out how to get the device from the register message
        //ActorRef device = actorSystem.actorFor("device");
        // @todo: probably set the sender to some local ref
		user.tell(message, null);
		//device.tell(message, user);
        
		// send response to recipient reflecting
		// current state of this SIP request
		// messageSender.sendRequest(request, null);
	}

    public void setActorSystem(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    public void setMessageSender(SipMessageSender messageSender) {
        this.messageSender = messageSender;
    }
}
