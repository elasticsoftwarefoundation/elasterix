package org.elasterix.sip.codec;

/**
 * The default {@link SipResponse} implementation.
 * 
 * @author Leonard Wolters
 */
public class SipResponseImpl extends SipMessageImpl implements SipResponse {

    private SipResponseStatus status;

    /**
     * Creates a new instance.
     *
     * @param version the Sip version of this response
     * @param status  the status of this response
     */
    public SipResponseImpl(SipVersion version, SipResponseStatus status) {
        super(version);
        setStatus(status);
    }

    @Override
    public SipResponseStatus getStatus() {
        return status;
    }

    private void setStatus(SipResponseStatus status) {
        if (status == null) {
            throw new NullPointerException("status");
        }
        this.status = status;
    }
}
