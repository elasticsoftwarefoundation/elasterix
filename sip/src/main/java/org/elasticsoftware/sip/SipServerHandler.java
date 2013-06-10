package org.elasticsoftware.sip;

import org.apache.log4j.Logger;
import org.elasticsoftware.sip.codec.*;
import org.jboss.netty.channel.*;
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
    private static final Logger sipLog = Logger.getLogger("sip");

    private SipMessageHandler messageHandler;
    private SipChannelFactory sipChannelFactory;
    private boolean strictParsing = true;

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {
        SipMessage message = (SipMessage) e.getMessage();
        logMessage("RECEIVED", message);

        // update LRU cache (if set)
        if (sipChannelFactory != null) {
            // we need to check if the key used for caching this channel is
            // present. If no key is found, bounce message directly back
            // to sender (whilst we still have this channel)
            if (message instanceof SipRequest) {
                sipChannelFactory.setChannel(message.getSipUser(SipHeader.CONTACT), ctx.getChannel());
            } else if (message instanceof SipResponse) {
                sipChannelFactory.setChannel(message.getSipUser(SipHeader.CONTACT), ctx.getChannel());
            }
        }

        // only approved sip messages are sent along. Syntactically wrong
        // messages are immediately returned with appropriate response status

        // delegate action to handler
        if (message instanceof SipRequest) {
            SipRequest request = (SipRequest) message;

            if (strictParsing) {
                switch (request.getMethod()) {
                    case INVITE:
                        if (!checkForHeaders(request, ctx.getChannel(), SipHeader.FROM, SipHeader.TO)) return;
                        break;
                    case REGISTER:
                        if (!checkForHeaders(request, ctx.getChannel(), SipHeader.CALL_ID,
                                SipHeader.CONTACT, SipHeader.FROM, SipHeader.VIA)) return;
                        break;
                }
            }

            messageHandler.onRequest(request);
        } else if (message instanceof SipResponse) {
            SipResponse response = (SipResponse) message;
            messageHandler.onResponse(response);
        }
    }

    private boolean checkForHeaders(SipRequest request, Channel channel, SipHeader... headers) {
        if (!strictParsing) return true;
        for (SipHeader header : headers) {
            if (!StringUtils.hasLength(request.getHeaderValue(header))) {
                log.warn(String.format("No %s header found in SIP message. Bouncing it",
                        header.getName()));
                request.setResponseStatus(SipResponseStatus.BAD_REQUEST);
                logMessage("SENDING", request);
                channel.write(request.toSipResponse());
                return false;
            }
        }
        return true;
    }

    private void logMessage(String prefix, SipMessage message) {
        if (sipLog.isDebugEnabled()) {
            sipLog.debug(String.format("%s\n%s", prefix, message));
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s\n%s", prefix, message));
        }
    }

//	private void writeResponse(SipResponse message, MessageEvent e) {
//		// Decide whether to close the connection or not.		
//		//boolean keepAlive = SipHeaders.isKeepAlive(request);
//		boolean keepAlive = true;
//
//		// Build the response object.
//		//response.setContent(ChannelBuffers.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));
//		message.setHeader(SipHeader.CONTENT_TYPE, "text/plain; charset=UTF-8");
//
//		if (keepAlive) {
//			// Add 'Content-Length' header only for a keep-alive connection.
//			message.setHeader(SipHeader.CONTENT_LENGTH, message.getContent().readableBytes());
//		}
//		
//    	logMessage("Sending", message);
//
//		// Write the response.
//		ChannelFuture future = e.getChannel().write(message);
//
//		// Close the non-keep-alive connection after the write operation is done.
//		if (!keepAlive) {
//			future.addListener(ChannelFutureListener.CLOSE);
//		}
//	}

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
