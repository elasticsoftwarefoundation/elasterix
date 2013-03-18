package org.elasterix.sip.codec;

import static org.jboss.netty.handler.codec.http.HttpConstants.CR;
import static org.jboss.netty.handler.codec.http.HttpConstants.LF;

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
 *
 * @author Leonard Wolters
 */
public abstract class SipMessageDecoder extends ReplayingDecoder<SipMessageDecoder.State> {

    private final int maxInitialLineLength;
    private final int maxHeaderSize;
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
     * {@code maxInitialLineLength (4096}}, {@code maxHeaderSize (8192)}, and
     * {@code maxChunkSize (8192)}.
     */
    protected SipMessageDecoder() {
        this(4096, 8192);
    }

    /**
     * Creates a new instance with the specified parameters.
     */
    protected SipMessageDecoder(int maxInitialLineLength, int maxHeaderSize) {
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
        this.maxInitialLineLength = maxInitialLineLength;
        this.maxHeaderSize = maxHeaderSize;
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, 
    		ChannelBuffer buffer, State state) 
    throws Exception {
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
	            String[] initialLine = splitInitialLine(readLine(buffer, maxInitialLineLength));
	            if (initialLine.length < 3) {
	                // Invalid initial line - ignore.
	                checkpoint(State.SKIP_CONTROL_CHARS);
	                return null;
	            }
	            message = createMessage(initialLine);
	            checkpoint(State.READ_HEADER);
	        }
	        case READ_HEADER: {
	            State nextState = readHeaders(buffer);
	            checkpoint(nextState);           
	            if (nextState == State.SKIP_CONTROL_CHARS) {
	                // No content is expected.
	                return message;
	            }
	            long contentLength = SipHeaders.getContentLength(message, -1);
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
	            int toRead = actualReadableBytes();
	            return buffer.readBytes(toRead);
	        }       
	        case READ_FIXED_LENGTH_CONTENT: {
	            return readFixedLengthContent(buffer);
	        }
	        default: {
	            throw new Error("Shouldn't reach here.");
	        }
        }
    }

    private boolean isContentAlwaysEmpty(SipMessage msg) {
        if (msg instanceof SipResponse) {
        	SipResponse res = (SipResponse) msg;
            int code = res.getStatus().getCode();

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
        long length = SipHeaders.getContentLength(message, -1);
        assert length <= Integer.MAX_VALUE;
        int toRead = (int) length - contentRead;
        if (toRead > actualReadableBytes()) {
            toRead = actualReadableBytes();
        }
        contentRead += toRead;
        if (length < contentRead) {
        	return buffer.readBytes(toRead);
        }
        if (content == null) {
            content = buffer.readBytes((int) length);
        } else {
            content.writeBytes(buffer, (int) length);
        }
        return reset();
    }

    private State readHeaders(ChannelBuffer buffer) throws TooLongFrameException {
        headerSize = 0;
        final SipMessage message = this.message;
        String line = readHeader(buffer);
        String name = null;
        String value = null;
        if (line.length() != 0) {
            message.clearHeaders();
            do {
                char firstChar = line.charAt(0);
                if (name != null && (firstChar == ' ' || firstChar == '\t')) {
                    value = value + ' ' + line.trim();
                } else {
                    if (name != null) {
                        message.addHeader(name, value);
                    }
                    String[] header = splitHeader(line);
                    name = header[0];
                    value = header[1];
                }

                line = readHeader(buffer);
            } while (line.length() != 0);

            // Add the last header.
            if (name != null) {
                message.addHeader(name, value);
            }
        }

        State nextState;

        if (isContentAlwaysEmpty(message)) {
            nextState = State.SKIP_CONTROL_CHARS;
        } else if (SipHeaders.getContentLength(message, -1) >= 0) {
            nextState = State.READ_FIXED_LENGTH_CONTENT;
        } else {
            nextState = State.READ_VARIABLE_LENGTH_CONTENT;
        }
        return nextState;
    }

    private String readHeader(ChannelBuffer buffer) throws TooLongFrameException {
        StringBuilder sb = new StringBuilder(64);
        int headerSize = this.headerSize;

        loop:
        for (;;) {
            char nextByte = (char) buffer.readByte();
            headerSize ++;

            switch (nextByte) {
            case CR:
                nextByte = (char) buffer.readByte();
                headerSize ++;
                if (nextByte == LF) {
                    break loop;
                }
                break;
            case LF:
                break loop;
            }

            // Abort decoding if the header part is too large.
            if (headerSize >= maxHeaderSize) {
                // TODO: Respond with Bad Request and discard the traffic
                //    or close the connection.
                //       No need to notify the upstream handlers - just log.
                //       If decoding a response, just throw an exception.
                throw new TooLongFrameException(
                        "HTTP header is larger than " +
                        maxHeaderSize + " bytes.");
            }

            sb.append(nextByte);
        }

        this.headerSize = headerSize;
        return sb.toString();
    }

    protected abstract boolean isDecodingRequest();
    protected abstract SipMessage createMessage(String[] initialLine) throws Exception;

    private static String readLine(ChannelBuffer buffer, int maxLineLength) throws TooLongFrameException {
        StringBuilder sb = new StringBuilder(64);
        int lineLength = 0;
        while (true) {
            byte nextByte = buffer.readByte();
            if (nextByte == CR) {
                nextByte = buffer.readByte();
                if (nextByte == LF) {
                    return sb.toString();
                }
            } else if (nextByte == LF) {
                return sb.toString();
            } else {
                if (lineLength >= maxLineLength) {
                    // TODO: Respond with Bad Request and discard the traffic
                    //    or close the connection.
                    //       No need to notify the upstream handlers - just log.
                    //       If decoding a response, just throw an exception.
                    throw new TooLongFrameException(
                            "An HTTP line is larger than " + maxLineLength +
                            " bytes.");
                }
                lineLength ++;
                sb.append((char) nextByte);
            }
        }
    }

    private static String[] splitInitialLine(String sb) {
        int aStart;
        int aEnd;
        int bStart;
        int bEnd;
        int cStart;
        int cEnd;

        aStart = findNonWhitespace(sb, 0);
        aEnd = findWhitespace(sb, aStart);

        bStart = findNonWhitespace(sb, aEnd);
        bEnd = findWhitespace(sb, bStart);

        cStart = findNonWhitespace(sb, bEnd);
        cEnd = findEndOfString(sb);

        return new String[] {
                sb.substring(aStart, aEnd),
                sb.substring(bStart, bEnd),
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
