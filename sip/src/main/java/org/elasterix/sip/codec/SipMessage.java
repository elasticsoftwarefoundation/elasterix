package org.elasterix.sip.codec;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * An SIP message which provides common properties for {@link SipRequest} and
 * {@link SipResponse}.
 * @see SipHeaders
 * <br>
 * Keep in mind that SIP is *not* an extension of HTTP, see
 * http://tools.ietf.org/html/rfc3261#section-7
 *
 * @author Leonard Wolters
 */
public interface SipMessage {

    /**
     * Returns the header value with the specified header name.  If there are
     * more than one header value for the specified header name, the first
     * value is returned.
     *
     * @return the header value or {@code null} if there is no such header
     */
    String getHeader(String name);

    /**
     * Returns the header values with the specified header name.
     *
     * @return the {@link List} of header values.  An empty list if there is no
     *         such header.
     */
    List<String> getHeaders(String name);

    /**
     * Returns the all header names and values that this message contains.
     *
     * @return the {@link List} of the header name-value pairs.  An empty list
     *         if there is no header in this message.
     */
    List<Map.Entry<String, String>> getHeaders();

    /**
     * Returns {@code true} if and only if there is a header with the specified
     * header name.
     */
    boolean containsHeader(String name);

    /**
     * Returns the {@link Set} of all header names that this message contains.
     */
    Set<String> getHeaderNames();

    /**
     * Returns the protocol version of this message.
     */
    SipVersion getProtocolVersion();

    /**
     * Returns the content of this message.  If there is no content or
     * {@link #isChunked()} returns {@code true}, an
     * {@link ChannelBuffers#EMPTY_BUFFER} is returned.
     */
    ChannelBuffer getContent();

    /**
     * Sets the content of this message.  If {@code null} is specified,
     * the content of this message will be set to {@link ChannelBuffers#EMPTY_BUFFER}.
     */
    void setContent(ChannelBuffer content);

    /**
     * Adds a new header with the specified name and value.
     */
    void addHeader(String name, Object value);

    /**
     * Sets a new header with the specified name and value.  If there is an
     * existing header with the same name, the existing header is removed.
     */
    void setHeader(String name, Object value);

    /**
     * Sets a new header with the specified name and values.  If there is an
     * existing header with the same name, the existing header is removed.
     */
    void setHeader(String name, Iterable<?> values);

    /**
     * Removes the header with the specified name.
     */
    void removeHeader(String name);

    /**
     * Removes all headers from this message.
     */
    void clearHeaders();
    
    /**
     * Sets the response status for this message
     * @param responseStatus
     */
    void setResponseStatus(SipResponseStatus responseStatus);
    
    /**
     * Returns the response status of this message
     * @return
     */
    SipResponseStatus getResponseStatus();
}
