package org.elasterix.sip.codec;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Enumeration for all available SIP Headers
 * 
 * @author Leonard Wolters
 */
public enum SipHeader {
	ACCEPT("Accept"),
	ACCEPT_ENCODING("Accept-Encoding"),
	ACCEPT_LANGUAGE("Accept-Language"),
	ALERT_INFO("Alert-Info"),
	ALLOW("Allow"),
	AUTHENTICATION_INFO("Authentication-info"),
	AUTHORIZATION("Authorization"),
	CALL_ID("Call-ID"),
	CALL_INFO("Call-Info"),
	CONTACT("Contact"),
	CONTENT_DISPOSITION("Content-Disposition"),
	CONTENT_ENCODING("Content-Encoding"),
	CONTENT_LANGUAGE("Content-Language"),
	CONTENT_LENGTH("Content-Length"),
	CONTENT_TYPE("Content-Type"),
	CSEQ("CSeq"),
	ERROR_INFO("Error-Info"),
	EXPIRES("Expires"),
	FROM("From"),
	IN_REPLY_TO("In-Reply-To"),
	MAX_FORWARDS("Max-Forwards"),
	MIN_EXPIRES("Min-Expires"),
	MIME_VERSION("Mime-Version"),
	ORGANIZATION("Organization"),
	PRIORITY("Priority"),
	PROXY_AUTHENTICATE("Proxy-Authenticate"),
	PROXY_AUTHORIZATION("Proxy-Authorization"),
	PROXY_REQUIRE("Proxy-Require"),
	RECORD_ROUTE("Record-Route"),
	REPLY_TO("Reply-To"),
	RETRY_AFTER("Reply-After"),
	ROUTE("Route"),
	SERVER("Server"),
	SUBJECT("Subject"),
	SUPPORTED("Supported"),
	TIMESTAMP("Timestamp"),
	TO("To"),
	UNSUPPORTED("Unsupported"),
	USER_AGENT("User-Agent"),
	VIA("Via"),
	WARNING("Warning"),
	WWW_AUTHENTICATE("WWW-Authenticate");

	private static final ConcurrentHashMap<String, SipHeader> cached = new ConcurrentHashMap<String, SipHeader>();
	
	private final String name;
	private SipHeader(String name) {
		this.name= name;
	}
	
	public String getName() {
		return name;
	}
	
	public static SipHeader lookup(String value) {
		// do we need to init
		if(cached.size() == 0) {
			for(SipHeader h : values()) {
				cached.putIfAbsent(h.name, h);
			}
		}
		return cached.get(value);
	}
}
