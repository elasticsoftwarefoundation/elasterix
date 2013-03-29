package org.elasterix.server.messages;

import org.elasterix.sip.codec.SipMessage;

/**
 * @author Leonard Wolters
 */
public abstract class AbstractSipMessage {
	private final SipMessage message;

	protected AbstractSipMessage(SipMessage sipMessage) {
		this.message = sipMessage;
	}
	
	public SipMessage getSipMessage() {
		return message;
	}
}
