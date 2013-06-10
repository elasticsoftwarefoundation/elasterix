package org.elasticsoftware.sip;

import org.elasticsoftware.sip.codec.SipRequest;
import org.elasticsoftware.sip.codec.SipResponse;

/**
 * Handler for all <code>SipMethod</code>'s.<br>
 * This handler should be implemented by the third party
 * and is used by the {@link SipServerHandler} to trigger
 * corresponding (incoming) <code>SipRequest</code>'s and
 * <code>SipResponse</code>'s messages
 *
 * @author Leonard Wolters
 */
public interface SipMessageHandler {

    void onRequest(SipRequest request);

    void onResponse(SipResponse response);
}
