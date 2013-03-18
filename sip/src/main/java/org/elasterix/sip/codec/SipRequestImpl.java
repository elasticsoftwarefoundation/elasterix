package org.elasterix.sip.codec;

/**
 * The default {@link SipRequest} implementation.
 * 
 * @author Leonard Wolters
 */
public class SipRequestImpl extends SipMessageImpl implements SipRequest {
    private SipMethod method;
    private String uri;

    /**
     * Creates a new instance.
     *
     * @param version     the SIP version of the request
     * @param method      the SIP method of the request
     * @param uri         the URI or path of the request
     */
    public SipRequestImpl(SipVersion version, SipMethod method, String uri) {
        super(version);
        setMethod(method);
        setUri(uri);
    }

    @Override
    public SipMethod getMethod() {
        return method;
    }

    private void setMethod(SipMethod method) {
        if (method == null) {
            throw new NullPointerException("method");
        }
        this.method = method;
    }

    @Override
    public String getUri() {
        return uri;
    }

    private void setUri(String uri) {
        if (uri == null) {
            throw new NullPointerException("uri");
        }
        this.uri = uri;
    }
}
