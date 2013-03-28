package org.elasterix.server.sip;

import org.apache.log4j.Logger;
import org.elasterix.sip.SipMessageCallback;

/**
 * @author Leonard Wolters
 */
public class SipMessageCallbackImpl implements SipMessageCallback {
	private static final Logger log = Logger.getLogger(SipMessageCallbackImpl.class);

	@Override
	public void callback(int statusCode) {
		log.info(String.format("Callback [%d]", statusCode));
	}
}
