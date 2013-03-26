package org.elasterix.sip.codec;

/**
 * The request method of SIP 
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
    REGISTER("REGISTER");

    private String methodName;
    private SipMethod(String methodName) {
    	this.methodName = methodName;
    }
    
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
