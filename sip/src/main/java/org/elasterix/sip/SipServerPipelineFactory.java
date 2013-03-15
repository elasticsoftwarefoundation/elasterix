package org.elasterix.sip;

import static org.jboss.netty.channel.Channels.pipeline;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.elasterix.sip.ssl.DummySecureSslContextFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpContentCompressor;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.ssl.SslHandler;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author leonard Wolters
 */
public class SipServerPipelineFactory implements ChannelPipelineFactory {
	
	private boolean ssl = false;
	private boolean compression = true;
	private boolean handleHttpChunks = true;
	private SSLContext sslContext;
	
	@Override
	public ChannelPipeline getPipeline() throws Exception {

		// Create a default pipeline implementation.
		ChannelPipeline pipeline = pipeline();

		// Uncomment the following line if you want HTTPS
		if(ssl) {
			SSLEngine engine = getSslContext().createSSLEngine();
			engine.setUseClientMode(false);
			pipeline.addLast("ssl", new SslHandler(engine));
		}

		pipeline.addLast("decoder", new HttpRequestDecoder());
		if(!handleHttpChunks) {
			pipeline.addLast("aggregator", new HttpChunkAggregator(1048576));
		}
		pipeline.addLast("encoder", new HttpResponseEncoder());		
		if(compression) {
			pipeline.addLast("deflater", new HttpContentCompressor());
		}
		pipeline.addLast("handler", new SipServerHandler());
		return pipeline;
	}
	
	////////////////////////////////////
	//
	//  Getters /  Setters
	//
	////////////////////////////////////	
	
	public void setSslContext(SSLContext sslContext) {
		this.sslContext = sslContext;
	}
	
	@Required
	@Value("${sip.compression}")
	public void setCompression(boolean compression) {
		this.compression = compression;
	}
	
	@Required
	@Value("${sip.handlechunks}")
	public void setHandleHttpChunks(boolean handleHttpChunks) {
		this.handleHttpChunks = handleHttpChunks;
	}

	@Required
	@Value("${sip.enabled}")
	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	protected SSLContext getSslContext() {
		if(sslContext == null) {
			sslContext = DummySecureSslContextFactory.getServerContext();			
		}
		return sslContext;
	}
}
