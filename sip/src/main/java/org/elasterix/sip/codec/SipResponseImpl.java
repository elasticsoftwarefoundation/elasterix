package org.elasterix.sip.codec;

/**
 * The default {@link SipResponse} implementation.
 * 
 * @author Leonard Wolters
 */
public class SipResponseImpl extends SipMessageImpl implements SipResponse {
	
    /**
     * Creates a new instance.
     *
     * @param version the Sip version of this response
     * @param responseStatus  the status of this response
     */
    public SipResponseImpl(SipVersion version, SipResponseStatus responseStatus) {
        super(version);
        setResponseStatus(responseStatus);
    }
}
