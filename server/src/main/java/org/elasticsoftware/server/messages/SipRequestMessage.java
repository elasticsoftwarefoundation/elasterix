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
import org.elasticsoftware.sip.codec.SipRequest;

/**
 * @author Leonard Wolters
 */
public class SipRequestMessage extends SipMessage {
    private final String uri;
    private final String method;

    public SipRequestMessage(SipRequest request) {
        super(request.getProtocolVersion().toString(), request.getHeaders(),
                null);
        this.uri = request.getUri();
        this.method = request.getMethod().name();
    }
    
    @JsonCreator
    public SipRequestMessage(@JsonProperty("uri") String uri,
    				   @JsonProperty("version") String version,
    				   @JsonProperty("method") String method,
                       @JsonProperty("headers") Map<String, List<String>> headers,
                       @JsonProperty("content") byte[] content) {
        super(version, headers, content);
        this.uri = uri;
        this.method = method;
    }

    @JsonProperty("uri")
    public String getUri() {
        return uri;
    }
    
    @JsonProperty("method")
    public String getMethod() {
        return method;
    }
}
