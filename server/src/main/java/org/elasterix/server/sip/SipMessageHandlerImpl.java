package org.elasterix.server.sip;

import org.apache.log4j.Logger;
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
		
		// do actor stuff.
		
		// send response to recipient reflecting
		// current state of this SIP request
		messageSender.sendRequest(request, null);
	}
}
