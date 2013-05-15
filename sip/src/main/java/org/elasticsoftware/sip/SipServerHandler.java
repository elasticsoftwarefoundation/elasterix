package org.elasticsoftware.sip;

import org.apache.log4j.Logger;
import org.elasticsoftware.sip.codec.SipHeader;
import org.elasticsoftware.sip.codec.SipMessage;
import org.elasticsoftware.sip.codec.SipRequest;
import org.elasticsoftware.sip.codec.SipResponseStatus;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.StringUtils;

/**
 * Sip Server Handler<br>
 * <br>
 * This handler takes care of all incoming SIP messages sent by the SipServer 
 * and must sent back corresponding SIP responses, indicating the state of
 * the message<br>
 * 
 * @author Leonard Wolters
 */
public class SipServerHandler extends SimpleChannelUpstreamHandler {
	private static final Logger log = Logger.getLogger(SipServerHandler.class);	

	private SipMessageHandler messageHandler;
	private SipChannelFactory sipChannelFactory;
	private boolean strictParsing = true;

    @Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) 
	throws Exception {
		SipRequest request = (SipRequest) e.getMessage();
		if(log.isDebugEnabled()) {
			log.debug(String.format("messageReceived\n%s", request));
		}
		
		// update LRU cache (if set)
		if(sipChannelFactory != null) {
			// we need to check if the key used for caching this channel is 
			// present. If no key is found, bounce message directly back
			// to sender (whilst we still have this channel)
			if(strictParsing && !checkForHeaders(request, ctx.getChannel(), SipHeader.FROM)) return;
			sipChannelFactory.setChannel(request.getHeaderValue(SipHeader.FROM), 
					ctx.getChannel());
		}

		// only approved sip messages are sent along. Syntactically wrong
		// messages are immediately returned with appropriate response status
		
		// delegate action to handler
		switch(request.getMethod()) {
		case ACK:
			messageHandler.onAck(request);
			break;
		case BYE:
			messageHandler.onBye(request);
			break;
		case CANCEL:
			messageHandler.onCancel(request);
			break;
		case INVITE:
			if(strictParsing && !checkForHeaders(request, ctx.getChannel(), SipHeader.FROM, SipHeader.TO)) return;
			messageHandler.onInvite(request);
			break;
		case OPTIONS:
			messageHandler.onInvite(request);
			break;
		case REGISTER:
			if(strictParsing && !checkForHeaders(request, ctx.getChannel(), SipHeader.CALL_ID, 
					SipHeader.CONTACT, SipHeader.FROM, SipHeader.VIA)) return;
			messageHandler.onRegister(request);
			break;
		default:
			log.error(String.format("Unrecognized method[%s]", 
					request.getMethod().name()));
			writeResponse(request, e, SipResponseStatus.NOT_IMPLEMENTED);
			return;
		}
		
		// Response are not written here. The implementation of the message handler
		// should sent/write the response to this request (using the 
		// SipMessageSender), not this method. (except when exception occurs)
	}
    
    private boolean checkForHeaders(SipRequest request, Channel channel, SipHeader... headers) {
    	if(!strictParsing) return true;
    	for(SipHeader header : headers) {
	    	if(!StringUtils.hasLength(request.getHeaderValue(header))) {
				log.warn(String.format("No %s header found in SIP message. Bouncing it",
						header.getName()));
				request.setResponseStatus(SipResponseStatus.BAD_REQUEST);
				channel.write(request);
				return false;
			}
    	}
    	return true;
    }

	private void writeResponse(SipMessage message, MessageEvent e, SipResponseStatus status) {
		// Decide whether to close the connection or not.		
		//boolean keepAlive = SipHeaders.isKeepAlive(request);
		boolean keepAlive = true;

		// Build the response object.
		//response.setContent(ChannelBuffers.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));
		message.setHeader(SipHeader.CONTENT_TYPE, "text/plain; charset=UTF-8");

		if (keepAlive) {
			// Add 'Content-Length' header only for a keep-alive connection.
			message.setHeader(SipHeader.CONTENT_LENGTH, message.getContent().readableBytes());
		}

		// Write the response.
		ChannelFuture future = e.getChannel().write(message);

		// Close the non-keep-alive connection after the write operation is done.
		if (!keepAlive) {
			future.addListener(ChannelFutureListener.CLOSE);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		log.error(String.format("exceptionCaught: [%s] -> [%s]", 
				e.getCause().getMessage(), e.getCause().getCause()), e.getCause());
		e.getChannel().close();
	}	

	////////////////////////////////////
	//
	//  Getters /  Setters
	//
	////////////////////////////////////	


	@Required
    public void setMessageHandler(SipMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }
	
    public void setSipChannelFactory(SipChannelFactory sipChannelFactory) {
        this.sipChannelFactory = sipChannelFactory;
    }
}
