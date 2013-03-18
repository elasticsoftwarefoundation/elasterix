package org.elasterix.sip.codec;

/**
 * The response code and its description of SIP method<br>
 * <br>
 * See http://tools.ietf.org/html/rfc3261#section-21
 * 
 * @author Leonard Wolters
 */
public enum SipResponseStatus {
	
	/** 
	 * 100 range are PROVISIONAL response codes, 
	 * see http://tools.ietf.org/html/rfc3261#section-21.1
	 */
	TRYING(100, "Trying", true),
	RINGING(180, "Ringing", true),
	CALL_FORWARDED(181, "Call Is Being Forwarded", true),
	QUEUED(182, "Queued", true),
	SESSION_PROGRESS(183, "Session In Progress", true),

    /** 200 range are SUCCESS response codes*/
    OK(200, "OK"),
	
    /** 300 range are REDIRECTION response codes*/
	MULTIPLE_CHOICES(300, "Multiple Choices"),
	MOVED_PERMANENTLY(301, "Moved Permanently"),
	MOVED_TEMPORARILY(302, "Moved Temporarily"),
	USE_PROXY(305, "Use Proxy"),
	ALTERNATIVE_SERVICE(380, "Alternative Service"),
	
	/** 400 range are FAILURE response codes*/
	BAD_REQUEST(400, "Bad Request"),
	UNAUTHORIZED(401, "Unauthorized"),
	PAYMENT_REQUIRED(402, "Payment Required"),
	FORBIDDED(403,"Forbidden"),
	NOT_FOUND(404,"Not Found"),
	METHOD_NOT_ALLOWED(405,"Method Not Allowed"),
	NOT_ACCEPTABLE(406, "Not Acceptable"),
	PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
	REQUEST_TIMEOUT(408, "Request Timeout"),
	GONE(410, "Gone"),
	REQUEST_ENTITY_TOO_LARGE(413,""),
	REQUEST_URI_TOO_LONG(414, "Request-URI Too Long"),
	UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
	UNSUPPORTED_URI_SCHEME(416, "Unsupported URI Scheme"),
	BAD_EXTENSION(420, "Bad Extension"),
	EXTENSION_REQUIRED(421, "Extension Required"),
	INTERVAL_TOO_BRIEF(423, "Interval Too Brief"),
	TEMPORARILY_UNAVAILABLE(480, "Temporarily Unavailable"),
	CALL_TRANSACTION_DOES_NOT_EXIST(481, "Call Transaction Does Not Exist"),
	LOOP_DETECTED(482, "Loop Detected"),
	TOO_MANY_HOPS(483, "Too Many Hops"),
	ADDRESS_INCOMPLETE(484, "Address Incomplete"),
	AMBIGUOUS(485, "Ambiguous"),
	BUSY_HERE(486, "Busy Here"),
	REQUEST_TERMINATED(487, "Request Terminated"),
	NOT_ACCEPTABLE_HERE(488, "Not Acceptable Here"),
	REQUEST_PENDING(491, "Request Pending"),
	UNDECIPHERABLE(493, "Undecipherable"),
	
	/** 500 range are SERVER FAILURE response codes*/
	SERVER_INTERNAL_ERROR(500, "Server Internal Error"),
	NOT_IMPLEMENTED(501, "Not Implemented"),
	BAD_GATEWAY(502, "Bad Gateway"),
	SERVICE_UNAVAILABLE(503, "Service Unavailable"),
	SERVER_TIMEOUT(504, "Server Time-Out"),
	VERSION_NOT_SUPPORTED(505, "Version Not Supported"),
	MESSAGE_TOO_LARGE(513, "Message Too Large"),
	
	/** 600 range are GLOBAL FAILURE response codes*/
	BUSY_EVERYWHERE(600, "Busy Everywhere"),
	DECLINE(603, "Decline"),
	DOES_NOT_EXIST_ANYWHERE(604, "Does Not Exist Anywhere"),
	NOT_ACCEPTABLE_SERVER(606, "Not Acceptable");

    private final int code;
    private final String reasonPhrase;
    private final boolean provisional;
    private SipResponseStatus(int code, String reasonPhrase) {
    	this(code, reasonPhrase, false);
    }
    private SipResponseStatus(int code, String reasonPhrase,
    		boolean provisional) {
        if (code < 0) {
            throw new IllegalArgumentException(
                    "code: " + code + " (expected: 0+)");
        }
        if (reasonPhrase == null) {
            throw new NullPointerException("reasonPhrase");
        }
        this.code = code;
        this.reasonPhrase = reasonPhrase;
        this.provisional = provisional;
    }

    /**
     * Returns the code of this status.
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns the reason phrase of this status.
     */
    public String getReasonPhrase() {
        return reasonPhrase;
    }
    
    public static SipResponseStatus lookup(int code, String value) {
    	for(SipResponseStatus s : values()) {
    		if(s.code == code) {
    			return s;
    		}
    	}
    	return null;
    }
}
