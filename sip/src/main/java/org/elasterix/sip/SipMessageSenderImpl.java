package org.elasterix.sip;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.elasterix.sip.codec.SipClientCodec;
import org.elasterix.sip.codec.SipMessage;
import org.springframework.stereotype.Component;

/**
 * Standard implementation of <code>SipMessageSender</code>
 * Send incoming <code>SipMessage</code>'s to its belonging recipient.
 * <br>
 * <br>
 * This implementation takes automatically care of handling sockets and
 * connections, and reestablish connections to client's when required.<br> 
 * 
 * @author leonard
 *
 */
@Component
public class SipMessageSenderImpl implements SipMessageSender {
	private static final Logger log = Logger.getLogger(SipMessageSenderImpl.class);
	private SipClientCodec codec;
	
	@PostConstruct
	private void init() {
		codec = new SipClientCodec();
		log.info("Initialized SipClientCodec");
	}

	@Override
	public void sendMessage(SipMessage message, SipMessageCallback callback) {
		log.info(String.format("Incoming Message[%s]", message));
	}
}
