package org.elasterix.sip.codec;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;

/**
 * Decodes {@link ChannelBuffer}s into {@link SipRequest}s
 *
 * <h3>Parameters that prevents excessive memory consumption</h3>
 * <table border="1">
 * <tr>
 * <th>Name</th><th>Meaning</th>
 * </tr>
 * <tr>
 * <td>{@code maxInitialLineLength}</td>
 * <td>The maximum length of the initial line (e.g. {@code "GET / HTTP/1.0"})
 *     If the length of the initial line exceeds this value, a
 *     {@link TooLongFrameException} will be raised.</td>
 * </tr>
 * <tr>
 * <td>{@code maxHeaderSize}</td>
 * <td>The maximum length of all headers.  If the sum of the length of each
 *     header exceeds this value, a {@link TooLongFrameException} will be raised.</td>
 * </tr>
 * </table>
 */
public class SipRequestDecoder extends SipMessageDecoder {
	
	public SipRequestDecoder() {
		super(4096, 8192);
	}
	public SipRequestDecoder(int maxInitialLineLength, int maxHeaderSize) {
		super(maxInitialLineLength, maxHeaderSize);
	}

	@Override
	protected boolean isDecodingRequest() {
		return true;
	}

	@Override
	protected SipMessage createMessage(String[] initialLine) throws Exception {
		return new SipRequestImpl(SipVersion.valueOf(initialLine[2]), 
				SipMethod.valueOf(initialLine[0]), initialLine[1]);
	}
}
