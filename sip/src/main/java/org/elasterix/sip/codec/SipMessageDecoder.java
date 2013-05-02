package org.elasterix.sip.codec;

import static org.jboss.netty.handler.codec.http.HttpConstants.CR;
import static org.jboss.netty.handler.codec.http.HttpConstants.LF;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.replay.ReplayingDecoder;

/**
 * Decodes {@link ChannelBuffer}s into {@link SipMessage}s
 *
 * <h3>Parameters that prevents excessive memory consumption</h3>
 * <table border="1">
 * <tr>
 * <th>Name</th><th>Meaning</th>
 * </tr>
 * <tr>
 * <td>{@code maxInitialLineLength}</td>
 * <td>The maximum length of the initial line.
 *     If the length of the initial line exceeds this value, a
 *     {@link TooLongFrameException} will be raised.</td>
 * </tr>
 * <tr>
 * <td>{@code maxHeaderSize}</td>
 * <td>The maximum length of all headers.  If the sum of the length of each
 *     header exceeds this value, a {@link TooLongFrameException} will be raised.</td>
 * </tr>
 * </table>
 * <br>
 * <br>
 * The ReplayingDecoder only makes sense in TCP connections. Basically what it
 * does is give you the ability to create checkpoints when reading structured
 * variable-sized messages without having to manually check for the available bytes. <br>
 * <br>
 * What that means is that, unlike other decoders, you can request as many bytes as
 * you want from the buffer. If they are not available the operation will silently
 * fail until the socket reads more data — and nothing gets changed. If enough
 * bytes are available, the buffer is drained and you mark a checkpoint. Having
 * this checkpoint set means that if the next read operation fails (less bytes
 * than the ones you're requesting), it will start at the last saved checkpoint.
 *
 * @author Leonard Wolters
 */
public abstract class SipMessageDecoder extends ReplayingDecoder<SipMessageDecoder.State> {
	private static final Logger log = Logger.getLogger(SipMessageDecoder.class);

	private final int maxInitialLineLength;
	private final int maxHeaderSize;
	private final int maxHeaderLineLength;
	private SipMessage message;
	private ChannelBuffer content;
	private int headerSize;
	private int contentRead;

	/**
	 * The internal state of {@link SipMessageDecoder}.
	 */
	protected enum State {
		SKIP_CONTROL_CHARS,
		READ_INITIAL,
		READ_HEADER,
		READ_VARIABLE_LENGTH_CONTENT,
		READ_FIXED_LENGTH_CONTENT,
	}

	/**
	 * Creates a new instance with the default
	 * {@code maxInitialLineLength (4096}}, {@code maxHeaderSize (8192)},
	 *  {@code maxHeaderSize (4096)}
	 */
	protected SipMessageDecoder() {
		this(4096, 8192, 4096);
	}

	/**
	 * Creates a new instance with the specified parameters.
	 */
	protected SipMessageDecoder(int maxInitialLineLength, int maxHeaderSize,
			int maxHeaderLineLength) {
		super(State.SKIP_CONTROL_CHARS, true);
		if (maxInitialLineLength <= 0) {
			throw new IllegalArgumentException(
					"maxInitialLineLength must be a positive integer: " +
							maxInitialLineLength);
		}
		if (maxHeaderSize <= 0) {
			throw new IllegalArgumentException(
					"maxHeaderSize must be a positive integer: " +
							maxHeaderSize);
		}
		if (maxHeaderLineLength <= 0) {
			throw new IllegalArgumentException(
					"maxHeaderLineLength must be a positive integer: " +
							maxHeaderSize);
		}
		this.maxInitialLineLength = maxInitialLineLength;
		this.maxHeaderSize = maxHeaderSize;
		this.maxHeaderLineLength = maxHeaderLineLength;
		log.debug(String.format("init(%d,%d,%d)", maxInitialLineLength, maxHeaderSize, maxHeaderLineLength));
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel,
			ChannelBuffer buffer, State state)
					throws Exception {
		if(log.isDebugEnabled()) log.debug(String.format("decode. State[%s]", state.name()));
		switch (state) {
		case SKIP_CONTROL_CHARS: {
			try {
				skipControlCharacters(buffer);
				checkpoint(State.READ_INITIAL);
			} finally {
				checkpoint();
			}
		}
		case READ_INITIAL: {
			if(log.isDebugEnabled()) log.debug(String.format("decode. READ_INITIAL"));
			String[] initialLine = splitInitialLine(readLine(buffer, maxInitialLineLength));
			if (initialLine.length < 3) {

				// Invalid initial line - ignore.
				checkpoint(State.SKIP_CONTROL_CHARS);
				return createMessage(SipResponseStatus.BAD_GATEWAY);
			}
			message = createMessage(initialLine);
			if(message == null || message.getResponseStatus() != null) {
				// something went wrong while constructing the message, which
				// might indicate unsupported sip versions, sip methods etc.
				checkpoint(State.SKIP_CONTROL_CHARS);
				return message;
			}
			checkpoint(State.READ_HEADER);
		}
		case READ_HEADER: {
			if(log.isDebugEnabled()) log.debug(String.format("decode. READ_HEADER"));
			State nextState = readHeaders(buffer, maxHeaderSize);
			if(log.isDebugEnabled()) log.debug(String.format("decode. Next state: " + nextState.name()));
			checkpoint(nextState);
			if (nextState == State.SKIP_CONTROL_CHARS) {
				// No content is expected.
				return message;
			}
			long contentLength = message.getContentLength(-1);
			if (contentLength == 0 || contentLength == -1 && isDecodingRequest()) {
				content = ChannelBuffers.EMPTY_BUFFER;
				return reset();
			}

			switch (nextState) {
			case READ_FIXED_LENGTH_CONTENT:
				break;
			case READ_VARIABLE_LENGTH_CONTENT:
				break;
			default:
				throw new IllegalStateException("Unexpected state: " + nextState);
			}
			// We return null here, this forces decode to be called again
			// where we will decode the content
			return null;
		}
		case READ_VARIABLE_LENGTH_CONTENT: {
			if(log.isDebugEnabled()) log.debug(String.format("decode. READ_VARIABLE_LENGTH_CONTENT"));
			int toRead = actualReadableBytes();
			return buffer.readBytes(toRead);
		}
		case READ_FIXED_LENGTH_CONTENT: {
			if(log.isDebugEnabled()) log.debug(String.format("decode. READ_FIXED_LENGTH_CONTENT"));
			return readFixedLengthContent(buffer);
		}
		default: {
			throw new Error("Shouldn't reach here.");
		}
		}
	}

