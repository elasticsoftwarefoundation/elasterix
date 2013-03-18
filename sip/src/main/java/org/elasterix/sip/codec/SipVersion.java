package org.elasterix.sip.codec;

/**
 * The version of SIP protocol
 * 
 * @author Leonard Wolters
 */
public enum SipVersion {
	SIP_1_0("SIP", 1, 0, false),
	SIP_2_0("SIP", 2, 0, true);
	
	private final String protocolName;
    private final int majorVersion;
    private final int minorVersion;
    private final boolean keepAliveDefault;
	private SipVersion(String protocolName, int majorVersion, int minorVersion,
			boolean keepAliveDefault) {
		this.protocolName = protocolName;
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
		this.keepAliveDefault = keepAliveDefault;
	}
	
	public boolean isKeepAliveDefault() {
		return keepAliveDefault;
	}
}
