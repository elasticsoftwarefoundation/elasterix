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
    public static SipMethod lookup(String name) {
    	for(SipMethod m: values()) {
    		if(m.methodName.equals(name)) {
    			return m;
    		}
    	}
    	return null;
    }
}
