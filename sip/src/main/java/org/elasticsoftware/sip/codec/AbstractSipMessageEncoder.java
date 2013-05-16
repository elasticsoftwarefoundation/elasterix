package org.elasticsoftware.sip.codec;

import static org.jboss.netty.buffer.ChannelBuffers.dynamicBuffer;
import static org.jboss.netty.buffer.ChannelBuffers.wrappedBuffer;
import static org.jboss.netty.handler.codec.http.HttpConstants.COLON;
import static org.jboss.netty.handler.codec.http.HttpConstants.CR;
import static org.jboss.netty.handler.codec.http.HttpConstants.LF;
import static org.jboss.netty.handler.codec.http.HttpConstants.SP;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;
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
public abstract class AbstractSipMessageEncoder extends OneToOneEncoder {
	private static final Logger log = Logger.getLogger(AbstractSipMessageEncoder.class);
	protected static final Charset charSet = Charset.forName("UTF-8");

	// avoid construction...
    protected AbstractSipMessageEncoder() {
    }

    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg)
    throws Exception {
    	if(log.isDebugEnabled()) log.debug("encode");
        if (msg instanceof SipMessage) {
        	SipMessage m = (SipMessage) msg;

            ChannelBuffer header = dynamicBuffer(channel.getConfig().getBufferFactory());
            encodeInitialLine(header, m);
            encodeHeaders(header, m);

            // always add a single white line between headers and content
            header.writeByte(CR);
            header.writeByte(LF);

            ChannelBuffer content = m.getContent();
            if (content != null && !content.readable()) {
            	if(log.isDebugEnabled()) log.debug("no content available");
            	// no content available
                return header;
            } else {
            	if(log.isDebugEnabled()) log.debug("content available");

				// TODO write content depending on content type
				return wrappedBuffer(header, content);
            }
        }

        // Unknown message type.
        return msg;
    }

    private static void encodeHeaders(ChannelBuffer buf, SipMessage message) {
        try {
            for (Map.Entry<String, List<String>> header : message.getHeaders().entrySet()) {
            	encodeHeader(buf, header.getKey(), header.getValue());
            }
        } catch (UnsupportedEncodingException e) {
            throw (Error) new Error().initCause(e);
        }
    }

    private static void encodeHeader(ChannelBuffer buf, String header, List<String> values)
            throws UnsupportedEncodingException {               
    	
    	if(values.size() == 0) {
        	log.warn(String.format("encodeHeader. No values found for header[%s]", header));
        	buf.writeBytes(header.getBytes(charSet));
        	buf.writeByte(COLON);
        	buf.writeByte(SP);
        	buf.writeByte(CR);
        	buf.writeByte(LF);
        	return;
    	}
    	
        //
        // see http://tools.ietf.org/html/rfc3261#section-7.3.1
        //
    	for(String s : values) {
    		buf.writeBytes(header.getBytes(charSet));
        	buf.writeByte(COLON);
        	buf.writeByte(SP);
    		buf.writeBytes(s.getBytes(charSet));
    		buf.writeByte(CR);
    	    buf.writeByte(LF);
    	}
    }

    protected abstract void encodeInitialLine(ChannelBuffer buf, SipMessage message) throws Exception;
}
