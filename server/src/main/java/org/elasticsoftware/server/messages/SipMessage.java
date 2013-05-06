/*
 * Copyright 2013 Joost van de Wijgerd, Leonard Wolters
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.elasticsoftware.server.messages;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.elasticsoftware.sip.codec.SipHeader;
import org.elasticsoftware.sip.codec.SipResponse;
import org.elasticsoftware.sip.codec.SipResponseStatus;
import org.elasticsoftware.sip.codec.SipServerCodec;
import org.elasticsoftware.sip.codec.SipVersion;
import org.elasticsoftware.sip.codec.impl.SipResponseImpl;
import org.elasticsoftware.sip.codec.netty.SipResponseNetty;
import org.springframework.util.StringUtils;

/**
 * @author Leonard Wolters
 */
public abstract class SipMessage {
	private int response;
	private String responseMessage;
	private String version;
    private final LinkedHashMap<String,List<String>> headers;
    protected final byte[] content;

    protected SipMessage(String version, Map<String, List<String>> headers, byte[] content) {
    	this.version = version;
    	this.headers = new LinkedHashMap<String,List<String>>();
    	this.headers.putAll(headers);
        this.content = content;
    }
    
    protected SipMessage(String version, List<Map.Entry<String, String>> headers, byte[] content) {
    	this.version = version;
    	this.headers = new LinkedHashMap<String, List<String>>();
        for(Map.Entry<String, String> entry : headers) {
        	List<String> values = new ArrayList<String>();
        	values.add(entry.getValue());
        	this.headers.put(entry.getKey(), values);
        }
        this.content = content;
    }
    
    public final String getHeader(SipHeader header) {
        List<String> headerValues = headers.get(header.getName());
        return headerValues == null || headerValues.isEmpty() ? null : headerValues.get(0);
    }
    
    public final Long getHeaderAsLong(SipHeader header) {
        List<String> headerValues = headers.get(header.getName());
        try {
        	return headerValues == null || headerValues.isEmpty() ? null : Long.parseLong(headerValues.get(0));
        } catch (NumberFormatException e) {
        	return null;
        }
    }
    
    public void addHeader(SipHeader header, String value) {
    	List<String> values = headers.get(header.getName());
    	if(values == null) {
    		values = new ArrayList<String>();
    		headers.put(header.getName(), values);
    	}
    	values.add(value);
    }
    
    public void setHeader(SipHeader header, String value) {
    	headers.remove(header.getName());
    	addHeader(header, value);
    }
    
    public void setHeader(SipHeader header, Object value) {
    	headers.remove(header.getName());
    	addHeader(header, value.toString());
    }

    @JsonIgnore
    public String getContentType() {
        return getHeader(SipHeader.CONTENT_TYPE);
    }

    @JsonProperty("content")
    public byte[] getContent() {
        return content;
    }
    
    @JsonProperty("response")
    public int getResponse() {
    	return response;
    }
    
    @JsonProperty("version")
    public String getVersion() {
    	return version;
    }
    
    @JsonProperty("headers")
    public Map<String, List<String>> getHeaders() {
        return headers;
    }
    
    @JsonProperty("responseMessage")
    public String getResponseMessage() {
        return responseMessage;
    }
    
    public SipMessage setSipResponseStatus(SipResponseStatus responseStatus, int a, int b) {
    	return setSipResponseStatus(responseStatus, null);
    }
    
    public SipMessage setSipResponseStatus(SipResponseStatus responseStatus, String message) {
    	this.response = responseStatus.getCode();
    	this.responseMessage = message;
    	return this;
    }
    
    public SipResponse toSipResponse() {
    	SipResponseStatus status = SipResponseStatus.lookup(response);
    	SipVersion version = SipVersion.lookup(this.version, true);
    	SipResponse response = null;
    	if(SipServerCodec.USE_NETTY_IMPLEMENTATION) {
        	response = new SipResponseNetty(version, status);
    	} else {
        	response = new SipResponseImpl(version, status);
    	}
    	for(Map.Entry<String, List<String>> entry : headers.entrySet()) {
    		response.addHeader(SipHeader.lookup(entry.getKey()), entry.getValue().toArray());
    	}
    	// TODO: fix content
    	return response;
    }
    
    @JsonIgnore
    public String getUser() {
    	String user = getHeader(SipHeader.TO);
    	if(!StringUtils.hasLength(user)) {
    		return user;
    	}
    	int idx = user.indexOf("sip:");
    	if(idx != -1) {
    		idx += 4;
    		int idx2 = user.indexOf("@", idx);
    		if(idx != -1) {
    			return user.substring(idx, idx2);
    		}
    	}
        return user;
    }
    
    @JsonIgnore
    public String getUserAgentClient() {
        return getHeader(SipHeader.CALL_ID);
    }
}
