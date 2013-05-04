package org.elasticsoftware.sip.codec;

import static org.jboss.netty.handler.codec.http.HttpConstants.CR;
import static org.jboss.netty.handler.codec.http.HttpConstants.LF;
import static org.jboss.netty.handler.codec.http.HttpConstants.SP;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * @author Leonard Wolters
 */
public class SipResponseEncoder extends SipMessageEncoder {

	@Override
	protected void encodeInitialLine(ChannelBuffer buf, SipMessage message)
	throws Exception {
		// example:
		// SIP/2.0 200 OK
        buf.writeBytes(message.getProtocolVersion().toString().getBytes(charSet));
        buf.writeByte(SP);
        buf.writeBytes(String.valueOf(message.getResponseStatus().getCode()).getBytes(charSet));
        buf.writeByte(SP);
        buf.writeBytes(String.valueOf(message.getResponseStatus().getReasonPhrase()).getBytes(charSet));
        buf.writeByte(CR);
        buf.writeByte(LF);
	}
}
