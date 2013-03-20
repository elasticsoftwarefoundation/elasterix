package org.elasterix.sip.codec;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.handler.codec.PrematureChannelClosureException;

/**
 * A combination of {@link SipRequestEncoder} and {@link SipResponseDecoder}
 * which enables easier client side SIP implementation. <br>
 * <br>
 * Please refer to
 * {@link SipResponseDecoder} to learn what additional state management needs
 * to be done and why {@link SipResponseDecoder} can not handle it by itself.
 *
 * If the {@link Channel} gets closed and there are requests missing for a response
 * a {@link PrematureChannelClosureException} is thrown.
 *
 * @see SipServerCodec
 *
 * @author Leonard Wolter
 */
public class SipClientCodec implements ChannelUpstreamHandler,
        ChannelDownstreamHandler {
	private static final Logger log = Logger.getLogger(SipClientCodec.class);

    /** A queue that is used for correlating a request and a response. */
    final Queue<SipMethod> queue = new ConcurrentLinkedQueue<SipMethod>();

    /** If true, decoding stops (i.e. pass-through) */
    volatile boolean done;

    private final SipRequestEncoder encoder = new Encoder();
    private final SipResponseDecoder decoder;
    private final AtomicLong requestResponseCounter = new AtomicLong(0);

    private final boolean failOnMissingResponse;

    /**
     * Creates a new instance with the default decoder options
     * ({@code maxInitialLineLength (4096}}, {@code maxHeaderSize (8192)}).
     */
    public SipClientCodec() {
    	this(4096, 8192, true);
    }
    
    /**
     * Creates a new instance with the default decoder options
     * ({@code maxInitialLineLength (4096}}, {@code maxHeaderSize (8192)}).
     */
    public SipClientCodec(int maxInitialLineLength, int maxHeaderSize,
    		boolean failOnMissingResponse) {
        decoder = new Decoder(maxInitialLineLength, maxHeaderSize);
        this.failOnMissingResponse = failOnMissingResponse;
    }

    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e)
            throws Exception {
        decoder.handleUpstream(ctx, e);
    }

    public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e)
            throws Exception {
        encoder.handleDownstream(ctx, e);
    }

    private final class Encoder extends SipRequestEncoder {
        Encoder() {
        }

        @Override
        protected Object encode(ChannelHandlerContext ctx, Channel channel,
                Object msg) throws Exception {
            if (msg instanceof SipRequest && !done) {
                queue.offer(((SipRequest) msg).getMethod());
            }

            Object obj =  super.encode(ctx, channel, msg);
            if (failOnMissingResponse) {
                if (msg instanceof SipRequest) {
                    requestResponseCounter.incrementAndGet();
                }
            }
            return obj;
        }
    }

    private final class Decoder extends SipResponseDecoder {

        Decoder(int maxInitialLineLength, int maxHeaderSize) {
            super(maxInitialLineLength, maxHeaderSize);
        }

        @Override
        protected Object decode(ChannelHandlerContext ctx, Channel channel,
                ChannelBuffer buffer, State state) throws Exception {
            if (done) {
                return buffer.readBytes(actualReadableBytes());
            } else {
                Object msg = super.decode(ctx, channel, buffer, state);
                if (failOnMissingResponse) {
                    decrement(msg);
                }
                return msg;
            }
        }

        private void decrement(Object msg) {
            if (msg == null) {
                return;
            }

            // check if its a SipMessage 
            if (msg instanceof SipMessage) {
                requestResponseCounter.decrementAndGet();
            } else if (msg instanceof Object[]) {
                // we just decrement it here as we only use this if the end of the chunk is reached
                // It would be more safe to check all the objects in the array but would also be slower
                requestResponseCounter.decrementAndGet();
            }
        }

        @Override
        public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            super.channelClosed(ctx, e);
            if (failOnMissingResponse) {
                long missingResponses = requestResponseCounter.get();
                if (missingResponses > 0) {
                    throw new PrematureChannelClosureException(
                            "Channel closed but still missing " + missingResponses + " response(s)");
                }
            }
        }
    }
}
