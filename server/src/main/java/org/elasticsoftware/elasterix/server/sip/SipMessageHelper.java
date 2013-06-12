package org.elasticsoftware.elasterix.server.sip;

import java.util.UUID;

import org.elasticsoftware.elasterix.server.ServerConfig;
import org.elasticsoftware.elasterix.server.messages.SipRequestMessage;
import org.elasticsoftware.elasterix.server.messages.SipResponseMessage;
import org.elasticsoftware.sip.codec.SipHeader;
import org.elasticsoftware.sip.codec.SipMethod;
import org.elasticsoftware.sip.codec.SipUser;
import org.elasticsoftware.sip.codec.SipVersion;

/**
 * @author Leonard Wolters
 */
public class SipMessageHelper {
	
	public static SipUser createSipUser() {
		return new SipUser(ServerConfig.getUsername(), ServerConfig.getUsername(),
				ServerConfig.getIPAddress(), ServerConfig.getSipPort());
	}

	/**
	 * Create a SIP Options Request
	 * 
	 * @return
	 */
	public static SipRequestMessage createOptions(SipUser to, SipVersion version, String address) {
		return constructRequest(SipMethod.OPTIONS, null, to, version, "102 OPTIONS", address);
	}

	/**
	 * Create a SIP Invite Request
	 * 
	 * @return
	 */
	public static SipRequestMessage createInvite(SipUser from, SipUser to, SipVersion version,
			String address) {
		return constructRequest(SipMethod.INVITE, from, to, version, "1 INVITE", address);
	}
	
	protected static SipRequestMessage constructRequest(SipMethod method, SipUser from, SipUser to, 
			SipVersion version, String cSeq, String address) {
		
		if(from == null) {
			from = createSipUser();
		}
		
		String rinstance = "6f8dc969b62d1466";
		
		// uri should contain the IP address of the UAC
		String uri = String.format("sip:%s@%s", to.getUsername(), address);
		uri += ";rinstance=" + rinstance;
		uri += ";transport=" + ServerConfig.getProtocol();
		
		SipRequestMessage message = new SipRequestMessage(method.name(), uri, version.toString(), null, null, false);
		message.addHeader(SipHeader.ALLOW, ServerConfig.getAllow());
        message.addHeader(SipHeader.CALL_ID, UUID.randomUUID().toString());
        message.addHeader(SipHeader.CONTACT, from.toHeader(null, null, true));
        message.addHeader(SipHeader.CONTENT_LENGTH, "0");
        message.addHeader(SipHeader.CSEQ, cSeq);
        message.addHeader(SipHeader.DATE, ServerConfig.getDateNow());
        message.addHeader(SipHeader.FROM, from.fromHeader(null));
        message.addHeader(SipHeader.MAX_FORWARDS, "70");
        message.addHeader(SipHeader.SUPPORTED, ServerConfig.getSupported());
        message.addHeader(SipHeader.TO, to.toHeader(rinstance, ServerConfig.getProtocol(), true));
        message.addHeader(SipHeader.USER_AGENT, ServerConfig.getUserAgent());
        message.setHeader(SipHeader.VIA, String.format("%s/%s %s:%d;branch=z9hG4bK326c96f4",
				message.getVersion().toString(), ServerConfig.getProtocol(), ServerConfig.getIPAddress(), 
				ServerConfig.getSipPort()));
        return message;
	}
	
	public static SipRequestMessage checkRequest(SipRequestMessage message) {
		
		//
		// set required headers
		//
		message.setHeader(SipHeader.CALL_ID, ServerConfig.getNewCallId());
		message.setHeader(SipHeader.MAX_FORWARDS, Integer.toString(ServerConfig.getMaxForwards()));
		
		//
		// Add header
		//
		message.setHeader(SipHeader.VIA, String.format("%s/%s %s:%d;branch=z9hG4bK326c96f4",
				message.getVersion().toString(), ServerConfig.getProtocol(), ServerConfig.getIPAddress(), 
				ServerConfig.getSipPort()));
		
		return message;
	}
	
	public static SipResponseMessage checkResponse(SipResponseMessage message) {
		
		//
		// set required headers 		
		// 
		message.setHeader(SipHeader.ALLOW, ServerConfig.getAllow());
		message.setHeader(SipHeader.DATE, ServerConfig.getDateNow());
		message.setHeader(SipHeader.SERVER, ServerConfig.getServerName());
		message.setHeader(SipHeader.SUPPORTED, ServerConfig.getSupported());
		
		// 
		// alter existing headers
		//
		SipUser contact = message.getSipUser(SipHeader.CONTACT);
		if(contact != null) {
			message.appendHeader(SipHeader.VIA, "received", contact.getDomain());
			message.appendHeader(SipHeader.VIA, "rport", Long.toString(contact.getPort()));
		}
		
		//
		// Optionally, remove headers
		//
		message.removeHeader(SipHeader.MAX_FORWARDS);
		message.removeHeader(SipHeader.USER_AGENT);
		
		// check content
		if(message.getContent() == null || message.getContent().length == 0) {
			message.setHeader(SipHeader.CONTENT_LENGTH, 0);
		}	
		
		return message;
	}
}
