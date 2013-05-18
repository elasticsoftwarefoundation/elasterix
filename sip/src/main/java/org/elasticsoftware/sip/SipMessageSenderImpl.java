package org.elasticsoftware.sip;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.elasticsoftware.sip.codec.SipHeader;
import org.elasticsoftware.sip.codec.SipRequest;
import org.elasticsoftware.sip.codec.SipResponse;
import org.jboss.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Component;

/**
 * Standard implementation of <code>SipMessageSender</code>
 * Send incoming <code>SipMessage</code>'s to its belonging recipient.
 * <br>
 * <br>
 * This implementation takes automatically care of handling sockets and
 * connections, and reestablish connections to client's when required.<br> 
 * 
 * @author Leonard Wolters
 */
@Component
public class SipMessageSenderImpl implements SipMessageSender {
	private static final Logger log = Logger.getLogger(SipMessageSenderImpl.class);
	
	/** Channel Factory is required for obtaining channels used for sending messages*/
	private SipChannelFactory sipChannelFactory;
	
	@PostConstruct
	private void init() {
	}

	@Override
	public void sendRequest(SipRequest request, SipMessageCallback callback) {
		log.info(String.format("Sending Request\n%s", request));
		
		Channel c = sipChannelFactory.getChannel(request.getSipUser(SipHeader.TO));
		if(c == null) {
			log.error(String.format("sendRequest. No channel set/found."));
			return;
		}
		if(c.isConnected() && c.isOpen()) {
			c.write(request);
		} else {
			log.warn(String.format("sendRequest. Channel not connected or closed"));
		}
	}

	@Override
	public void sendResponse(SipResponse response, SipMessageCallback callback) {
		log.info(String.format("Sending Response\n%s", response));
		
		Channel c = sipChannelFactory.getChannel(response.getSipUser(SipHeader.CONTACT));
		if(c == null) {
			log.error(String.format("sendResponse. No channel set/found."));
			return;
		}
		if(c.isConnected() && c.isOpen()) {
			c.write(response);
		} else {
			log.warn(String.format("sendResponse. Channel not connected or closed"));
		}
	}

	@Required
    public void setSipChannelFactory(SipChannelFactory sipChannelFactory) {
        this.sipChannelFactory = sipChannelFactory;
    }
}
