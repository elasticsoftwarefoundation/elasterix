package org.elasterix.sip.codec;

/**
 * The request method of SIP 
 * 
 * @author Leonard Wolters
 */
public enum SipMethod {
    /**
     * The OPTIONS method represents a request for information about the communication options
     * available on the request/response chain identified by the Request-URI. This method allows
     * the client to determine the options and/or requirements associated with a resource, or the
     * capabilities of a server, without implying a resource action or initiating a resource
     * retrieval.
     */
    OPTIONS("OPTIONS"), 

    /**
     * The REGISTER method 
     */
    REGISTER("REGISTER"),

    /**
     * The INVITE method 
     */
    INVITE("INVITE");

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
