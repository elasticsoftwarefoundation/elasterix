package org.elasterix.server.messages;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.elasterix.sip.codec.SipHeader;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Leonard Wolters
 */
public abstract class SipMessage {
    private final Map<String,List<String>> headers;
    protected final byte[] content;

    protected SipMessage(Map<String, List<String>> headers, byte[] content) {
        this.headers = headers;
        this.content = content;
    }

    @JsonProperty("headers")
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public final String getHeader(String name) {
        List<String> headerValues = headers.get(name);
        return headerValues == null || headerValues.isEmpty() ? null : headerValues.get(0);
    }

    public final List<String> getHeaders(String name) {
        List<String> headerValues = headers.get(name);
        return headerValues == null || headerValues.isEmpty() ? Collections.<String>emptyList() : headerValues;
    }

    @JsonIgnore
    public String getContentType() {
        return getHeader(SipHeader.CONTENT_TYPE.getName());
    }

    @JsonProperty("content")
    public byte[] getContent() {
        return content;
    }
}
