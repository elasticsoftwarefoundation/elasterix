package org.elasterix.sip.codec;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.util.internal.CaseIgnoringComparator;

/**
 * See http://tools.ietf.org/html/rfc3261#section-20
 * 
 * @author Leonard Wolters
 */
public class SipHeaders {

	public static final class Names {
		public static final String ACCEPT = "Accept";
		public static final String ACCEPT_ENCODING = "Accept-Encoding";
		public static final String ACCEPT_LANGUAGE = "Accept-Language";
		public static final String ALERT_INFO = "Alert-Info";
		public static final String ALLOW = "Allow";
		public static final String AUTHENTICATION_INFO = "Authentication-info";
		public static final String AUTHORIZATION = "Authorization";
		public static final String CALL_ID = "Call-ID";
		public static final String CALL_INFO = "Call-Info";
		public static final String CONTACT = "Contact";
		public static final String CONTENT_DISPOSITION = "Content-Disposition";
		public static final String CONTENT_ENCODING = "Content-Encoding";
		public static final String CONTENT_LANGUAGE = "Content-Language";
		public static final String CONTENT_LENGTH = "Content-Length";
		public static final String CONTENT_TYPE = "Content-Type";
		public static final String CSEQ = "CSeq";
		public static final String ERROR_INFO = "Error-Info";
		public static final String EXPIRES = "Expires";
		public static final String FROM = "From";
		public static final String IN_REPLY_TO = "In-Reply-To";
		public static final String MAX_FORWARDS = "Max-Forwards";
		public static final String MIN_EXPIRES = "Min-Expires";
		public static final String MIME_VERSION = "Mime-Version";
		public static final String ORGANIZATION = "Organization";
		public static final String PRIORITY = "Priority";
		public static final String PROXY_AUTHENTICATE = "Proxy-Authenticate";
		public static final String PROXY_AUTHORIZATION = "Proxy-Authorization";
		public static final String PROXY_REQUIRE = "Proxy-Require";
		public static final String RECORD_ROUTE = "Record-Route";
		public static final String REPLY_TO = "Reply-To";
		public static final String RETRY_AFTER = "Reply-After";
		public static final String ROUTE = "Route";
		public static final String SERVER = "Server";
		public static final String SUBJECT = "Subject";
		public static final String SUPPORTED = "Supported";
		public static final String TIMESTAMP = "Timestamp";
		public static final String TO = "To";
		public static final String UNSUPPORTED = "Unsupported";
		public static final String USER_AGENT = "User-Agent";
		public static final String VIA = "Via";
		public static final String WARNING = "Warning";
		public static final String WWW_AUTHENTICATE = "WWW-Authenticate";
	}
	
	public static final class Values {
		 public static final String CHUNKED = "chunked";
		 public static final String CLOSE = "close";
		 public static final String KEEP_ALIVE = "keep-alive";
	}
	
    /**
     * Returns the header value with the specified header name.  If there are
     * more than one header value for the specified header name, the first
     * value is returned.
     *
     * @return the header value or {@code null} if there is no such header
     */
    public static String getHeader(SipMessage message, String name) {
        return message.getHeader(name);
    }

