package org.elasterix.sip.codec;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.embedder.EncoderEmbedder;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for encoding (outgoing) SIP Request
 * 
 * http://biasedbit.com/netty-tutorial-replaying-decoder/
 * 
 * @author Leonard Wolters
 */
public class SipResponseEncoderTest extends AbstractSipTest {
	private static final Logger log = Logger.getLogger(SipResponseEncoderTest.class);
	
	@Test
	public void testMessageBadRequest() throws Exception {
		
		SipMessage msg = new SipMessageImpl(SipResponseStatus.BAD_REQUEST);
		
		SipResponseEncoder encoder = new SipResponseEncoder();
		EncoderEmbedder<ChannelBuffer> embedder = new EncoderEmbedder<ChannelBuffer>(encoder);
		embedder.offer(msg);
		
		ChannelBuffer buffer = embedder.poll();
		Assert.assertEquals("SIP/2.0 400 Bad Request", buffer.toString(SipResponseEncoder.charSet).trim());
	}
	
	@Test
	public void testHeaders() throws Exception {
		
		SipMessage msg = new SipMessageImpl(SipResponseStatus.OK);
		msg.addHeader(SipHeader.VIA, "SIP/2.0/UDP server10.biloxi.com\n ;branch=z9hG4bKnashds8;received=192.0.2.3");
		msg.addHeader(SipHeader.VIA, "SIP/2.0/UDP bigbox3.site3.atlanta.com\n ;branch=z9hG4bK77ef4c2312983.1;received=192.0.2.2");
		msg.addHeader(SipHeader.VIA, "SIP/2.0/UDP pc33.atlanta.com\n ;branch=z9hG4bK776asdhds ;received=192.0.2.1");
		msg.addHeader(SipHeader.TO, "Bob <sip:bob@biloxi.com>;tag=a6c85cf");
		msg.addHeader(SipHeader.FROM, "Alice <sip:alice@atlanta.com>;tag=1928301774");
		msg.addHeader(SipHeader.CALL_ID, "a84b4c76e66710@pc33.atlanta.com");
		msg.addHeader(SipHeader.CSEQ, "314159 INVITE");
		msg.addHeader(SipHeader.CONTACT, "<sip:bob@192.0.2.4>");
		msg.addHeader(SipHeader.CONTENT_TYPE, "application/sdp");
		msg.addHeader(SipHeader.CONTENT_LENGTH, "131");
		
		SipResponseEncoder encoder = new SipResponseEncoder();
		EncoderEmbedder<ChannelBuffer> embedder = new EncoderEmbedder<ChannelBuffer>(encoder);
		embedder.offer(msg);
	
		ChannelBuffer buffer = embedder.poll();
		String content = prepare(getFileContent("bob_ack_invite.txt", SipResponseEncoder.charSet).trim());
		String generated = prepare((buffer.toString(SipResponseEncoder.charSet).trim()));
		Assert.assertEquals(content, generated);
	}
}
