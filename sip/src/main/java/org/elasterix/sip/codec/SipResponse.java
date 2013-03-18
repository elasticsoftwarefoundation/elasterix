package org.elasterix.sip.codec;

/**
 * An SIP response.
 *
 * @see SipRequest
 */
public interface SipResponse extends SipMessage {

    /**
     * Returns the status of this response.
     */
    SipResponseStatus getStatus();
}
