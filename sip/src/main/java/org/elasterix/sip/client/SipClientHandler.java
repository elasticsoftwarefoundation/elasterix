package org.elasterix.sip.client;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.util.CharsetUtil;

/**
 * @author Leonard Wolters
 */
public class SipClientHandler extends SimpleChannelUpstreamHandler {
	private static final String NL = "\r\n";

    private boolean readingChunks;
    private StringBuilder buf;
    
    public SipClientHandler(StringBuilder buffer) {
    	this.buf = buffer;
	}

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) 
    throws Exception {
    	
        if (!readingChunks) {
            HttpResponse response = (HttpResponse) e.getMessage();
            
            buf.setLength(0);
            buf.append("STATUS: " + response.getStatus()).append(NL);
            buf.append("VERSION: " + response.getProtocolVersion()).append(NL);
            buf.append("").append(NL);

            if (!response.getHeaderNames().isEmpty()) {
                for (String name: response.getHeaderNames()) {
                    for (String value: response.getHeaders(name)) {
                    	buf.append("HEADER: " + name + " = " + value).append(NL);
                    }
                }
                buf.append("").append(NL);
            }

            if (response.isChunked()) {
                readingChunks = true;
                buf.append("CHUNKED CONTENT {").append(NL);
            } else {
                ChannelBuffer content = response.getContent();
                if (content.readable()) {
                	buf.append("CONTENT {").append(NL);
                	buf.append(content.toString(CharsetUtil.UTF_8)).append(NL);
                	buf.append("} END OF CONTENT").append(NL);
                }
            }
        } else {
            HttpChunk chunk = (HttpChunk) e.getMessage();
            if (chunk.isLast()) {
                readingChunks = false;
                buf.append("} END OF CHUNKED CONTENT").append(NL);
            } else {
            	buf.append(chunk.getContent().toString(CharsetUtil.UTF_8)).append(NL);                
            }
        }
    }
}
