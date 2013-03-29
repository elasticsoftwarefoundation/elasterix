package org.elasterix.server.messages;

import org.elasterix.sip.codec.SipRequest;

/**
 * @author Leonard Wolters
 */
public class SipRegister extends AbstractSipMessage {

	/**
	 * Constructor
	 * 
	 * @param request
	 */
	public SipRegister(SipRequest request) {
		super(request);
	}
}
