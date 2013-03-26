package org.elasterix.sip.codec;

import org.springframework.util.StringUtils;

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
	
	public static SipVersion lookup(String value) {
		return lookup(value, false);
	}
	public static SipVersion lookup(String value, boolean throwException) {
		if(!StringUtils.hasLength(value)) {
			return null;
		}
		// SIP/1.0 or SIP/2.0
		int idx = value.indexOf('/');
		if(idx == -1 || idx == value.length()) {
			if(throwException) {
				throw new IllegalArgumentException(String.format("Invalid value[%s]",value));
			} 
			return null;
		}
		String protocol = value.substring(0, idx);
		if("SIP".equalsIgnoreCase(protocol)) {
			value = value.substring(idx+1);
			int majorVersion = -1;
			int minorVersion = -1;
			int idx2 = value.indexOf('.');
			if(idx2 != -1) {
				majorVersion = Integer.parseInt(value.substring(0, idx2));
				minorVersion = Integer.parseInt(value.substring(idx2 + 1));
			} else {
				// no minor version present
				majorVersion = Integer.parseInt(value);
			}
			// OK, only check major version for now...
			if(majorVersion == 1) {
				return SIP_1_0;
			} else if(majorVersion == 2) {
				return SIP_2_0;
			}
			if(throwException) {
				throw new IllegalArgumentException(String.format("Unsupported SIP version[%d]",
						majorVersion));
			}
		}
		if(throwException) {
			throw new IllegalArgumentException(String.format("Protocol[%s] not supported",
					protocol));
		}
		return null;
	}
}
