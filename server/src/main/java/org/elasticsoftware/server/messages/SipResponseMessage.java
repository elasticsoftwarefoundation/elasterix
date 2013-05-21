/*
 * Copyright 2013 Leonard Wolters
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

import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.elasticsoftware.sip.codec.SipHeader;
import org.elasticsoftware.sip.codec.SipResponse;
import org.elasticsoftware.sip.codec.SipResponseImpl;
import org.elasticsoftware.sip.codec.SipResponseStatus;
import org.elasticsoftware.sip.codec.SipVersion;

/**
 * @author Leonard Wolters
 */
public class SipResponseMessage extends AbstractSipMessage {
	private int response;
	private String responseMessage;
   
    public SipResponseMessage(SipResponse response) {
        super(response.getVersion().toString(), response.getHeaders(),
                null);
        setSipResponseStatus(response.getResponseStatus());
    }
    
    @JsonCreator
    public SipResponseMessage(@JsonProperty("version") String version,
    				   @JsonProperty("response") int response,
                       @JsonProperty("headers") Map<String, List<String>> headers,
                       @JsonProperty("content") byte[] content,
    				   @JsonProperty("responseMessage") String responseMessage) {
        super(version, headers, content);
        this.response = response;
        this.responseMessage = responseMessage;
    }
    
    @Override
	public String toString() {
		return String.format("SipResponseMessage[%d, %s]", getResponse(), getResponseMessage());
	}
    
    public SipResponseMessage setSipResponseStatus(SipResponseStatus responseStatus) {
    	this.response = responseStatus.getCode();
    	this.responseMessage = responseStatus.getOptionalMessage();
    	return this;
    }
    
    @JsonProperty("responseMessage")
    public String getResponseMessage() {
        return responseMessage;
    }
    
    @JsonProperty("response")
    public int getResponse() {
    	return response;
    }
    
    public SipResponse toSipResponse() {
    	SipResponseStatus status = SipResponseStatus.lookup(response);
    	status.setOptionalMessage(responseMessage);
    	SipVersion version = SipVersion.lookup(getVersion(), true);
    	SipResponse response = new SipResponseImpl(version, status);
    	for(Map.Entry<String, List<String>> entry : getHeaders().entrySet()) {
    		response.addHeader(SipHeader.lookup(entry.getKey()), entry.getValue().toArray());
    	}
    	// TODO: fix content
    	return response;
    }
}
