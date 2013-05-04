package org.elasticsoftware.sip;

import org.elasticsoftware.sip.codec.SipRequest;

/**
 * Handler for all <code>SipMethod</code>'s.<br>
 * This handler should be implemented by the third party
 * and is used by the {@link SipServerHandler} to trigger
 * corresponding (incoming) <code>SipRequest</code>'s 
 * 
 * @author Leonard Wolters
 */
public interface SipMessageHandler {
	
	void onAck(SipRequest request);
	
	void onBye(SipRequest request);
	
	void onCancel(SipRequest request);
	
	void onInvite(SipRequest request);
	
	void onOptions(SipRequest request);

	void onRegister(SipRequest request);
}
