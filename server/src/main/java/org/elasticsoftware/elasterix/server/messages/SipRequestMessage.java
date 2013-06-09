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

package org.elasticsoftware.elasterix.server.messages;

import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.elasticsoftware.sip.codec.SipHeader;
import org.elasticsoftware.sip.codec.SipMethod;
import org.elasticsoftware.sip.codec.SipRequest;
import org.elasticsoftware.sip.codec.SipRequestImpl;
import org.elasticsoftware.sip.codec.SipResponseStatus;
import org.elasticsoftware.sip.codec.SipVersion;

/**
 * @author Leonard Wolters
 */
public class SipRequestMessage extends AbstractSipMessage {
    private final String uri;
    private final String method;
    
    private boolean authenticated = false;
    
    public SipRequestMessage(SipRequest request) {
        super(request.getVersion().toString(), request.getHeaders(),
                null);
        this.uri = request.getUri();
        this.method = request.getMethod().name();
    }
    
    @JsonCreator
    public SipRequestMessage( @JsonProperty("method") String method,
    				   @JsonProperty("uri") String uri,
    				   @JsonProperty("version") String version,
                       @JsonProperty("headers") Map<String, List<String>> headers,
                       @JsonProperty("content") byte[] content,
                       @JsonProperty("authenticated") boolean authenticated) {
        super(version, headers, content);
        this.uri = uri;
        this.method = method;
        this.authenticated = authenticated;
    }

    @JsonProperty("uri")
    public String getUri() {
        return uri;
    }
    
    @JsonProperty("method")
    public String getMethod() {
        return method;
    }
    
    @JsonProperty("authenticated")
    public boolean isAuthenticated() {
		return authenticated;
	}

	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}

    @JsonIgnore
    public SipMethod getSipMethod() {
        return SipMethod.lookup(method);
    }
    
    @Override
	public String toString() {
		return String.format("SipRequestMessage[%s, %s]", method, uri);
	}

    public SipResponseMessage toSipResponseMessage(SipResponseStatus status) {
    	return new SipResponseMessage(this.getVersion().toString(), status.getCode(), 
    			getHeaders(), getContent(), status.getOptionalMessage());
    }

    public SipRequest toSipRequest() {
    	SipRequest request = new SipRequestImpl(SipVersion.lookup(getVersion()), getSipMethod(), getUri());
    	for(Map.Entry<String, List<String>> entry : getHeaders().entrySet()) {
    		request.addHeader(SipHeader.lookup(entry.getKey()), entry.getValue().toArray());
    	}
    	// TODO fix content
    	return request;
    }

	@Override
	public String toShortString() {
		return String.format("%s %s %s", getMethod(), getUri(), getVersion());
	}
}
