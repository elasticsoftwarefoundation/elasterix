package org.elasticsoftware.sip.codec;

import org.codehaus.jackson.annotate.JsonCreator;

/**
 * The request method of SIP<br>
 * <br>
 * For a full list, please see http://en.wikipedia.org/wiki/List_of_SIP_request_methods
 * 
 * @author Leonard Wolters
 */
public enum SipMethod {
    // See http://tools.ietf.org/html/rfc3261#section-7.1
    ACK("ACK"),
    BYE("BYE"),
    CANCEL("CANCEL"),
    INVITE("INVITE"),
    OPTIONS("OPTIONS"), 
    REGISTER("REGISTER"),
    // optional
    // http://en.wikipedia.org/wiki/List_of_SIP_request_methods
    SUBSCRIBE("SUBSCRIBE"),
    NOTIFY("NOTIFY"),
    REFER("REFER"),
    INFO("INFO"),
    MESSAGE("MESSAGE"),
    PRACK("PRACK"),
    UPDATE("UPDATE"),
    PUBLISH("PUBLISH");

    private String methodName;

    private SipMethod(String methodName) {
    	this.methodName = methodName;
    }

    @JsonCreator
    public static SipMethod lookup(String value) {
    	return lookup(value, false);
    }
    
    public static SipMethod lookup(String value, boolean throwException) {
    	
    	for(SipMethod m: values()) {
    		if(m.methodName.equalsIgnoreCase(value)) {
    			return m;
    		}
    	}
    	if(throwException) 
    		throw new IllegalArgumentException(String.format("Invalid method[%s]", value));
    	
    	return null;
    }
}
