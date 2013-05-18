package org.elasticsoftware.sip.codec;

/**
 * Sip User Domain Object<br>
 * <br>
 * Parses a traditional sip user element, e.g. <br>
 * "Hans de Borst"<sip:124@sip.outerteams.com:5060>;tag=ce337d00
 * 
 * @author Leonard Wolters
 */
public class SipUser {
	private String displayName;
	private String username;
	private String domain;
	private int port;

	public SipUser(String value) {
		int idx = value.indexOf("<");
    	if(idx != -1) {
    		displayName = value.substring(0, idx).replace('\"', ' ').trim();
    		value = value.substring(idx+1, value.indexOf('>'));
    		// value => sip:124@sip.outerteams.com:5060
        }

    	idx = value.indexOf('@');
    	if(idx != -1) {
    		username = value.substring(0, idx);
    		domain = value.substring(idx+1);
    	}

    	// username => sip:124
    	idx = username.indexOf(':');
    	if(idx != -1) {
    		username = username.substring(idx+1);
    	}

    	// domain
    	idx = domain.indexOf(':');
    	if(idx != -1) {
    		// port might contain 'other' suffices
    		// <sip:124@62.163.143.30:60236;transport=UDP;rinstance=e6768ab86fdcf0b4>
    		String sPort = domain.substring(idx + 1);
    		int idx2 = sPort.indexOf(';');
    		if(idx2 != -1) {
    			sPort = sPort.substring(0, idx2);
    		}
    		port = Integer.parseInt(sPort);
    		domain = domain.substring(0, idx);
    	}
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getDomain() {
		return domain;
	}
	
	public int getPort() {
		return port;
	}
	
	@Override
	public String toString() {
		return String.format("User[%s, %s, %s, %d]", displayName, username, domain, port);
	}
}