    /**
     * Returns the header value with the specified header name.  If there are
     * more than one header value for the specified header name, the first
     * value is returned.
     *
     * @return the header value or the {@code defaultValue} if there is no such
     *         header
     */
    public static String getHeader(SipMessage message, String name, String defaultValue) {
        String value = message.getHeader(name);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    /**
     * Sets a new header with the specified name and value.  If there is an
     * existing header with the same name, the existing header is removed.
     */
    public static void setHeader(SipMessage message, String name, Object value) {
        message.setHeader(name, value);
    }

    /**
     * Sets a new header with the specified name and values.  If there is an
     * existing header with the same name, the existing header is removed.
     */
    public static void setHeader(SipMessage message, String name, Iterable<?> values) {
        message.setHeader(name, values);
    }

    /**
     * Adds a new header with the specified name and value.
     */
    public static void addHeader(SipMessage message, String name, Object value) {
        message.addHeader(name, value);
    }

    /**
     * Returns the integer header value with the specified header name.  If
     * there are more than one header value for the specified header name, the
     * first value is returned.
     *
     * @return the header value
     * @throws NumberFormatException
     *         if there is no such header or the header value is not a number
     */
    public static int getIntHeader(SipMessage message, String name) {
        String value = getHeader(message, name);
        if (value == null) {
            throw new NumberFormatException("null");
        }
        return Integer.parseInt(value);
    }

    /**
     * Returns the integer header value with the specified header name.  If
     * there are more than one header value for the specified header name, the
     * first value is returned.
     *
     * @return the header value or the {@code defaultValue} if there is no such
     *         header or the header value is not a number
     */
    public static int getIntHeader(SipMessage message, String name, int defaultValue) {
        String value = getHeader(message, name);
        if (value == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Sets a new integer header with the specified name and value.  If there
     * is an existing header with the same name, the existing header is removed.
     */
    public static void setIntHeader(SipMessage message, String name, int value) {
        message.setHeader(name, value);
    }

    /**
     * Sets a new integer header with the specified name and values.  If there
     * is an existing header with the same name, the existing header is removed.
     */
    public static void setIntHeader(SipMessage message, String name, Iterable<Integer> values) {
        message.setHeader(name, values);
    }

    /**
     * Adds a new integer header with the specified name and value.
     */
    public static void addIntHeader(SipMessage message, String name, int value) {
        message.addHeader(name, value);
    }

    /**
     * Returns the length of the content.  Please note that this value is
     * not retrieved from {@link HttpMessage#getContent()} but from the
     * {@code "Content-Length"} header, and thus they are independent from each
     * other.
     *
     * @return the content length or {@code 0} if this message does not have
     *         the {@code "Content-Length"} header
     */
    public static long getContentLength(SipMessage message) {
        return getContentLength(message, 0L);
    }

    /**
     * Returns the length of the content.  Please note that this value is
     * not retrieved from {@link HttpMessage#getContent()} but from the
     * {@code "Content-Length"} header, and thus they are independent from each
     * other.
     *
     * @return the content length or {@code defaultValue} if this message does
     *         not have the {@code "Content-Length"} header
     */
    public static long getContentLength(SipMessage message, long defaultValue) {
        String contentLength = message.getHeader(Names.CONTENT_LENGTH);
        if (contentLength != null) {
            return Long.parseLong(contentLength);
        }

        // WebSockset messages have constant content-lengths.
        if (message instanceof SipRequest) {
        	SipRequest req = (SipRequest) message;
//            if (SipMethod.GET.equals(req.getMethod()) &&
//                req.containsHeader(Names.SEC_WEBSOCKET_KEY1) &&
//                req.containsHeader(Names.SEC_WEBSOCKET_KEY2)) {
//                return 8;
//            }
        } else if (message instanceof SipResponse) {
        	SipResponse res = (SipResponse) message;
//            if (res.getStatus().getCode() == 101 &&
//                res.containsHeader(Names.SEC_WEBSOCKET_ORIGIN) &&
//                res.containsHeader(Names.SEC_WEBSOCKET_LOCATION)) {
//                return 16;
//            }
        }

        return defaultValue;
    }

    /**
     * Sets the {@code "Content-Length"} header.
     */
    public static void setContentLength(SipMessage message, long length) {
        message.setHeader(Names.CONTENT_LENGTH, length);
    }
   
    private static final int BUCKET_SIZE = 17;
    private static int hash(String name) {
        int h = 0;
        for (int i = name.length() - 1; i >= 0; i --) {
            char c = name.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                c += 32;
            }
            h = 31 * h + c;
        }
        if (h > 0) {
            return h;
        } else if (h == Integer.MIN_VALUE) {
            return Integer.MAX_VALUE;
        } else {
            return -h;
        }
    }
    private static boolean eq(String name1, String name2) {
        int nameLen = name1.length();
        if (nameLen != name2.length()) {
            return false;
        }

        for (int i = nameLen - 1; i >= 0; i --) {
            char c1 = name1.charAt(i);
            char c2 = name2.charAt(i);
            if (c1 != c2) {
                if (c1 >= 'A' && c1 <= 'Z') {
                    c1 += 32;
                }
                if (c2 >= 'A' && c2 <= 'Z') {
                    c2 += 32;
                }
                if (c1 != c2) {
                    return false;
                }
            }
        }
        return true;
    }
    private static int index(int hash) {
        return hash % BUCKET_SIZE;
    }
    private final HeaderEntry[] entries = new HeaderEntry[BUCKET_SIZE];
    private final HeaderEntry head = new HeaderEntry(-1, null, null);

    SipHeaders() {
        head.before = head.after = head;
    }

    void validateHeaderName(String name) {
        SipCodecUtil.validateHeaderName(name);
    }

    void addHeader(final String name, final Object value) {
        validateHeaderName(name);
        String strVal = toString(value);
        SipCodecUtil.validateHeaderValue(strVal);
        int h = hash(name);
        int i = index(h);
        addHeader0(h, i, name, strVal);
    }

    private void addHeader0(int h, int i, final String name, final String value) {
        // Update the hash table.
        HeaderEntry e = entries[i];
        HeaderEntry newEntry;
        entries[i] = newEntry = new HeaderEntry(h, name, value);
        newEntry.next = e;

        // Update the linked list.
        newEntry.addBefore(head);
    }

    void removeHeader(final String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        int h = hash(name);
        int i = index(h);
        removeHeader0(h, i, name);
    }

    private void removeHeader0(int h, int i, String name) {
        HeaderEntry e = entries[i];
        if (e == null) {
            return;
        }

        for (;;) {
            if (e.hash == h && eq(name, e.key)) {
                e.remove();
                HeaderEntry next = e.next;
                if (next != null) {
                    entries[i] = next;
                    e = next;
                } else {
                    entries[i] = null;
                    return;
                }
            } else {
                break;
            }
        }

        for (;;) {
            HeaderEntry next = e.next;
            if (next == null) {
                break;
            }
            if (next.hash == h && eq(name, next.key)) {
                e.next = next.next;
                next.remove();
            } else {
                e = next;
            }
        }
    }

    void setHeader(final String name, final Object value) {
        validateHeaderName(name);
        String strVal = toString(value);
        SipCodecUtil.validateHeaderValue(strVal);
        int h = hash(name);
        int i = index(h);
        removeHeader0(h, i, name);
        addHeader0(h, i, name, strVal);
    }

    void setHeader(final String name, final Iterable<?> values) {
        if (values == null) {
            throw new NullPointerException("values");
        }

        validateHeaderName(name);

        int h = hash(name);
        int i = index(h);

        removeHeader0(h, i, name);
        for (Object v: values) {
            if (v == null) {
                break;
            }
            String strVal = toString(v);
            SipCodecUtil.validateHeaderValue(strVal);
            addHeader0(h, i, name, strVal);
        }
    }

    void clearHeaders() {
        for (int i = 0; i < entries.length; i ++) {
            entries[i] = null;
        }
        head.before = head.after = head;
    }

    String getHeader(final String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }

        int h = hash(name);
        int i = index(h);
        HeaderEntry e = entries[i];
        while (e != null) {
            if (e.hash == h && eq(name, e.key)) {
                return e.value;
            }

            e = e.next;
        }
        return null;
    }

    List<String> getHeaders(final String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }

        LinkedList<String> values = new LinkedList<String>();

        int h = hash(name);
        int i = index(h);
        HeaderEntry e = entries[i];
        while (e != null) {
            if (e.hash == h && eq(name, e.key)) {
                values.addFirst(e.value);
            }
            e = e.next;
        }
        return values;
    }

