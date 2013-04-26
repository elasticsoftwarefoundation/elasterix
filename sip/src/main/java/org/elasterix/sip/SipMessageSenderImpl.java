package org.elasterix.sip;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.elasterix.sip.codec.SipClientCodec;
import org.elasterix.sip.codec.SipHeader;
import org.elasterix.sip.codec.SipRequest;
import org.elasterix.sip.codec.SipResponse;
import org.springframework.stereotype.Component;

/**
 * Standard implementation of <code>SipMessageSender</code>
 * Send incoming <code>SipMessage</code>'s to its belonging recipient.
 * <br>
 * <br>
 * This implementation takes automatically care of handling sockets and
 * connections, and reestablish connections to client's when required.<br> 
 * 
 * @author Leonard Wolters
 */
@Component
public class SipMessageSenderImpl implements SipMessageSender {
	private static final Logger log = Logger.getLogger(SipMessageSenderImpl.class);
	private SipClientCodec codec;
	
	/** 
	 * Keep track of users and their channel in order to quickly communicate or
	 * transfer sip messages
	 */
	
	@PostConstruct
	private void init() {
		codec = new SipClientCodec();
		log.info("Initialized SipClientCodec");
	}

	@Override
	public void sendRequest(SipRequest request, SipMessageCallback callback) {
		log.info(String.format("Sending Request\n%s", request));
		
		// get existing channel or open a new one for 'recipient' of this
		// message
		String to = request.getHeaderValue(SipHeader.FROM);
	}

	@Override
	public void sendResponse(SipResponse response, SipMessageCallback callback) {
		log.info(String.format("Sending Response\n%s", response));
	}
}
