package org.elasterix.sip;

import static org.jboss.netty.channel.Channels.pipeline;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.apache.log4j.Logger;
import org.elasterix.sip.codec.SipRequestDecoder;
import org.elasterix.sip.codec.SipResponseEncoder;
import org.elasterix.sip.ssl.DummySecureSslContextFactory;
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
public class SipServerPipelineFactory implements ChannelPipelineFactory {
	private static final Logger log = Logger.getLogger(SipServerPipelineFactory.class);

    private final SipServerHandler handler;
	private boolean ssl = false;
	private boolean compression = true;
	private SSLContext sslContext;

    public SipServerPipelineFactory(SipServerHandler handler) {
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

		pipeline.addLast("decoder", new SipRequestDecoder());
		pipeline.addLast("encoder", new SipResponseEncoder());		
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
