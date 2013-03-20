package org.elasterix.sip.codec;

import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelUpstreamHandler;

/**
 * A combination of {@link SipRequestDecoder} and {@link SipResponseEncoder}
 * which enables easier server side SIP implementation.
 * @see SipClientCodec
 *
 * @author Leonard Wolters
 */
public class SipServerCodec implements ChannelUpstreamHandler,
        ChannelDownstreamHandler {

    private final SipRequestDecoder decoder;
    private final SipResponseEncoder encoder = new SipResponseEncoder();

    /**
     * Creates a new instance with the default decoder options
     * ({@code maxInitialLineLength (4096}}, {@code maxHeaderSize (8192)}).
     */
    public SipServerCodec() {
    	this(4096, 8192);
    }
    
    /**
     * Creates a new instance with the default decoder options
     * ({@code maxInitialLineLength (4096}}, {@code maxHeaderSize (8192)}).
     */
    public SipServerCodec(int maxInitialLineLength, int maxHeaderSize) {
        decoder = new SipRequestDecoder(maxInitialLineLength, maxHeaderSize);
    }

    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e)
            throws Exception {
        decoder.handleUpstream(ctx, e);
    }

    public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e)
            throws Exception {
        encoder.handleDownstream(ctx, e);
    }
}
