package org.elasticsoftware.sip;

/**
 * Callback used by <code>SipMessageSenderImpl<code>
 * to receive callback's that indicate the response status of the message sent by
 * <code>SipMessageSender<code>
 *
 * @author Leonard Wolters
 */
public interface SipMessageCallback {

    /**
     * @param statusCode
     */
    void callback(int statusCode);
}
