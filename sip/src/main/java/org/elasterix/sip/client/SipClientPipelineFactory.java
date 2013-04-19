package org.elasterix.sip.client;

import static org.jboss.netty.channel.Channels.pipeline;

import javax.net.ssl.SSLEngine;

import org.elasterix.sip.codec.SipClientCodec;
import org.elasterix.sip.ssl.DummySecureSslContextFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpContentDecompressor;
import org.jboss.netty.handler.ssl.SslHandler;

/**
 * @author Leonard Wolters
 */
public class SipClientPipelineFactory implements ChannelPipelineFactory {
	private final boolean ssl;
	private final boolean decompression;
	
	public SipClientPipelineFactory(boolean ssl, boolean decompression) {
		this.ssl = ssl;
		this.decompression = decompression;
	}
	
	@Override
	public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = pipeline();

        if (ssl) {
            SSLEngine engine = DummySecureSslContextFactory.getClientContext().createSSLEngine();
            engine.setUseClientMode(true);
            pipeline.addLast("ssl", new SslHandler(engine));
        }
        
        pipeline.addLast("codec", new SipClientCodec());
        if(decompression) {
        	pipeline.addLast("inflater", new HttpContentDecompressor());
        }
        StringBuilder buf = new StringBuilder();
        pipeline.addLast("handler", new SipClientHandler(buf));
        return pipeline;
    }
}
