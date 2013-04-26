package org.elasterix.sip.codec;

import java.util.List;
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.internal.StringUtil;
import org.springframework.util.StringUtils;

/**
 * @author leonard Wolters
 */
public abstract class AbstractSipMessage implements SipMessage {
	protected final SipVersion version;
	protected SipResponseStatus responseStatus;
	protected ChannelBuffer content = ChannelBuffers.EMPTY_BUFFER;

	protected AbstractSipMessage(final SipVersion version) {
		this.version = version;
	}

	protected AbstractSipMessage(final SipVersion version, 
			final SipResponseStatus responseStatus) {
		this.version = version;
		this.responseStatus = responseStatus;
	}
	
    @Override
    public SipVersion getProtocolVersion() {
        return version;
    }

    @Override
    public void setContent(ChannelBuffer content) {
        if (content == null) {
            content = ChannelBuffers.EMPTY_BUFFER;
        }
        this.content = content;
    }

    @Override
    public ChannelBuffer getContent() {
        return content;
    }

	@Override
	public long getContentLength(long defaultValue) {
		String contentLength = getHeaderValue(SipHeader.CONTENT_LENGTH);
		if (contentLength != null) {
			return Long.parseLong(contentLength);
		}
		return defaultValue;
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
		for (Map.Entry<String, List<String>> e: getHeaders().entrySet()) {
			buf.append(e.getKey());
			buf.append(": ");
			buf.append(StringUtils.collectionToCommaDelimitedString(e.getValue()));
			buf.append(StringUtil.NEWLINE);
		}
	}
}
