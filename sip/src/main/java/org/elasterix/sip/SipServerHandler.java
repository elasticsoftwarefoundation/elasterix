package org.elasterix.sip;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

import org.apache.log4j.Logger;
import org.elasterix.sip.codec.SipMethod;
import org.elasterix.sip.codec.SipRequest;
import org.elasterix.sip.codec.SipResponse;
import org.elasterix.sip.codec.SipResponseImpl;
import org.elasterix.sip.codec.SipResponseStatus;
import org.elasterix.sip.codec.SipVersion;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;

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
	
	@Autowired
	private SipMessageHandler messageHandler;

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) 
	throws Exception {
		SipRequest request = (SipRequest) e.getMessage();
		
		// delegate action to handler
		if(SipMethod.ACK == request.getMethod()) {
			messageHandler.onAck(request);
		} else if(SipMethod.BYE == request.getMethod()) {
			messageHandler.onBye(request);
		} else if(SipMethod.CANCEL == request.getMethod()) {
			messageHandler.onCancel(request);
		} else if(SipMethod.INVITE == request.getMethod()) {
			messageHandler.onInvite(request);
		} else if(SipMethod.OPTIONS == request.getMethod()) {
			messageHandler.onOptions(request);
		} else if(SipMethod.REGISTER == request.getMethod()) {
			messageHandler.onRegister(request);
		}
		
		// writing reponse (indicating message is received accordingly!)
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
