package org.elasterix.sip.codec;

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
		
        SipResponse response = (SipResponse) message;
        buf.writeBytes(response.getProtocolVersion().toString().getBytes("UTF-8"));
        buf.writeByte(SP);
        buf.writeBytes(String.valueOf(response.getResponseStatus().getCode()).getBytes("UTF-8"));
        buf.writeByte(SP);
        buf.writeBytes(String.valueOf(response.getResponseStatus().getReasonPhrase()).getBytes("UTF-8"));
        buf.writeByte(CR);
        buf.writeByte(LF);
	}
}