	private Object reset() {
		SipMessage message = this.message;
		ChannelBuffer content = this.content;

		if (content != null) {
			message.setContent(content);
			this.content = null;
		}
		this.message = null;

		checkpoint(State.SKIP_CONTROL_CHARS);
		return message;
	}

	private static void skipControlCharacters(ChannelBuffer buffer) {
		for (;;) {
			char c = (char) buffer.readUnsignedByte();
			if (!Character.isISOControl(c) &&
					!Character.isWhitespace(c)) {
				buffer.readerIndex(buffer.readerIndex() - 1);
				break;
			}
		}
	}

	private Object readFixedLengthContent(ChannelBuffer buffer) {
		//we have a content-length so we just read the correct number of bytes
		long contentLength = message.getContentLength(-1);
		assert contentLength <= Integer.MAX_VALUE;
		int toRead = (int) contentLength - contentRead;
		if (toRead > actualReadableBytes()) {
			toRead = actualReadableBytes();
		}
		if(log.isDebugEnabled()) {
			log.debug(String.format("readFixedLengthContent. "
				+ "Length[%d], toRead[%d], actual[%d]", contentLength, toRead, actualReadableBytes()));
		}
		contentRead += toRead;
		if (contentLength < contentRead) {
			return buffer.readBytes(toRead);
		}
		if (content == null) {
			content = buffer.readBytes((int) contentLength);
		} else {
			content.writeBytes(buffer, (int) contentLength);
		}
		return reset();
	}

	/**
	 * Read and parses SIP headers<br>
	 * Process continue either until a empty line is reached or if end of file / content is
	 * reached
	 *
	 * @param buffer
	 * @param maxHeaderSize
	 * @return
	 * @throws TooLongFrameException
	 */
	private State readHeaders(ChannelBuffer buffer, int maxHeaderSize) throws TooLongFrameException {

		// reset (total) header size
		this.headerSize = 0;

		String name = null;
		String value = null;
		String line = readLine(buffer, maxHeaderLineLength);
		if (line.length() != 0) {
			// at least one header is present?
			message.clearHeaders();
			do {
				char firstChar = line.charAt(0);
				// header continues on next line?
				if (name != null && (firstChar == ' ' || firstChar == '\t')) {
					value = value + ' ' + line.trim();
				} else {
					if (name != null) {						
						message.addHeader(SipHeader.lookup(name), value);
					}
					String[] header = splitHeader(line);
					name = header[0];
					value = header[1];
				}
				line = readLine(buffer, maxHeaderLineLength);
				headerSize += line.length();
				if(headerSize >= maxHeaderSize) {
					throw new TooLongFrameException(String.format("Given headers size [%d] "
							+ "exceeds max[%d]", headerSize, maxHeaderSize));
				}
			} while (line.length() != 0);

			// Add the last header.
			if (name != null) {
				//SipHeaders.addHeader(message, name, value);
				message.addHeader(SipHeader.lookup(name), value);
			}
		}
		
		// check if we need to parse SDP content
		State nextState;
		if (isContentAlwaysEmpty(message)) {
			nextState = State.SKIP_CONTROL_CHARS;
		} else if (message.getContentLength(-1) >= 0) {
			nextState = State.READ_FIXED_LENGTH_CONTENT;
		} else {
			nextState = State.READ_VARIABLE_LENGTH_CONTENT;
		}
		return nextState;
	}

