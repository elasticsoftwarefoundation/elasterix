package org.elasticsoftware.sip.codec;

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
	public static final boolean USE_NETTY_IMPLEMENTATION = false;

    private final SipRequestDecoder decoder;
    private final SipResponseEncoder encoder = new SipResponseEncoder();

    /**
     * Creates a new instance with the default decoder options
     * ({@code maxInitialLineLength (4096}}, {@code maxHeaderSize (8192)}).
     */
    public SipServerCodec() {
    	this(4096, 8192, 4096);
    }
    
    /**
     * Creates a new instance with the default decoder options
     * ({@code maxInitialLineLength (4096}}, {@code maxHeaderSize (8192)},
     * {@code maxHeaderLineLenght (4096)}).
     */
    public SipServerCodec(int maxInitialLineLength, int maxHeaderSize, int
    		maxHeaderLineLenght) {
        decoder = new SipRequestDecoder(maxInitialLineLength, maxHeaderSize,
        		maxHeaderLineLenght);
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
