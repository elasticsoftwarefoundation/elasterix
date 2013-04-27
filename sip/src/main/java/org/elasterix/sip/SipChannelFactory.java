package org.elasterix.sip;

import org.elasterix.sip.codec.SipMessage;
import org.jboss.netty.channel.Channel;

/**
 * @author Leonard Wolters
 */
public interface SipChannelFactory {
	
	/**
	 * @param message
	 * @param channel
	 */
	void setChannel(SipMessage message, Channel channel);
	
	/**
	 * @param message
	 * @return
	 */
	Channel getChannel(SipMessage message);
}
