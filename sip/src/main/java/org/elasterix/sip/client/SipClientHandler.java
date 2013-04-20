package org.elasterix.sip.client;

import java.util.List;
import java.util.Map;

import org.elasterix.sip.codec.SipResponse;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.util.CharsetUtil;
import org.springframework.util.StringUtils;

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
        SipResponse response = (SipResponse) e.getMessage();
        if (!response.getHeaderNames().isEmpty()) {
            for (Map.Entry<String, String> header : response.getHeaders()) {
                buf.append(String.format("HEADER: %s = %s", header.getKey(), 
                		header.getValue())).append(NL);
                		//StringUtils.collectionToCommaDelimitedString(header.getValue()))).append(NL);
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
