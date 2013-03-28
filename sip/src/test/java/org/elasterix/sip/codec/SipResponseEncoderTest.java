package org.elasterix.sip.codec;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.embedder.EncoderEmbedder;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Tests for encoding (outgoing) SIP Request
 * 
 * http://biasedbit.com/netty-tutorial-replaying-decoder/
 * 
 * @author Leonard Wolters
 */
public class SipResponseEncoderTest {
	private static final Logger log = Logger.getLogger(SipResponseEncoderTest.class);
	
	@BeforeTest
	public void init() {
		Logger.getLogger("org.elasterix").setLevel(Level.DEBUG);
	}
	
	@Test
	public void testValidMessage() throws Exception {
		
		SipMessage msg = new SipMessageImpl(SipResponseStatus.BAD_REQUEST);
		
		SipResponseEncoder encoder = new SipResponseEncoder();
		EncoderEmbedder<ChannelBuffer> embedder = new EncoderEmbedder<ChannelBuffer>(encoder);
		embedder.offer(msg);
		
		ChannelBuffer buffer = embedder.poll();
		Assert.assertEquals("SIP/2.0 400 Bad Request", buffer.toString(SipResponseEncoder.charSet).trim());
	}
}
