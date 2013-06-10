package org.elasticsoftware.sip.codec;

/**
 * An SIP request.
 *
 * @author Leonard Wolters
 * @see SipResponse
 */
public interface SipRequest extends SipMessage {

    /**
     * Returns the method of this request.
     */
    SipMethod getMethod();

    /**
     * Returns the URI (or path) of this request.
     */
    String getUri();

    /**
     * Convert to sip response..
     *
     * @return
     */
    SipResponse toSipResponse();
}
