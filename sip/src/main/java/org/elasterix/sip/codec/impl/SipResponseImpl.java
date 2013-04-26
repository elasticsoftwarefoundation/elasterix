package org.elasterix.sip.codec.impl;

import org.elasterix.sip.codec.SipResponse;
import org.elasterix.sip.codec.SipResponseStatus;
import org.elasterix.sip.codec.SipVersion;
import org.jboss.netty.util.internal.StringUtil;

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
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(getProtocolVersion().name()).append(' ');
        buf.append(getResponseStatus().getCode()).append(' ');
        buf.append(getResponseStatus().getReasonPhrase());
        buf.append(StringUtil.NEWLINE);
        appendHeaders(buf);

        // Remove the last newline.
        buf.setLength(buf.length() - StringUtil.NEWLINE.length());
        return buf.toString();
    }
}
