package org.elasticsoftware.server.messages;

import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.elasticsoftware.sip.codec.SipRequest;

/**
 * Tagging interface

 * @author Leonard Wolters
 */
public class SipInvite extends SipRequestMessage {
	private boolean authenticated = false;
	
	public SipInvite(SipRequest request) {
		super(request);
	}
	
	@JsonCreator
	public SipInvite(@JsonProperty("uri") String uri,
			@JsonProperty("version") String version,
			@JsonProperty("method") String method,
			@JsonProperty("headers") Map<String, List<String>> headers,
			@JsonProperty("content") byte[] content,
			@JsonProperty("authenticated") boolean authenticated) {
		super(uri, version, method, headers, content);
		this.authenticated = authenticated;
	}
	
	@JsonProperty("authenticated")
    public boolean isAuthenticated() {
        return authenticated;
    }
	
	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}
}