	private boolean isContentAlwaysEmpty(SipMessage msg) {
		if (msg instanceof SipResponse) {
			SipResponse res = (SipResponse) msg;
			int code = res.getResponseStatus().getCode();

			// Correctly handle return codes of 1xx.
			//
			// See: http://tools.ietf.org/html/rfc3261#section-21
			if (code >= 100 && code < 200) {
				return true;
			}

			switch (code) {
			case 204: case 205: case 304:
				return true;
			}
		}
		return false;
	}
	protected abstract boolean isDecodingRequest();
	protected abstract SipMessage createMessage(String[] initialLine) throws Exception;
	protected abstract SipMessage createMessage(SipResponseStatus status) throws Exception;

	/**
	 * If the length of the line exceeds the maxLineLenght, a <code>TooLongFrameException</code>
	 * is thrown
	 *
	 * @param buffer
	 * @param maxLineLength
	 * @return
	 * @throws TooLongFrameException
	 */
	private String readLine(ChannelBuffer buffer, int maxLineLength) throws TooLongFrameException {
		StringBuilder sb = new StringBuilder(64);
		int lineLength = 0;
		while (actualReadableBytes() > 0) {
			byte nextByte = buffer.readByte();
			if (nextByte == CR) {
				nextByte = buffer.readByte();
				if (nextByte == LF) {
					return sb.toString();
				}
			} else if (nextByte == LF) {
				return sb.toString();
			} else {
				if (maxLineLength > 0 && lineLength >= maxLineLength) {
					throw new TooLongFrameException(String.format("Given line length [%d] "
							+ "exceeds max[%d]",  lineLength, maxLineLength));
				}
				lineLength ++;
				sb.append((char) nextByte);
			}
		}
		return sb.toString();
	}

	/**
	 * A SIP initial line always consists of 3 tokens, e.g.
	 * INVITE sip:bob@biloxi.com SIP/2.0 or <br>
	 * SIP/2.0 200 OK<br>
	 * <br>
	 * If less tokens are found, last two tokens are empty
	 *
	 * @param String[] containing 3 tokens.
	 * @return
	 */
	private static String[] splitInitialLine(String sb) {
		int aStart = findNonWhitespace(sb, 0);
		int aEnd = findWhitespace(sb, aStart);

		int bStart = findNonWhitespace(sb, aEnd);
		int bEnd = findWhitespace(sb, bStart);

		int cStart = findNonWhitespace(sb, bEnd);
		int cEnd = findEndOfString(sb);

		return new String[] { sb.substring(aStart, aEnd),
				bStart < bEnd? sb.substring(bStart, bEnd) : "",
						cStart < cEnd? sb.substring(cStart, cEnd) : "" };
	}

	private static String[] splitHeader(String sb) {
		final int length = sb.length();
		int nameStart;
		int nameEnd;
		int colonEnd;
		int valueStart;
		int valueEnd;

		nameStart = findNonWhitespace(sb, 0);
		for (nameEnd = nameStart; nameEnd < length; nameEnd ++) {
			char ch = sb.charAt(nameEnd);
			if (ch == ':' || Character.isWhitespace(ch)) {
				break;
			}
		}

		for (colonEnd = nameEnd; colonEnd < length; colonEnd ++) {
			if (sb.charAt(colonEnd) == ':') {
				colonEnd ++;
				break;
			}
		}

		valueStart = findNonWhitespace(sb, colonEnd);
		if (valueStart == length) {
			return new String[] {
					sb.substring(nameStart, nameEnd),
					""
			};
		}

		valueEnd = findEndOfString(sb);
		return new String[] {
				sb.substring(nameStart, nameEnd),
				sb.substring(valueStart, valueEnd)
		};
	}

	private static int findNonWhitespace(String sb, int offset) {
		int result;
		for (result = offset; result < sb.length(); result ++) {
			if (!Character.isWhitespace(sb.charAt(result))) {
				break;
			}
		}
		return result;
	}

	private static int findWhitespace(String sb, int offset) {
		int result;
		for (result = offset; result < sb.length(); result ++) {
			if (Character.isWhitespace(sb.charAt(result))) {
				break;
			}
		}
		return result;
	}

	private static int findEndOfString(String sb) {
		int result;
		for (result = sb.length(); result > 0; result --) {
			if (!Character.isWhitespace(sb.charAt(result - 1))) {
				break;
			}
		}
		return result;
	}
}
