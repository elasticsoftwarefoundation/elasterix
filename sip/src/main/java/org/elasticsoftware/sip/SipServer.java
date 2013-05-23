package org.elasticsoftware.sip;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ServerChannelFactory;
import org.jboss.netty.channel.socket.DatagramChannelFactory;
import org.jboss.netty.channel.socket.InternetProtocolFamily;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;

/**
 * A simple SIP server based on netty
 * 
 * @author leonard Wolters
 */
public class SipServer {
	private static final Logger log = Logger.getLogger(SipServer.class);
	
	/** Default port for the SIP server */
	private int port = 5060;

	/** TCP Server channel */
	private Channel serverChannel;
    private Channel datagramChannel;

    private SipServerHandler sipServerHandler;

	private int socketBacklog = 128;
	private boolean socketReuseAddress = true;
	private int childSocketKeepAlive = 10;
	private boolean childSocketTcpNoDelay = true;
	private int childSocketReceiveBufferSize = 8192;
	private int childSocketSendBufferSize = 8192;
	private ServerChannelFactory serverChannelFactory;
    private DatagramChannelFactory datagramChannelFactory;
	private ChannelPipelineFactory channelPipelineFactory;

	@PostConstruct
	public void start() {
		if(serverChannelFactory == null) {
			serverChannelFactory = new NioServerSocketChannelFactory(
					Executors.newCachedThreadPool(),
					Executors.newCachedThreadPool());
		}
		if(channelPipelineFactory == null) {
			channelPipelineFactory = new SipPipelineFactory(sipServerHandler);
		}
		ServerBootstrap bootstrap = new ServerBootstrap(serverChannelFactory);
		bootstrap.setOption("backlog", socketBacklog);
		bootstrap.setOption("reuseAddress", socketReuseAddress);
		bootstrap.setOption("child.keepAlive", childSocketKeepAlive);
		bootstrap.setOption("child.tcpNoDelay", childSocketTcpNoDelay);
		bootstrap.setOption("child.receiveBufferSize", childSocketReceiveBufferSize);
		bootstrap.setOption("child.sendBufferSize", childSocketSendBufferSize);
		bootstrap.setPipelineFactory(channelPipelineFactory);
		serverChannel = bootstrap.bind(new InetSocketAddress(port));

        if(datagramChannelFactory == null) {
            datagramChannelFactory = new NioDatagramChannelFactory(InternetProtocolFamily.IPv4);
        }
        // udp connection
        ConnectionlessBootstrap udpBootstrap = new ConnectionlessBootstrap(datagramChannelFactory);
        // @todo: add properties
        udpBootstrap.setPipelineFactory(channelPipelineFactory);
        datagramChannel = udpBootstrap.bind(new InetSocketAddress(port));
	}
	
	@PreDestroy
    public void stop() {
        serverChannel.close();
        datagramChannel.close();
    }
	
	////////////////////////////////////
	//
	//  Getters /  Setters
	//
	////////////////////////////////////

	@Required
	@Value("${sip.port}")
	public void setPort(int port) {
		this.port = port;
	}

    @Autowired
    public void setSipServerHandler(SipServerHandler sipServerHandler) {
        this.sipServerHandler = sipServerHandler;
    }
    
    public void setServerChannelFactory(ServerChannelFactory channelFactory) {
    	this.serverChannelFactory = channelFactory;
    }

    public void setDatagramChannelFactory(DatagramChannelFactory datagramChannelFactory) {
        this.datagramChannelFactory = datagramChannelFactory;
    }

    ////////////////////////////////////
	//
	//  Main, used for testing..
	//
	////////////////////////////////////	
    public static void main(String[] args) {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 5060;
        }
        SipServer server = new SipServer();
        server.setPort(port);
        server.start();
    }	
}
