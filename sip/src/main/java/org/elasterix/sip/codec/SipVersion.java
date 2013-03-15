package org.elasterix.sip.codec;

import org.jboss.netty.handler.codec.http.HttpVersion;

/**
 * The version of SIP protocol
 * 
 * @author Leonard Wolters
 */
public class SipVersion extends HttpVersion {
	public static final SipVersion SIP_1_0 = new SipVersion("SIP", 1, 0);
	public static final SipVersion SIP_2_0 = new SipVersion("SIP", 2, 0);
	
	protected SipVersion(String protocolName, int majorVersion, int minorVersion) {
		super(protocolName, majorVersion, minorVersion, false);
	}
}
