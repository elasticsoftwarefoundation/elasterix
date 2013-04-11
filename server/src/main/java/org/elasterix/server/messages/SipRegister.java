package org.elasterix.server.messages;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.elasterix.sip.codec.SipHeader;

import java.util.List;
import java.util.Map;

/**
 * @author Leonard Wolters
 * @author Joost van de Wijgerd
 */
public final class SipRegister extends SipMessage {
    private final String uri;

    public SipRegister(String uri, Map<String,List<String>> headers) {
        this(uri,headers,null);
    }

    @JsonCreator
    public SipRegister(@JsonProperty("uri") String uri,
                       @JsonProperty("headers") Map<String,List<String>> headers,
                       @JsonProperty("content") byte[] content) {
        super(headers, content);
        this.uri = uri;
    }

    @JsonProperty("uri")
    public String getUri() {
        return uri;
    }

    @JsonIgnore
    public String getUser() {
        return getHeader(SipHeader.TO.getName());
    }
}
