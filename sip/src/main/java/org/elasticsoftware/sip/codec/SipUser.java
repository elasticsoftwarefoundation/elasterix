package org.elasticsoftware.sip.codec;

import org.springframework.util.StringUtils;

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
    private int port = 0;
    
    public SipUser(String displayName, String username, String domain, int port) {
    	this.displayName = displayName;
    	this.username = username;
    	this.domain = domain;
    	this.port = port;
    }

    public SipUser(String value) {
        int idx = value.indexOf("<");
        if(idx == -1) {
        	// give value must be a 'uri' e.g. sip:1234@localhost:5060
        } else {
            displayName = value.substring(0, idx).replace('\"', ' ').trim();
            value = value.substring(idx + 1, value.indexOf('>'));
        }
        // remaining value must be a 'uri', e.g. 
        // sip:124@sip.outerteams.com:5060
        // sip:124@62.163.143.30:60236;transport=UDP;rinstance=e6768ab86fdcf0b4

        idx = value.indexOf('@');
        if (idx != -1) {
            username = value.substring(0, idx);
            domain = value.substring(idx + 1);
        }

        // username => sip:124
        idx = username.indexOf(':');
        if (idx != -1) {
            username = username.substring(idx + 1);
        }

        // domain
        idx = domain.indexOf(':');
        if (idx != -1) {
            // remeber that port might contain 'other' suffices, e.g. transport or rinstance
            String sPort = domain.substring(idx + 1);
            int idx2 = sPort.indexOf(';');
            if (idx2 != -1) {
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
    
    public String fromHeader(String tag) {
    	// From: "Unknown" <sip:Unknown@217.195.124.187>;tag=as406c5327
    	return String.format("\"%s\" <sip:%s@%s%s%s", getDisplayName(),
    			getUsername(), getDomain(), getPort() < 0 ? "" : ":" + Integer.toString(port), 
    			StringUtils.hasLength(tag) ? ";tag=" + tag : "");
    }
    public String toHeader(String transport, String rinstance, boolean appendChevrons) {
    	// To: <sip:124@62.163.143.30:63703;transport=UDP;rinstance=e849fb1679215146>
    	return String.format("%ssip:%s@%s%s%s%s%s", 
    			appendChevrons ? "<" : "", getUsername(), getDomain(), 
    			getPort() < 0 ? "" : ":" + Integer.toString(port), 
    			StringUtils.hasLength(rinstance) ? ";rinstance=" + rinstance : "",
    			StringUtils.hasLength(transport) ? ";transport=" + transport : "",
    			appendChevrons ? ">" : "");
    }

    @Override
    public String toString() {
        return String.format("User[%s, %s, %s, %d]", displayName, username, domain, port);
    }
}
