package org.elasticsoftware.sip.codec.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.elasticsoftware.sip.codec.AbstractSipMessage;
import org.elasticsoftware.sip.codec.SipHeader;
import org.elasticsoftware.sip.codec.SipMessage;
import org.elasticsoftware.sip.codec.SipResponseStatus;
import org.elasticsoftware.sip.codec.SipVersion;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * The default {@link SipMessage} implementation.
 */
public class SipMessageImpl extends AbstractSipMessage {
	private final LinkedHashMap<String, List<String>> headers =
			new LinkedHashMap<String, List<String>>();
    private ChannelBuffer content = ChannelBuffers.EMPTY_BUFFER;

    public SipMessageImpl(SipVersion version) {
    	super(version);
    }
    
    public SipMessageImpl(SipResponseStatus responseStatus) {
    	super(SipVersion.SIP_2_0, responseStatus);
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
}