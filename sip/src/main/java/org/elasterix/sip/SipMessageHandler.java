package org.elasterix.sip;

import org.elasterix.sip.codec.SipMessage;

/**
 * Handler for all <code>SipMethod</code>'s.<br>
 * This handler should be implemented by the third party
 * and is used by the {@link SipServerHandler} to trigger
 * corresponding (incoming) <code>SipMessage</code>'s 
 * 
 * @author Leonard Wolters
 */
public interface SipMessageHandler {
	
	void onAck(SipMessage message);
	
	void onBye(SipMessage message);
	
	void onCancel(SipMessage message);
	
	void onInvite(SipMessage message);
	
	void onOptions(SipMessage message);

	void onRegister(SipMessage message);
}
