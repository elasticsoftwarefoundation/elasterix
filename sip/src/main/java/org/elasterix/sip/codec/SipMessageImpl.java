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
    private SipVersion version;
    private ChannelBuffer content = ChannelBuffers.EMPTY_BUFFER;
    private SipResponseStatus responseStatus;

    /**
     * Creates a new instance.
     */
    protected SipMessageImpl(final SipVersion version) {
        setProtocolVersion(version);
    }
    
    protected SipMessageImpl(SipResponseStatus responseStatus) {
    	this.responseStatus = responseStatus;
    }

    @Override
    public void addHeader(final String name, final Object value) {
    	if(log.isDebugEnabled()) log.debug(String.format("addHeader. [%s] --> [%s]", name, value));
        headers.addHeader(name, value);
    }

    @Override
    public void setHeader(final String name, final Object value) {
        headers.setHeader(name, value);
    }

    @Override
    public void setHeader(final String name, final Iterable<?> values) {
        headers.setHeader(name, values);
    }

    @Override
    public void removeHeader(final String name) {
        headers.removeHeader(name);
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
    public String getHeader(final String name) {
        return headers.getHeader(name);
    }

    @Override
    public List<String> getHeaders(final String name) {
        return headers.getHeaders(name);
    }

    @Override
    public List<Map.Entry<String, String>> getHeaders() {
        return headers.getHeaders();
    }

    @Override
    public boolean containsHeader(final String name) {
        return headers.containsHeader(name);
    }

    @Override
    public Set<String> getHeaderNames() {
        return headers.getHeaderNames();
    }

    @Override
    public SipVersion getProtocolVersion() {
        return version;
    }

    private void setProtocolVersion(SipVersion version) {
        if (version == null) {
            throw new NullPointerException("version");
        }
        this.version = version;
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
    private void appendHeaders(StringBuilder buf) {
        for (Map.Entry<String, String> e: getHeaders()) {
            buf.append(e.getKey());
            buf.append(": ");
            buf.append(e.getValue());
            buf.append(StringUtil.NEWLINE);
        }
    }
}
