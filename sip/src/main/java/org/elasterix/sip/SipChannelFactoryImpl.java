package org.elasterix.sip;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.elasterix.sip.codec.SipHeader;
import org.elasterix.sip.codec.SipMessage;
import org.jboss.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.StringUtils;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

/**
 * Keep track of users and their channel in order to quickly communicate or
 * transfer sip messages

 * @author Leonard Wolters
 */
public class SipChannelFactoryImpl implements SipChannelFactory {
	private static final Logger log = Logger.getLogger(SipChannelFactoryImpl.class);
	/** Sip Server is required to create new channels */
	private SipServer sipServer;
    
	/** LRU Cache with a capacity of 5000 */
	private final ConcurrentMap<String, Channel> cache = 
			new ConcurrentLinkedHashMap.Builder<String, Channel>().maximumWeightedCapacity(5000).build();

	@Override
	public void setChannel(SipMessage message, Channel channel) {
		// always reset channel (remove/add)
		String key = getKey(message);
		cache.remove(key);
		cache.putIfAbsent(key, channel);
	}

	@Override
	public Channel getChannel(SipMessage message) {
		String key = getKey(message);
		Channel c = cache.get(key);
		if(c == null) {
			log.info(String.format("No channel found for [%s]. Creating it", key));
			createChannel(message);
		}
		return assureOpen(c, message);
	}
	
	@PreDestroy
    public void destroy() {
		log.info(String.format("Closing [%d] channels", cache.size()));
		for(Channel c : cache.values()) {
			try {
				c.close();
			} catch (Exception e) {
				log.error(e);
			}
		}
    }

	private String getKey(SipMessage message) {
		return message.getHeaderValue(SipHeader.TO);
	}
	
	private Channel createChannel(SipMessage message) {
		try {
			return sipServer.newChannel();
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		return null;
	}

	private Channel assureOpen(Channel c, SipMessage sipMessage) {
		if(c.isConnected() && c.isOpen()) {
			return c;
		}
		String to = sipMessage.getHeaderValue(SipHeader.TO);
		if(!StringUtils.hasLength(to)) {
			log.warn("assureOpen. No TO header found in message. Can't connect channel");
			return c;
		}
		// To: "Hans de Borst"<sip:124@sip.outerteams.com:5060>
		int idx = to.lastIndexOf('@');
		if(idx != -1) {
			to = to.substring(idx+1);
			try {
				idx = to.indexOf(":");
				String hostName = to.substring(0, idx);
				int port = Integer.parseInt(to.substring(idx + 1, to.lastIndexOf('>')));
				if(log.isDebugEnabled()) {
					log.debug(String.format("assureOpen. Connecting to[%s:%d]", hostName, port));
				}
				c.connect(new InetSocketAddress(hostName, port));
			} catch (Exception e) {
				log.warn(e.getMessage(), e);
			}
		} else {
			log.warn(String.format("assureOpen. Invalid TO header[%s]. Can't connect channel",
					to));			
		}
		return c;
	}

	@Required
	public void setSipServer(SipServer sipServer) {
		this.sipServer = sipServer;
	}

}
