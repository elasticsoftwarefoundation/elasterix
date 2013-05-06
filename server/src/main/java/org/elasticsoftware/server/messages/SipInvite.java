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
	public SipInvite(SipRequest request) {
		super(request);
	}
	
	@JsonCreator
	public SipInvite(@JsonProperty("uri") String uri,
			@JsonProperty("version") String version,
			@JsonProperty("method") String method,
			@JsonProperty("headers") Map<String, List<String>> headers,
			@JsonProperty("content") byte[] content) {
		super(uri, version, method, headers, content);
	}
}
