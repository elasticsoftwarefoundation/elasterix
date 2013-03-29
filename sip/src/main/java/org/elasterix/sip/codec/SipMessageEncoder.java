package org.elasterix.sip.codec;

import static org.jboss.netty.buffer.ChannelBuffers.dynamicBuffer;
import static org.jboss.netty.buffer.ChannelBuffers.wrappedBuffer;
import static org.jboss.netty.handler.codec.http.HttpConstants.COLON;
import static org.jboss.netty.handler.codec.http.HttpConstants.CR;
import static org.jboss.netty.handler.codec.http.HttpConstants.LF;
import static org.jboss.netty.handler.codec.http.HttpConstants.SP;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

/**
 * Encodes an {@link SipMessage} into a {@link ChannelBuffer}.
 *
 * @author Leonard Wolters
 */
public abstract class SipMessageEncoder extends OneToOneEncoder {
	private static final Logger log = Logger.getLogger(SipMessageEncoder.class);
	protected static final Charset charSet = Charset.forName("UTF-8");

	// avoid construction...
    protected SipMessageEncoder() {
    }

    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg)
    throws Exception {
    	log.debug("encode");
        if (msg instanceof SipMessage) {
        	SipMessage m = (SipMessage) msg;

            ChannelBuffer header = dynamicBuffer(channel.getConfig().getBufferFactory());
            encodeInitialLine(header, m);
            encodeHeaders(header, m);
            // TODO write SDP content....

            // always add a single white line between headers and content
            header.writeByte(CR);
            header.writeByte(LF);

            ChannelBuffer content = m.getContent();
            if (!content.readable()) {
            	// no content available
                return header;
            } else {
                return wrappedBuffer(header, content);
            }
        }

        // Unknown message type.
        return msg;
    }

    private static void encodeHeaders(ChannelBuffer buf, SipMessage message) {
        try {
            for (Map.Entry<String, String> h: message.getHeaders()) {
                encodeHeader(buf, h.getKey(), h.getValue());
            }
        } catch (UnsupportedEncodingException e) {
            throw (Error) new Error().initCause(e);
        }
    }

    private static void encodeHeader(ChannelBuffer buf, String header, String value)
            throws UnsupportedEncodingException {
    	if(log.isDebugEnabled()) log.debug(String.format("encodeHeader. [%s] --> [%s]", header, value));
        buf.writeBytes(header.getBytes(charSet));
        buf.writeByte(COLON);
        buf.writeByte(SP);
        buf.writeBytes(value.getBytes(charSet));
        buf.writeByte(CR);
        buf.writeByte(LF);
    }

    protected abstract void encodeInitialLine(ChannelBuffer buf, SipMessage message) throws Exception;
}
