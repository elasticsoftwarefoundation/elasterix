package org.elasterix.sip.client;

import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

/**
 * A 'to-the-bone' simple SIP client based on netty 
 * that sends SIP messages to any SIP Server
 * 
 * @author Leonard Wolters
 */
public class SipClient {
	public void start() {
        ClientBootstrap bootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));
        bootstrap.setPipelineFactory(new SipClientPipelineFactory(false, false));
	}	
}
