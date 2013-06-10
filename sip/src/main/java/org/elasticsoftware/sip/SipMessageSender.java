package org.elasticsoftware.sip;

import org.elasticsoftware.sip.codec.SipRequest;
import org.elasticsoftware.sip.codec.SipResponse;

/**
 * Interface for sending messages<br>
 * To be used by third party, and when using Spring make sure to
 * set it by using the <code>Autowired</code> annotation
 *
 * @author Leonard Wolters
 */
public interface SipMessageSender {

    /**
     * Sends a SIP response to the corresponding recipient,
     * as defined in the message.
     *
     * @param message
     */
    void sendResponse(SipResponse response, SipMessageCallback callback);

    /**
     * Sends a SIP request to the corresponding recipient,
     * as defined in the message.
     *
     * @param message
     */
    void sendRequest(SipRequest request, SipMessageCallback callback);
}