    List<Map.Entry<String, String>> getHeaders() {
        List<Map.Entry<String, String>> all =
            new LinkedList<Map.Entry<String, String>>();

        HeaderEntry e = head.after;
        while (e != head) {
            all.add(e);
            e = e.after;
        }
        return all;
    }

    boolean containsHeader(String name) {
        return getHeader(name) != null;
    }

    Set<String> getHeaderNames() {
        Set<String> names =
            new TreeSet<String>(CaseIgnoringComparator.INSTANCE);

        HeaderEntry e = head.after;
        while (e != head) {
            names.add(e.key);
            e = e.after;
        }
        return names;
    }

    private static String toString(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    private static final class HeaderEntry implements Map.Entry<String, String> {
        final int hash;
        final String key;
        String value;
        HeaderEntry next;
        HeaderEntry before, after;

        HeaderEntry(int hash, String key, String value) {
            this.hash = hash;
            this.key = key;
            this.value = value;
        }

        void remove() {
            before.after = after;
            after.before = before;
        }

        void addBefore(HeaderEntry e) {
            after  = e;
            before = e.before;
            before.after = this;
            after.before = this;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public String setValue(String value) {
            if (value == null) {
                throw new NullPointerException("value");
            }
            SipCodecUtil.validateHeaderValue(value);
            String oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        @Override
        public String toString() {
            return key + '=' + value;
        }
    }
}
