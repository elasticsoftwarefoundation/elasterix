package org.elasticsoftware.sip.codec;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.internal.StringUtil;
import org.springframework.util.StringUtils;

/**
 * @author leonard Wolters
 */
public class SipMessageImpl implements SipMessage {
	private final LinkedHashMap<String, List<String>> headers =
			new LinkedHashMap<String, List<String>>();
	private final SipVersion version;
	private SipResponseStatus responseStatus;
	private ChannelBuffer content = ChannelBuffers.EMPTY_BUFFER;

	protected SipMessageImpl(final SipVersion version, 
			final SipResponseStatus responseStatus) {
		this.version = version;
		this.responseStatus = responseStatus;
	}
	
	protected SipMessageImpl(SipMessage message) {
		this.headers.putAll(message.getHeaders());
		this.version = message.getVersion();
		this.responseStatus = message.getResponseStatus();
		this.content = message.getContent();
	}
	
    @Override
    public SipVersion getVersion() {
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
    public void addHeader(final SipHeader header, final Object... values) {
    	List<String> list = headers.get(header.getName());
    	if(list == null) {
    		list = new ArrayList<String>(values.length);
    		headers.put(header.getName(), list);
    	}
    	for(Object value : values) {
    		list.add(value.toString());
    	}
    }

    @Override
    public void setHeader(final SipHeader header, final Object... values) {
    	removeHeader(header);
        addHeader(header, values);
    }

    @Override
    public void removeHeader(final SipHeader header) {
        headers.remove(header.getName());
    }

    @Override
    public void clearHeaders() {
        headers.clear();
    }

    @Override
    public String getHeaderValue(final SipHeader header) {
    	List<String> list = headers.get(header.getName());
    	if(list != null && list.size() > 0) {
    		return list.get(0);
    	}
        return null;
    }

    @Override
    public List<String> getHeaderValues(final SipHeader header) {
        return headers.get(header.getName());
    }

    @Override
    public Map<String, List<String>> getHeaders() {
    	return headers;
    }

    @Override
    public boolean containsHeader(final SipHeader header) {
        return headers.containsKey(header.getName());
    }

    @Override
    public Set<String> getHeaderNames() {
        return headers.keySet();
    }
    
    @Override
    public SipUser getSipUser(SipHeader header) {
    	if(header == null) {
    		header = SipHeader.TO;
    	}
    	String user = getHeaderValue(header);
    	if(!StringUtils.hasLength(user)) {
    		return null;
    	}
    	return new SipUser(user);
    }

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append(getClass().getSimpleName());
		buf.append("(version: ");
		buf.append(getVersion().name());
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
