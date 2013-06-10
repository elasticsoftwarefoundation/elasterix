/*
 * Copyright 2013 Joost van de Wijgerd
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

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.elasticsoftware.elasticactors.http.messages.HttpRequest;
import org.elasticsoftware.elasticactors.http.messages.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Leonard Wolters
 */
public class ApiHttpMessage {
    private static final Logger log = Logger.getLogger(ApiHttpMessage.class);

    private String version;
    private String actorId;
    private String domain;
    private HttpRequest httpRequest;
    private String action;

    private ObjectMapper objectMapper;

    @JsonCreator
    protected ApiHttpMessage(@JsonProperty("httpRequest") HttpRequest httpRequest,
                             @JsonProperty("version") String version,
                             @JsonProperty("domain") String domain,
                             @JsonProperty("actorId") String actorId,
                             @JsonProperty("action") String action) {
        this.httpRequest = httpRequest;
        this.actorId = actorId;
        this.domain = domain;
        this.version = version;
        this.action = action;

        // init object mapper
        objectMapper = new ObjectMapper();
        //objectMapper.setAnnotationIntrospector(new JaxbAnnotationIntrospector());
        objectMapper.setSerializationInclusion(Inclusion.NON_NULL);
    }

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("domain")
    public String getDomain() {
        return domain;
    }

    @JsonProperty("actorId")
    public String getActorId() {
        return actorId;
    }

    @JsonProperty("action")
    public String getAction() {
        return action;
    }

    @JsonProperty("httpRequest")
    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    @JsonIgnore
    public JsonNode getContent() {
        return getContent(JsonNode.class);
    }

    @JsonIgnore
    public HttpMethod getMethod() {
        return HttpMethod.valueOf(httpRequest.getMethod());
    }

    @JsonIgnore
    public <T> T getContent(Class<T> clazz) {
        if (httpRequest.getContent() == null) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("No content. Content type[%s]", httpRequest.getContentType()));
            }
            return null;
        }
        if ("application/json".equalsIgnoreCase(httpRequest.getContentType())) {
            try {
                return (T) objectMapper.readValue(httpRequest.getContent(), clazz);
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        } else {
            log.warn(String.format("Not json content[%s]", httpRequest.getContentType()));
        }
        return (T) null;
    }

    @JsonIgnore
    public boolean hasContent() {
        if (httpRequest.getContent() == null || httpRequest.getContent().length == 0) {
            return false;
        }
        return true;
    }

    @JsonIgnore
    public boolean hasJsonContentType() {
        return "application/json".equalsIgnoreCase(httpRequest.getContentType());
    }

    public static ApiHttpMessage parse(HttpRequest request) {

        String url = request.getUrl();
        if (url.startsWith("/")) url = url.substring(1);
        String[] tokens = url.split("/");
        if (tokens.length < 3) {
            log.warn(String.format("Invalid url path[%s]. Not of type '/api/*/users/'", url));
            return null;
        }

        String uid = (tokens.length > 3 ? tokens[3] : null);
        String action = (tokens.length > 4 ? tokens[4] : null);
        return new ApiHttpMessage(request, tokens[1], tokens[2], uid, action);
    }

    public HttpResponse toHttpResponse(HttpResponseStatus status) {
        return toHttpResponse(status, status.getReasonPhrase(), null);
    }

    public HttpResponse toHttpResponse(HttpResponseStatus status, Object jsonObject) {
        return toHttpResponse(status, status.getReasonPhrase(), jsonObject);
    }

    private HttpResponse toHttpResponse(HttpResponseStatus status, String content, Object jsonObject) {

        // headers
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        headers.put(HttpHeaders.Names.CONTENT_TYPE, Arrays.asList("application/json"));

        // response
        byte[] response = content.getBytes(Charset.forName("UTF-8"));
        if (jsonObject != null) {
            try {
                response = objectMapper.writeValueAsBytes(jsonObject);
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }
        return new HttpResponse(status.getCode(), headers, response);
    }
}