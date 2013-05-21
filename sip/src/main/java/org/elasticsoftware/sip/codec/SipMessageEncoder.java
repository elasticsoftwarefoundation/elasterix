package org.elasticsoftware.sip.codec;

import static org.jboss.netty.handler.codec.http.HttpConstants.CR;
import static org.jboss.netty.handler.codec.http.HttpConstants.LF;
import static org.jboss.netty.handler.codec.http.HttpConstants.SP;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.springframework.util.StringUtils;

/**
 * @author Leonard Wolters
 */
public class SipMessageEncoder extends AbstractSipMessageEncoder {
	private static final Logger log = Logger.getLogger(SipMessageEncoder.class);

	@Override
	protected void encodeInitialLine(ChannelBuffer buf, SipMessage message)
	throws Exception {
		
		// check if we need to send a SIP Request or a SIP Response
		if(message instanceof SipRequest) {
			encodeRequest(buf, (SipRequest) message);
		} else if(message instanceof SipResponse) {
			encodeResponse(buf, (SipResponse) message);
		} else {
			log.error(String.format("encodeInitialLine. Unsupported message[%s]",
					message.getClass().getSimpleName()));
			throw new UnsupportedOperationException(String.format("Unsupported message[%s]",
					message.getClass().getSimpleName()));
		}
	}
	
	/**
	 * Encodes the initial line of a SipRequest, e.g.
	 * REGISTER sip:sip.outerteams.com:5060 SIP/2.0
	 * 
	 * @param buf
	 * @param request
	 */
	private void encodeRequest(ChannelBuffer buf, SipRequest request) {
		buf.writeBytes(request.getMethod().name().getBytes(charSet));
        buf.writeByte(SP);
		buf.writeBytes(request.getUri().getBytes(charSet));
        buf.writeByte(SP);
        buf.writeBytes(request.getVersion().toString().getBytes(charSet));
        buf.writeByte(CR);
        buf.writeByte(LF);
	}

	/**
	 * Encodes the initial line of a SipResponse, e.g.
	 * SIP/2.0 200 OK
	 * 
	 * @param buf
	 * @param request
	 */
	private void encodeResponse(ChannelBuffer buf, SipResponse response) {
        buf.writeBytes(response.getVersion().toString().getBytes(charSet));
        buf.writeByte(SP);
        buf.writeBytes(String.valueOf(response.getResponseStatus().getCode()).getBytes(charSet));
        buf.writeByte(SP);
        buf.writeBytes(String.valueOf(response.getResponseStatus().getReasonPhrase()).getBytes(charSet));
        if(StringUtils.hasLength(response.getResponseStatus().getOptionalMessage())) {
            buf.writeByte(SP);
            buf.writeBytes(String.format("(%s)", response.getResponseStatus().getOptionalMessage()).getBytes(charSet));
        }
        buf.writeByte(CR);
        buf.writeByte(LF);
	}
}
