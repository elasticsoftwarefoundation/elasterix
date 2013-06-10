package org.elasticsoftware.sip.codec;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Decodes {@link ChannelBuffer}s into {@link SipRequest}s
 * <p/>
 * <h3>Parameters that prevents excessive memory consumption</h3>
 * <table border="1">
 * <tr>
 * <th>Name</th><th>Meaning</th>
 * </tr>
 * <tr>
 * <td>{@code maxInitialLineLength}</td>
 * <td>The maximum length of the initial line (e.g. {@code "GET / HTTP/1.0"})
 * If the length of the initial line exceeds this value, a
 * {@link TooLongFrameException} will be raised.</td>
 * </tr>
 * <tr>
 * <td>{@code maxHeaderSize}</td>
 * <td>The maximum length of all headers.  If the sum of the length of each
 * header exceeds this value, a {@link TooLongFrameException} will be raised.</td>
 * </tr>
 * <tr>
 * <td>{@code maxHeaderLineLength}</td>
 * <td>The maximum length of a single header.  If the length of a single
 * header exceeds this value, a {@link TooLongFrameException} will be raised.</td>
 * </tr>
 * </table>
 */
public class SipMessageDecoder extends AbstractSipMessageDecoder {
    private static final Logger log = Logger.getLogger(SipMessageDecoder.class);
    private static final Pattern URI_PATTERN =
            Pattern.compile("^sip:([_a-z0-9-]+(\\.[_a-z0-9-]+)*@)*[a-z0-9-]+(\\.[a-z0-9-]+)*(:[0-9]+)*(;[a-z0-9-]+=[a-z0-9-]+)*$",
                    Pattern.CASE_INSENSITIVE);

    public SipMessageDecoder() {
        super(4096, 8192, 4096);
    }

    public SipMessageDecoder(int maxInitialLineLength, int maxHeaderSize,
                             int maxHeaderLineLength) {
        super(maxInitialLineLength, maxHeaderSize, maxHeaderLineLength);
    }

    @Override
    protected boolean isDecodingRequest() {
        return true;
    }

    @Override
    protected SipMessage createMessage(SipResponseStatus responseStatus) throws Exception {
        return new SipMessageImpl(SipVersion.SIP_2_0, responseStatus);
    }

    @Override
    protected SipMessage createMessage(String[] initialLine) throws Exception {
        if (log.isDebugEnabled()) log.debug(String.format("createMessage. Creating SIP Message[%s]",
                StringUtils.arrayToCommaDelimitedString(initialLine)));

        // OK, check if we have a SIP Request or a SIP Response message....

        // SIP Request   --> REGISTER sip:sip.outerteams.com:5060 SIP/2.0
        // SIP Response  --> SIP/2.0 401 Unauthorized
        if (initialLine[0].substring(0, 2).equalsIgnoreCase("SIP")) {
            return decodeResponse(initialLine);
        } else {
            return decodeRequest(initialLine);
        }
    }

    /**
     * Parses a SIP Response initial line e.g. SIP/2.0 401 Unauthorized.
     * If an parsing error occurs, no message is sent back to sender but
     * only an error message is logged
     *
     * @param initialLine
     * @return
     */
    private SipMessage decodeResponse(String[] initialLine) {
        SipVersion version = SipVersion.lookup(initialLine[0], false);
        if (version == null) {
            log.warn(String.format("constructResponse. Invalid Sip Version[%s]", initialLine[0]));
            return new SipMessageImpl(SipVersion.SIP_2_0, SipResponseStatus.VERSION_NOT_SUPPORTED);
        }
        SipResponseStatus response = SipResponseStatus.lookup(Integer.parseInt(initialLine[1]));
        if (response == null) {
            log.warn(String.format("constructResponse. Invalid Sip Method[%s]", initialLine[0]));
            return new SipMessageImpl(SipVersion.SIP_2_0, SipResponseStatus.RESPONSE_CODE_NOT_SUPPORTED);
        }
        return new SipResponseImpl(version, response);
    }

    /**
     * Parses a SIP Request initial line e.g. REGISTER sip:sip.outerteams.com:5060 SIP/2.0
     *
     * @param initialLine
     * @return
     */
    private SipMessage decodeRequest(String[] initialLine) {
        SipVersion version = SipVersion.lookup(initialLine[2], false);
        if (version == null) {
            log.warn(String.format("constructRequest. Invalid Sip Version[%s]", initialLine[2]));
            return new SipMessageImpl(SipVersion.SIP_2_0, SipResponseStatus.VERSION_NOT_SUPPORTED);
        }
        SipMethod method = SipMethod.lookup(initialLine[0], false);
        if (method == null) {
            log.warn(String.format("constructRequest. Invalid Sip Method[%s]", initialLine[0]));
            return new SipMessageImpl(SipVersion.SIP_2_0, SipResponseStatus.METHOD_NOT_ALLOWED);
        }
        String uri = initialLine[1];
        Matcher matcher = URI_PATTERN.matcher(uri);
        if (!matcher.matches()) {
            log.warn(String.format("constructRequest. Invalid URI[%s]", uri));
            return new SipMessageImpl(SipVersion.SIP_2_0, SipResponseStatus.BAD_REQUEST);
        }
        // TODO: Check domain of URI (do we accept this? Or do we need to transfer / redirect
        // request?
        return new SipRequestImpl(version, method, uri);
    }
}
