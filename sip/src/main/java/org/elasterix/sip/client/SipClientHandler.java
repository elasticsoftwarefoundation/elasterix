package org.elasterix.sip.client;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.util.CharsetUtil;

/**
 * @author Leonard Wolters
 */
public class SipClientHandler extends SimpleChannelUpstreamHandler {
	private static final String NL = "\r\n";

	/** Local cache for content, used by testing process */
    private StringBuilder buf;
    
    public SipClientHandler(StringBuilder buffer) {
    	this.buf = buffer;
	}

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) 
    throws Exception {
    	
        HttpResponse response = (HttpResponse) e.getMessage();
        if (!response.getHeaderNames().isEmpty()) {
            for (String name: response.getHeaderNames()) {
                for (String value: response.getHeaders(name)) {
                	buf.append("HEADER: " + name + " = " + value).append(NL);
                }
            }
            buf.append("").append(NL);
        }

        ChannelBuffer content = response.getContent();
        if (content.readable()) {
        	buf.append("CONTENT {").append(NL);
        	buf.append(content.toString(CharsetUtil.UTF_8)).append(NL);
        	buf.append("} END OF CONTENT").append(NL);
        }
    }
}
