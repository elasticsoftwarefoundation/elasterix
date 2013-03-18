package org.elasterix.sip.codec;

import static org.jboss.netty.handler.codec.http.HttpConstants.CR;
import static org.jboss.netty.handler.codec.http.HttpConstants.LF;
import static org.jboss.netty.handler.codec.http.HttpConstants.SP;

import org.jboss.netty.buffer.ChannelBuffer;

/**
 * Encodes an {@link SipRequest} into a {@link ChannelBuffer}.
 * 
 * @author Leonard Wolters
 */
public class SipRequestEncoder extends SipMessageEncoder {
	private static final char SLASH = '/';

	@Override
	protected void encodeInitialLine(ChannelBuffer buf, SipMessage message)
	throws Exception {
		SipRequest request = (SipRequest) message;
		buf.writeBytes(request.getMethod().toString().getBytes("UTF-8"));
		buf.writeByte(SP);

		// Add / as absolute path if no is present.
		// See http://tools.ietf.org/html/rfc2616#section-5.1.2
		String uri = request.getUri();
		int start = uri.indexOf("://");
		if (start != -1) {
			int startIndex = start + 3;
			if (uri.lastIndexOf(SLASH) <= startIndex) {
				uri += SLASH;
			}
		}

		buf.writeBytes(uri.getBytes("UTF-8"));
		buf.writeByte(SP);
		buf.writeBytes(request.getProtocolVersion().toString().getBytes("UTF-8"));
		buf.writeByte(CR);
		buf.writeByte(LF);
	}
}
