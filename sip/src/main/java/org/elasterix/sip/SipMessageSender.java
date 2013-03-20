package org.elasterix.sip;

import org.elasterix.sip.codec.SipMessage;

/**
 * Interface for sending messages<br>
 * To be used by third party, and when using Spring make sure to
 * set it by using the <code>Autowired</code> annotation 
 * 
 * @author Leonard Wolters
 */
public interface SipMessageSender {

	/**
	 * Sends a SIP message to the corresponding recipient,
	 * as defined in the message.
	 * 
	 * @param message
	 */
	void sendMessage(SipMessage message, SipMessageCallback callback);
}
