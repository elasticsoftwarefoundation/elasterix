/*
 * Copyright 2013 Joost van de Wijgerd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.elasticsoftware.sip;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;

import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
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
	/** Initial capacity of cache */
	private int initialCacheSize = 5000;
	private SipServerHandler sipServerHandler;
    
	/** LRU Cache with a capacity of 5000 */
	private final ConcurrentMap<String, Channel> cache = 
			new ConcurrentLinkedHashMap.Builder<String, Channel>().maximumWeightedCapacity(initialCacheSize).build();

	@Override
	public void setChannel(String address, Channel channel) {
		// always reset channel (remove/add)
		if(StringUtils.hasLength(address)) {
			cache.put(address, channel);
		}
	}

	@Override
	public Channel getChannel(String address) {
		return assureOpen(cache, address);
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

	private Channel assureOpen(ConcurrentMap<String, Channel> cache, String address) {

		// check is channel is open
		Channel c = cache.get(address);
		if(c != null && c.isConnected() && c.isOpen()) {
			return c;
		}

		if(!StringUtils.hasLength(address)) {
			log.warn("assureOpen. No address given. Can't connect channel");
			return c;
		}
		
		// To: "Hans de Borst"<sip:124@sip.outerteams.com:5060>
		int idx = address.lastIndexOf('@');
		if(idx != -1) {
			address = address.substring(idx+1);
			try {
				idx = address.indexOf(":");
				String hostname = address.substring(0, idx);
				int port = Integer.parseInt(address.substring(idx + 1, address.lastIndexOf('>')));
				if(log.isDebugEnabled()) {
					log.debug(String.format("assureOpen. Connecting address[%s:%d]", hostname, port));
				}
				c = createChannel(hostname, port);
			} catch (Exception e) {
				log.warn(e.getMessage(), e);
			}
		} else {
			log.warn(String.format("assureOpen. Invalid address[%s]. Can't connect channel",
					address));			
		}
		// update cache
		cache.put(address, c);
		return c;
	}
	
	/**
	 * Creates a new channel to given host and port.<br>
	 * 
	 * @param host
	 * @param port
	 * @return
	 * @throws Exception
	 */
	private Channel createChannel(String host, int port) throws Exception {
		// Important notice; use NioClientSocketChannelFactory instead
		// of NioServerSocketChannelFactory
		ChannelFactory channelFactory = new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool());
		ClientBootstrap bootstrap = new ClientBootstrap(channelFactory);
		//bootstrap.setPipelineFactory(new SipClientPipelineFactory(false,false));
		bootstrap.setPipelineFactory(new SipPipelineFactory(sipServerHandler));
		ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
		
		// open / connect to channel
		Channel c = future.await().getChannel();
        if (!future.isSuccess()) {
        	log.warn(String.format("createChannel. Establishing connection failed[%s]", 
        			future.getCause().getMessage()));
            bootstrap.releaseExternalResources();
        }
        return c;
	}

	public void setInitialCacheSize(int initialCacheSize) {
		this.initialCacheSize = initialCacheSize;
	}

	@Required
	public void setSipServerHandler(SipServerHandler sipServerHandler) {
		this.sipServerHandler = sipServerHandler;
	}
}
