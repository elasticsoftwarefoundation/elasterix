package org.elasticsoftware.elasterix.server.messages;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @author Leonard Wolters
 */
public class TimeoutMessage {
	private String userAgentClient;
	
	private long timeoutInMilliseconds;
	
	@JsonCreator
	public TimeoutMessage() {
	}
	
	@JsonProperty("userAgentClient")
	public void setUserAgentClient(String userAgentClient) {
		this.userAgentClient = userAgentClient;
	}
	
	@JsonProperty("timeoutInMilliseconds")
	public void setTimeoutInMilliSeconds(long timeoutInMilliseconds) {
		this.timeoutInMilliseconds = timeoutInMilliseconds;
	}
	
	public String getUserAgentClient() {
		return userAgentClient;
	}

	public long getTimeoutInMilliseconds() {
		return timeoutInMilliseconds;
	}
	
	public boolean isExpired(long lastUpdate) {
		return (lastUpdate + timeoutInMilliseconds < System.currentTimeMillis());
	}
}
