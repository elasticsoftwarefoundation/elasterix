package org.elasterix.sip.codec.netty;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.elasterix.sip.codec.SipCodecUtil;
import org.elasterix.sip.codec.SipHeader;
import org.elasterix.sip.codec.SipMessage;
import org.jboss.netty.util.internal.CaseIgnoringComparator;

/**
 * See http://tools.ietf.org/html/rfc3261#section-20
 * 
 * @author Leonard Wolters
 */
public class SipHeaders {
	private static final ConcurrentHashMap<Integer, SipHeader> cached 
		= new ConcurrentHashMap<Integer, SipHeader>();

    /**
     * Returns the header value with the specified header.  If there is
     * more than one header value for the specified header, the first
     * value is returned.
     *
     * @return the header value or {@code null} if there is no such header
     */
    public static String getHeaderValue(SipMessage message, String header) {
        return message.getHeaderValue(lookup(header));
    }

    /**
     * Returns the header value with the specified header.  If there is
     * more than one header value for the specified header, the first
     * value is returned. If no value is found at all, <code>defaultValue</code>
     * is returned
     *
     * @return the header value or the {@code defaultValue} if there is no such
     *         header
     */
    public static String getHeaderValue(SipMessage message, String header, 
    		String defaultValue) {
        String value = message.getHeaderValue(lookup(header));
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    /**
     * Sets a new header with the specified value. Existing header values 
     * (if present) will be removed.
     */
    public static void setHeader(SipMessage message, String header, Object value) {
        message.setHeader(lookup(header), value);
    }

    /**
     * Sets a new header with the specified values. Existing header values 
     * (if present) will be removed.
     */
    public static void setHeader(SipMessage message, String header, Iterable<?> values) {
        message.setHeader(lookup(header), values);
    }

    /**
     * Adds a new header with the specified value.
     */
    public static void addHeader(SipMessage message, String header, Object value) {
    	message.addHeader(lookup(header), value);
    }

    /**
     * Returns the integer header value for the specified header.  If
     * there is more than one header value for the specified header, the
     * first value is returned.
     *
     * @return the header value
     * @throws NumberFormatException
     *         if there is no such header or the header value is not a number
     */
    public static int getHeaderValueAsInt(SipMessage message, String header) {
        String value = getHeaderValue(message, header);
        if (value == null) {
            throw new NumberFormatException("null");
        }
        return Integer.parseInt(value);
    }

    /**
     * Returns the integer header value with the specified header. If
     * there is more than one header value for the specified header, the
     * first value is returned. If no value is found for given header, 
     * defaultValue is returned
     *
     * @return the header value or the {@code defaultValue} if there is no such
     *         header or the header value is not a number
     */
    public static int getHeaderAsInt(SipMessage message, String header, int defaultValue) {
        String value = getHeaderValue(message, header);
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
     * Sets a new integer header with the specified value. Existing header values
     * will be removed
     */
    public static void setHeader(SipMessage message, String header, int value) {
        message.setHeader(lookup(header), value);
    }

    /**
     * Adds a new integer header with the specified value.
     */
    public static void addHeader(SipMessage message, String header, int value) {
        message.addHeader(lookup(header), value);
    }

    /**
     * Returns the length of the content. Please note that this value is
     * not retrieved from {@link SipMessage#getContent()} but from the
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
     * Sets the {@code "Content-Length"} header.
     */
    public static void setContentLength(SipMessage message, long length) {
        message.setHeader(SipHeader.CONTENT_LENGTH, length);
    }
   
	/**
     * Returns the length of the content.  Please note that this value is
     * not retrieved from {@link SipMessage#getContent()} but from the
     * {@code "Content-Length"} header, and thus they are independent from each
     * other.
     *
     * @return the content length or {@code defaultValue} if this message does
     *         not have the {@code "Content-Length"} header
     */
    public static long getContentLength(SipMessage message, long defaultValue) {
        String contentLength = message.getHeaderValue(SipHeader.CONTENT_LENGTH);
        if (contentLength != null) {
            return Long.parseLong(contentLength);
        }
        return defaultValue;
    }
    
    private static SipHeader lookup(String headerName) {
    	SipHeader sh = cached.get(hash(headerName));
    	if(sh != null) {
    		return sh;
    	}
		for(SipHeader s : SipHeader.values()) {
			if(eq(headerName, s.getName())) {
				cached.putIfAbsent(hash(headerName), s);
				return s;
			}
		}
		return null;
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
    
    /**
     * Complicated method equals method build for 'performance'.
     * As soon as 1 character is different, false is returned.
     * 
     * @param name1
     * @param name2
     * @return
     */
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
