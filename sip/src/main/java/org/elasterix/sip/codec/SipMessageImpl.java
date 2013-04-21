package org.elasterix.sip.codec;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.internal.StringUtil;

/**
 * The default {@link SipMessage} implementation.
 */
public class SipMessageImpl implements SipMessage {
	private static final Logger log = Logger.getLogger(SipMessageImpl.class);
    private final SipHeaders headers = new SipHeaders();
    private final SipVersion version;
    private ChannelBuffer content = ChannelBuffers.EMPTY_BUFFER;
    private SipResponseStatus responseStatus;

    /**
     * Creates a new instance.
     */
    protected SipMessageImpl(final SipVersion version) {
    	this.version = version;
    }
    
    protected SipMessageImpl(SipResponseStatus responseStatus) {
    	this.version = SipVersion.SIP_2_0;
    	this.responseStatus = responseStatus;
    }

    @Override
    public void addHeader(final SipHeader header, final Object value) {
    	if(log.isDebugEnabled()) log.debug(String.format("addHeader. [%s] --> [%s]", header.getName(), value));
        headers.addHeader(header.getName(), value);
    }

    @Override
    public void setHeader(final SipHeader header, final Object value) {
        headers.setHeader(header.getName(), value);
    }

    @Override
    public void setHeader(final SipHeader header, final Iterable<?> values) {
        headers.setHeader(header.getName(), values);
    }

    @Override
    public void removeHeader(final SipHeader header) {
        headers.removeHeader(header.getName());
    }

    @Override
    public void clearHeaders() {
        headers.clearHeaders();
    }

    @Override
    public void setContent(ChannelBuffer content) {
        if (content == null) {
            content = ChannelBuffers.EMPTY_BUFFER;
        }
        this.content = content;
    }

    @Override
    public String getHeaderValue(final SipHeader header) {
        return headers.getHeader(header.getName());
    }

    @Override
    public List<String> getHeaderValues(final SipHeader header) {
        return headers.getHeaders(header.getName());
    }

    @Override
    public List<Map.Entry<String, String>> getHeaders() {
    	return headers.getHeaders();
    }

    @Override
    public boolean containsHeader(final SipHeader header) {
        return headers.containsHeader(header.getName());
    }

    @Override
    public Set<String> getHeaderNames() {
        return headers.getHeaderNames();
    }

    @Override
    public SipVersion getProtocolVersion() {
        return version;
    }

    @Override
    public ChannelBuffer getContent() {
        return content;
    }
    
    @Override
    public SipResponseStatus getResponseStatus() {
        return responseStatus;
    }

    @Override
    public void setResponseStatus(SipResponseStatus responseStatus) {
        if (responseStatus == null) {
            throw new NullPointerException("responseStatus");
        }
        this.responseStatus = responseStatus;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(getClass().getSimpleName());
        buf.append("(version: ");
        buf.append(getProtocolVersion().name());
        buf.append(')');
        buf.append(StringUtil.NEWLINE);
        appendHeaders(buf);

        // Remove the last newline.
        buf.setLength(buf.length() - StringUtil.NEWLINE.length());
        return buf.toString();
    }
    protected void appendHeaders(StringBuilder buf) {
        for (Map.Entry<String, String> e: getHeaders()) {
            buf.append(e.getKey());
            buf.append(": ");
            buf.append(e.getValue());
            buf.append(StringUtil.NEWLINE);
        }
    }
}
