package org.elasterix.sip;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

import java.util.Map;

import org.apache.log4j.Logger;
import org.elasterix.sip.codec.SipRequest;
import org.elasterix.sip.codec.SipResponse;
import org.elasterix.sip.codec.SipResponseImpl;
import org.elasterix.sip.codec.SipResponseStatus;
import org.elasterix.sip.codec.SipVersion;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.util.CharsetUtil;

/**
 * Sip Server Handler<br>
 * <br>
 * This handler takes care of all incoming SIP messages and sent back corresponding 
 * SIP responses<br>
 * 
 * @author Leonard Wolters
 */
public class SipServerHandler extends SimpleChannelUpstreamHandler {
	private static final Logger log = Logger.getLogger(SipServerHandler.class);

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) 
	throws Exception {
		SipRequest request = (SipRequest) e.getMessage();

		// print SIP request (only in debug mode)
		if(log.isDebugEnabled()) {
			StringBuilder buf = new StringBuilder();
			for (Map.Entry<String, String> h: request.getHeaders()) {
				buf.append("HEADER: " + h.getKey() + " = " + h.getValue() + "\r\n");
			}
			buf.append("\r\n");
			ChannelBuffer content = request.getContent();
			if (content.readable()) {
				buf.append("CONTENT: " + content.toString(CharsetUtil.UTF_8) + "\r\n");
			}
			log.debug(String.format("SIP Received\r\n[%s]", buf.toString()));
		}
		
		// writing reponse
		writeResponse(request, e);
	}

	private void writeResponse(SipRequest request, MessageEvent e) {
		// Decide whether to close the connection or not.		
		//boolean keepAlive = SipHeaders.isKeepAlive(request);
		boolean keepAlive = true;

		// Build the response object.
		SipResponse response = new SipResponseImpl(SipVersion.SIP_2_0, 
				SipResponseStatus.OK);
		//response.setContent(ChannelBuffers.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));
		response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");

		if (keepAlive) {
			// Add 'Content-Length' header only for a keep-alive connection.
			response.setHeader(CONTENT_LENGTH, response.getContent().readableBytes());
			// Add keep alive header as per:
			// - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
			response.setHeader(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
		}

		// Write the response.
		ChannelFuture future = e.getChannel().write(response);

		// Close the non-keep-alive connection after the write operation is done.
		if (!keepAlive) {
			future.addListener(ChannelFutureListener.CLOSE);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		log.error(e.getCause().getMessage(), e.getCause().getCause());
		e.getChannel().close();
	}	

	////////////////////////////////////
	//
	//  Getters /  Setters
	//
	////////////////////////////////////	

}
