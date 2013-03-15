package org.elasterix.sip.client;

import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

/**
 * A SIP client that sends messages to the SIP Server
 * 
 * @author Leonard Wolters
 */
public class SipClient {

	public void start() {
		// configure client
        ClientBootstrap bootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));
        bootstrap.setPipelineFactory(new SipClientPipelineFactory(false));
	}
}
