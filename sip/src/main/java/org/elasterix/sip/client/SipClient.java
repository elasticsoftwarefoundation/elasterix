package org.elasterix.sip.client;

import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

/**
 * A 'to-the-bone' simple SIP client based on netty 
 * that sends SIP messages to any SIP Server
 * 
 * @author Leonard Wolters
 */
public class SipClient {
	//private int port = 5060;

	private ChannelFactory channelFactory;

	public void start() {
		if(channelFactory == null) {
			channelFactory = new NioServerSocketChannelFactory(
					Executors.newCachedThreadPool(),
					Executors.newCachedThreadPool());
		}
		ClientBootstrap bootstrap = new ClientBootstrap(channelFactory);
		bootstrap.setPipelineFactory(new SipClientPipelineFactory(false, false));
	}	

	public void setChannelFactory(ChannelFactory channelFactory) {
		this.channelFactory = channelFactory;
	}
}
