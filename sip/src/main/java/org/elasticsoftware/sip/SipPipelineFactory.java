package org.elasticsoftware.sip;

import static org.jboss.netty.channel.Channels.pipeline;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.apache.log4j.Logger;
import org.elasticsoftware.sip.codec.SipMessageDecoder;
import org.elasticsoftware.sip.codec.SipMessageEncoder;
import org.elasticsoftware.sip.ssl.DummySecureSslContextFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpContentCompressor;
import org.jboss.netty.handler.ssl.SslHandler;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;

/**
 * Standard SIP pipeline factory<br>
 * 
 * @author Leonard Wolters
 */
public class SipPipelineFactory implements ChannelPipelineFactory {
	private static final Logger log = Logger.getLogger(SipPipelineFactory.class);

    private final SipServerHandler handler;
	private boolean ssl = false;
	private boolean compression = true;
	private SSLContext sslContext;

    public SipPipelineFactory(SipServerHandler handler) {
        this.handler = handler;
    }

    @Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = pipeline();
		if(log.isDebugEnabled()) {
			log.debug(String.format("Create pipeline(ssl: %b)", ssl));
		}
		if(ssl) {
			SSLEngine engine = getSslContext().createSSLEngine();
			engine.setUseClientMode(false);
			pipeline.addLast("ssl", new SslHandler(engine));
		}

		pipeline.addLast("decoder", new SipMessageDecoder());
		pipeline.addLast("encoder", new SipMessageEncoder());		
		if(compression) {
			pipeline.addLast("deflater", new HttpContentCompressor());
		}
		pipeline.addLast("handler", handler);
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
