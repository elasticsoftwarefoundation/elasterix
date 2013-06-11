package org.elasticsoftware.sip.codec;

import java.nio.charset.Charset;

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
     * @param version        the Sip version of this response
     * @param responseStatus the status of this response
     */
    public SipResponseImpl(SipVersion version, SipResponseStatus responseStatus) {
        super(version, responseStatus);
    }

    protected SipResponseImpl(SipMessage message) {
        super(message);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(getVersion().name()).append(' ');
        buf.append(getResponseStatus().getCode()).append(' ');
        buf.append(getResponseStatus().getReasonPhrase());
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
}
