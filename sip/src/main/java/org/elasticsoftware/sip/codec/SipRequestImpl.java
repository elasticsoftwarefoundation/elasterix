package org.elasticsoftware.sip.codec;

import java.nio.charset.Charset;

import org.jboss.netty.util.internal.StringUtil;

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
     * @param version the SIP version of the request
     * @param method  the SIP method of the request
     * @param uri     the URI or path of the request
     */
    public SipRequestImpl(SipVersion version, SipMethod method, String uri) {
        super(version, null);
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

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(String.format("%s %s %s", getMethod().name(), getUri(), getVersion().name()));
        buf.append(StringUtil.NEWLINE);
        appendHeaders(buf);
        
        if(getContent() != null) {
        	buf.append(StringUtil.NEWLINE);
			buf.append(getContent().toString(Charset.forName("UTF-8")));
		}

        // Remove the last newline.
        buf.setLength(buf.length() - StringUtil.NEWLINE.length());
        return buf.toString();
    }

    @Override
    public SipResponse toSipResponse() {
        return new SipResponseImpl(this);
    }
}
