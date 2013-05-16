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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.elasticsoftware.sip.codec.SipHeader;
import org.elasticsoftware.sip.codec.SipResponse;
import org.elasticsoftware.sip.codec.SipResponseImpl;
import org.elasticsoftware.sip.codec.SipResponseStatus;
import org.elasticsoftware.sip.codec.SipVersion;
import org.springframework.util.StringUtils;

/**
 * @author Leonard Wolters
 */
public abstract class AbstractSipMessage {
	private int response;
	private String responseMessage;
	private String version;
    private final LinkedHashMap<String,List<String>> headers = new LinkedHashMap<String,List<String>>();
    protected final byte[] content;

    protected AbstractSipMessage(String version, Map<String, List<String>> headers, byte[] content) {
    	this.version = version;
    	if(headers != null) {
    		this.headers.putAll(headers);
    	}
        this.content = content;
    }
    
    protected AbstractSipMessage(String version, List<Map.Entry<String, String>> headers, byte[] content) {
    	this.version = version;
    	if(headers != null) {
	        for(Map.Entry<String, String> entry : headers) {
	        	List<String> values = new ArrayList<String>();
	        	values.add(entry.getValue());
	        	this.headers.put(entry.getKey(), values);
	        }
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
    
    public boolean appendHeader(SipHeader header, String key, String value) {
    	String val = getHeader(header);
    	if(StringUtils.hasLength(val)) {
    		// TODO check for duplicate?
    		setHeader(header, String.format("%s;%s=%s", val, key, value));
    		return true;
    	}
    	return false;
    }
    
    public boolean removeHeader(SipHeader header) {
    	return headers.remove(header.getName()) != null;
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
    
    public AbstractSipMessage setSipResponseStatus(SipResponseStatus responseStatus, int a, int b) {
    	return setSipResponseStatus(responseStatus, null);
    }
    
    public AbstractSipMessage setSipResponseStatus(SipResponseStatus responseStatus, String message) {
    	this.response = responseStatus.getCode();
    	this.responseMessage = message;
    	return this;
    }
    
    public SipResponse toSipResponse() {
    	SipResponseStatus status = SipResponseStatus.lookup(response);
    	SipVersion version = SipVersion.lookup(this.version, true);
    	SipResponse response = new SipResponseImpl(version, status);
    	for(Map.Entry<String, List<String>> entry : headers.entrySet()) {
    		response.addHeader(SipHeader.lookup(entry.getKey()), entry.getValue().toArray());
    	}
    	// TODO: fix content
    	return response;
    }
    
    /**
     * Parses a traditional sip user element belonging to given header, e.g. <br>
     * "Hans de Borst"<sip:124@sip.outerteams.com:5060>;tag=ce337d00<br>
     * If no header is passed, SipHeader.TO will be used
     * 
     * @param header
     * @return
     */
    @JsonIgnore
    public SipUser getUser(SipHeader header) {
    	if(header == null) {
    		header = SipHeader.TO;
    	}
    	String user = getHeader(header);
    	if(!StringUtils.hasLength(user)) {
    		return null;
    	}
    	return new SipUser(user);
    }
    
    @JsonIgnore
    public String getUserAgentClient() {
        return getHeader(SipHeader.CALL_ID);
    }
	
	/**
	 * Tokenizes value belonging to given header, e.g.
	 * Authorization: Digest username="124",realm="elasticsoftware",nonce="24855234",
	 * uri="sip:sip.outerteams.com:5060",response="749c35e9fe30d6ba46cc801bdfe535a0",algorithm=MD5
	 * <br>
	 * <br>
	 * will be tokenized into:
	 * <ol>
	 *  <li>digest      -> digest</li>
	 *  <li>username    -> 124</li>
	 *  <li>realm       -> elasticsoftware</li>
	 *  <li>nonce       -> 24855234</li>
	 *  <li>uri         -> sip:sip.outerteams.com:5060</li>
	 *  <li>response    -> 749c35e9fe30d6ba46cc801bdfe535a0</li>
	 *  <li>algorithm   -> MD5</li>
	 * </ol>
	 * <br>
	 * <b>This method does **not** handle spaces and comma correctly in attribute values</b>
	 * 
	 * @param value
	 * @return
	 */
	public Map<String, String> tokenize(SipHeader header) {
		Map<String, String> map = new HashMap<String, String>();
		
		// sanity check
		String value = getHeader(header);
		if(StringUtils.isEmpty(value)) {
			return map;
		}
		
		// Authorization: Digest username="124",realm="elasticsoftware",nonce="24855234",
		// uri="sip:sip.outerteams.com:5060",response="749c35e9fe30d6ba46cc801bdfe535a0",algorithm=MD5
		StringTokenizer st = new StringTokenizer(value, " ,", false);
		while(st.hasMoreTokens()) {
			String token = st.nextToken();
			int idx = token.indexOf("=");
			if(idx != -1) {
				map.put(token.substring(0, idx).toLowerCase(), 
						token.substring(idx+1).replace('\"', ' ').trim());
			} else {
				map.put(token.toLowerCase(), token);
			}
		}
		return map;
	}
}
