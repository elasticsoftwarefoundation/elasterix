package org.elasticsoftware.sip;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;

/**
 * Dummy channel event
 * 
 * @author Leonard Wolters
 */
public class DummyChannelEvent implements ChannelEvent {

	@Override
	public Channel getChannel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ChannelFuture getFuture() {
		// TODO Auto-generated method stub
		return null;
	}

}
