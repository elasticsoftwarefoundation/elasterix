package org.elasterix.sip.client;

import static org.jboss.netty.channel.Channels.pipeline;

import javax.net.ssl.SSLEngine;

import org.elasterix.sip.ssl.DummySecureSslContextFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpClientCodec;
import org.jboss.netty.handler.codec.http.HttpContentDecompressor;
import org.jboss.netty.handler.ssl.SslHandler;

/**
 * @author Leonard Wolters
 */
public class SipClientPipelineFactory implements ChannelPipelineFactory {
	private final boolean ssl;
	private boolean decompression = true;
	private boolean handleHttpChunks = true;
	
	public SipClientPipelineFactory(boolean ssl) {
		this.ssl = ssl;
	}
	
	@Override
	public ChannelPipeline getPipeline() throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = pipeline();

        if (ssl) {
            SSLEngine engine = DummySecureSslContextFactory.getClientContext().createSSLEngine();
            engine.setUseClientMode(true);
            pipeline.addLast("ssl", new SslHandler(engine));
        }
        
        pipeline.addLast("codec", new HttpClientCodec());
        if(decompression) {
        	pipeline.addLast("inflater", new HttpContentDecompressor());
        }
        if(!handleHttpChunks) {
        	pipeline.addLast("aggregator", new HttpChunkAggregator(1048576));
        }
        StringBuilder buf = new StringBuilder();
        pipeline.addLast("handler", new SipClientHandler(buf));
        return pipeline;
    }
}
