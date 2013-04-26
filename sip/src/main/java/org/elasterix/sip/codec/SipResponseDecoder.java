package org.elasterix.sip.codec;

import org.elasterix.sip.codec.impl.SipResponseImpl;
import org.elasterix.sip.codec.netty.SipResponseNetty;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;

/**
 * Decodes {@link ChannelBuffer}s into {@link SipResponse}s.
 *
 * <h3>Parameters that prevents excessive memory consumption</h3>
 * <table border="1">
 * <tr>
 * <th>Name</th><th>Meaning</th>
 * </tr>
 * <tr>
 * <td>{@code maxInitialLineLength}</td>
 * <td>The maximum length of the initial line (e.g. {@code "HTTP/1.0 200 OK"})
 *     If the length of the initial line exceeds this value, a
 *     {@link TooLongFrameException} will be raised.</td>
 * </tr>
 * <tr>
 * <td>{@code maxHeaderSize}</td>
 * <td>The maximum length of all headers.  If the sum of the length of each
 *     header exceeds this value, a {@link TooLongFrameException} will be raised.</td>
 * </tr>
 * </table>
 * 
 * @author Leonard Wolters
 */
public class SipResponseDecoder extends SipMessageDecoder {
	
	public SipResponseDecoder(int maxInitialLineLength, int maxHeaderSize,
			int maxHeaderLineLenght) {
		super(maxInitialLineLength, maxHeaderSize, maxHeaderLineLenght);
	}

	@Override
	protected boolean isDecodingRequest() {
		return false;
	}

	@Override
	protected SipMessage createMessage(String[] initialLine) throws Exception {
		return SipServerCodec.USE_NETTY_IMPLEMENTATION ?
				new SipResponseNetty(SipVersion.valueOf(initialLine[0]),
						SipResponseStatus.lookup(Integer.valueOf(initialLine[1]))) :
				new SipResponseImpl(
						SipVersion.valueOf(initialLine[0]),
						SipResponseStatus.lookup(Integer.valueOf(initialLine[1])));
	}

	@Override
	protected SipMessage createMessage(SipResponseStatus status)
			throws Exception {
		return null;	
	}
}
