package org.elasterix.sip.codec;

import static org.jboss.netty.buffer.ChannelBuffers.dynamicBuffer;
import static org.jboss.netty.buffer.ChannelBuffers.wrappedBuffer;
import static org.jboss.netty.handler.codec.http.HttpConstants.COLON;
import static org.jboss.netty.handler.codec.http.HttpConstants.CR;
import static org.jboss.netty.handler.codec.http.HttpConstants.LF;
import static org.jboss.netty.handler.codec.http.HttpConstants.SP;

import java.io.UnsupportedEncodingException;
import java.util.Map;

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
	
	// avoid construction...
    protected SipMessageEncoder() {
    }

    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) 
    throws Exception {
        if (msg instanceof SipMessage) {
        	
        	SipMessage m = (SipMessage) msg;

            ChannelBuffer header = dynamicBuffer(channel.getConfig().getBufferFactory());
            encodeInitialLine(header, m);
            encodeHeaders(header, m);
            header.writeByte(CR);
            header.writeByte(LF);

            ChannelBuffer content = m.getContent();
            if (!content.readable()) {
                return header; // no content
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
        buf.writeBytes(header.getBytes("ASCII"));
        buf.writeByte(COLON);
        buf.writeByte(SP);
        buf.writeBytes(value.getBytes("ASCII"));
        buf.writeByte(CR);
        buf.writeByte(LF);
    }

    protected abstract void encodeInitialLine(ChannelBuffer buf, SipMessage message) throws Exception;
}
