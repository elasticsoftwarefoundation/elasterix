package org.elasterix.sip.codec.netty;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.elasterix.sip.codec.AbstractSipMessage;
import org.elasterix.sip.codec.SipHeader;
import org.elasterix.sip.codec.SipMessage;
import org.elasterix.sip.codec.SipResponseStatus;
import org.elasterix.sip.codec.SipVersion;

/**
 * The default {@link SipMessage} implementation.
 */
public class SipMessageNetty extends AbstractSipMessage {
    private final SipHeaders headers = new SipHeaders();

    public SipMessageNetty(final SipVersion version) {
    	super(version);
    }
    
    public SipMessageNetty(SipResponseStatus responseStatus) {
    	super(SipVersion.SIP_2_0, responseStatus);
    }

    @Override
    public void addHeader(final SipHeader header, final Object... value) {
    	for(Object obj : value) {
    		headers.addHeader(header.getName(), obj);
    	}
    }

    @Override
    public void setHeader(final SipHeader header, final Object... value) {
        headers.setHeader(header.getName(), value);
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
    public String getHeaderValue(final SipHeader header) {
        return headers.getHeader(header.getName());
    }

    @Override
    public List<String> getHeaderValues(final SipHeader header) {
        return headers.getHeaders(header.getName());
    }

    @Override
    public Map<String, List<String>> getHeaders() {
    	LinkedHashMap<String, List<String>> map = new LinkedHashMap<String, List<String>>();
    	for(Map.Entry<String, String> entry : headers.getHeaders()) {
    		List<String> list = map.get(entry.getKey());
    		if(list == null) {
    			list = new ArrayList<String>();
    			map.put(entry.getKey(), list);
    		}
    		list.add(entry.getValue());
    	}
    	return map;
    }

    @Override
    public boolean containsHeader(final SipHeader header) {
        return headers.containsHeader(header.getName());
    }

    @Override
    public Set<String> getHeaderNames() {
        return headers.getHeaderNames();
    }
}
