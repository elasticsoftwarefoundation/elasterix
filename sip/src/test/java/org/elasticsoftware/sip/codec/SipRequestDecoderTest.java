package org.elasticsoftware.sip.codec;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.embedder.DecoderEmbedder;
import org.jboss.netty.util.CharsetUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for decoding (incoming) SIP Request
 * 
 * @author Leonard Wolters
 */
public class SipRequestDecoderTest extends AbstractSipTest {
	private static final Logger log = Logger.getLogger(SipRequestDecoderTest.class);
	
	@Test
	public void testInitialLineNoCarriageReturn() throws Exception {
		ChannelBuffer buf = ChannelBuffers.copiedBuffer("bla bla bla", CharsetUtil.UTF_8);
		SipMessageDecoder decoder = new SipMessageDecoder();
		DecoderEmbedder<SipMessage> embedder = new DecoderEmbedder<SipMessage>(decoder);
		embedder.offer(buf);
		
		// initial line is not parsed due to buffer ended?. No createMessage called
		SipMessage message = embedder.poll();
		Assert.assertNotNull(message);
		Assert.assertTrue(decoder.isDecodingRequest());
		Assert.assertTrue(SipResponseStatus.VERSION_NOT_SUPPORTED == message.getResponseStatus());
	}

	@Test
	public void testInitialLineInvalidSipVersion() throws Exception {
		ChannelBuffer buf = ChannelBuffers.copiedBuffer("bla bla bla\n", CharsetUtil.UTF_8);
		SipMessageDecoder decoder = new SipMessageDecoder();
		DecoderEmbedder<SipMessage> embedder = new DecoderEmbedder<SipMessage>(decoder);
		embedder.offer(buf);
		SipMessage message = embedder.poll();
		Assert.assertNotNull(message);
		Assert.assertTrue(SipResponseStatus.VERSION_NOT_SUPPORTED == message.getResponseStatus());
	}
	
	@Test
	public void testInitialLineInvalidSipMethod() throws Exception {
		ChannelBuffer buf = ChannelBuffers.copiedBuffer("bla bla SIP/2.0", CharsetUtil.UTF_8);
		SipMessageDecoder decoder = new SipMessageDecoder();
		DecoderEmbedder<SipMessage> embedder = new DecoderEmbedder<SipMessage>(decoder);
		embedder.offer(buf);
		SipMessage message = embedder.poll();
		Assert.assertNotNull(message);
		Assert.assertTrue(SipResponseStatus.METHOD_NOT_ALLOWED == message.getResponseStatus());
	}
	
	@Test
	public void testInitialLineValid() throws Exception {
		ChannelBuffer buf = ChannelBuffers.copiedBuffer("INVITE sip:bob@biloxi.com SIP/2.0", CharsetUtil.UTF_8);
		SipMessageDecoder decoder = new SipMessageDecoder();
		DecoderEmbedder<SipMessage> embedder = new DecoderEmbedder<SipMessage>(decoder);
		embedder.offer(buf);
		SipMessage message = embedder.poll();
		Assert.assertNotNull(message);
		Assert.assertNull(message.getResponseStatus());
	}
	
	@Test
	public void testInitialLineValidWithPort() throws Exception {
		ChannelBuffer buf = ChannelBuffers.copiedBuffer("INVITE sip:bob@biloxi.com:5060 SIP/2.0", CharsetUtil.UTF_8);
		SipMessageDecoder decoder = new SipMessageDecoder();
		DecoderEmbedder<SipMessage> embedder = new DecoderEmbedder<SipMessage>(decoder);
		embedder.offer(buf);
		SipMessage message = embedder.poll();
		Assert.assertNotNull(message);
		Assert.assertNull(message.getResponseStatus());
	}

	@Test
	public void testInitialLineValidWithoutUsername() throws Exception {
		ChannelBuffer buf = ChannelBuffers.copiedBuffer("INVITE sip:test.biloxi.com SIP/2.0", CharsetUtil.UTF_8);
		SipMessageDecoder decoder = new SipMessageDecoder();
		DecoderEmbedder<SipMessage> embedder = new DecoderEmbedder<SipMessage>(decoder);
		embedder.offer(buf);
		SipMessage message = embedder.poll();
		Assert.assertNotNull(message);
		Assert.assertNull(message.getResponseStatus());
	}

	@Test
	public void testInitialLineValidLocalhostWithoutPort() throws Exception {
		ChannelBuffer buf = ChannelBuffers.copiedBuffer("INVITE sip:localhost SIP/2.0", CharsetUtil.UTF_8);
		SipMessageDecoder decoder = new SipMessageDecoder();
		DecoderEmbedder<SipMessage> embedder = new DecoderEmbedder<SipMessage>(decoder);
		embedder.offer(buf);
		SipMessage message = embedder.poll();
		Assert.assertNotNull(message);
		Assert.assertNull(message.getResponseStatus());
	}

	@Test
	public void testInitialLineValidLocalhostWithPort() throws Exception {
		ChannelBuffer buf = ChannelBuffers.copiedBuffer("INVITE sip:127.0.0.1:5060 SIP/2.0", CharsetUtil.UTF_8);
		SipMessageDecoder decoder = new SipMessageDecoder();
		DecoderEmbedder<SipMessage> embedder = new DecoderEmbedder<SipMessage>(decoder);
		embedder.offer(buf);
		SipMessage message = embedder.poll();
		Assert.assertNotNull(message);
		Assert.assertNull(message.getResponseStatus());
	}
	
	@Test
	public void testInitialLineValidWithoutUsernameWithPort() throws Exception {
		ChannelBuffer buf = ChannelBuffers.copiedBuffer("INVITE sip:test.biloxi.com:5060 SIP/2.0", CharsetUtil.UTF_8);
		SipMessageDecoder decoder = new SipMessageDecoder();
		DecoderEmbedder<SipMessage> embedder = new DecoderEmbedder<SipMessage>(decoder);
		embedder.offer(buf);
		SipMessage message = embedder.poll();
		Assert.assertNotNull(message);
		Assert.assertNull(message.getResponseStatus());
	}

	@Test
	public void testInitialLineValidWithoutUsernameWithPortAndTransport() throws Exception {
		ChannelBuffer buf = ChannelBuffers.copiedBuffer("SUBSCRIBE sip:1234@localhost:5060;transport=UDP SIP/2.0", CharsetUtil.UTF_8);
		SipMessageDecoder decoder = new SipMessageDecoder();
		DecoderEmbedder<SipMessage> embedder = new DecoderEmbedder<SipMessage>(decoder);
		embedder.offer(buf);
		SipMessage message = embedder.poll();
		Assert.assertNotNull(message);
		Assert.assertNull(message.getResponseStatus());
	}
	
	@Test
	public void testInitialLineInvalidWithoutUsernameWithPortAndTransport() throws Exception {
		ChannelBuffer buf = ChannelBuffers.copiedBuffer("SUBSCRIBE sip:1234@localhost:5060;transport= SIP/2.0", CharsetUtil.UTF_8);
		SipMessageDecoder decoder = new SipMessageDecoder();
		DecoderEmbedder<SipMessage> embedder = new DecoderEmbedder<SipMessage>(decoder);
		embedder.offer(buf);
		SipMessage message = embedder.poll();
		Assert.assertNotNull(message);
		Assert.assertTrue(SipResponseStatus.BAD_REQUEST == message.getResponseStatus());
	}
	
	@Test
	public void testInitialLineAsterisk() throws Exception {
		ChannelBuffer buf = ChannelBuffers.copiedBuffer("INVITE sip:sip.outerteams.com:5060 SIP/2.0", CharsetUtil.UTF_8);
		SipMessageDecoder decoder = new SipMessageDecoder();
		DecoderEmbedder<SipMessage> embedder = new DecoderEmbedder<SipMessage>(decoder);
		embedder.offer(buf);
		SipMessage message = embedder.poll();
		Assert.assertNotNull(message);
		Assert.assertNull(message.getResponseStatus());
	}

	@Test
	public void testIncorrectContentLength() throws Exception {
		ChannelBuffer buf = createChannelFromFile("alice_invite_bob.txt", 10);
		SipMessageDecoder decoder = new SipMessageDecoder();
		DecoderEmbedder<SipMessage> embedder = new DecoderEmbedder<SipMessage>(decoder);
		embedder.offer(buf);
		SipMessage message = embedder.poll();
		// message should be null...
		Assert.assertNull(message);
	}
	
	@Test
	public void testValidMessage() throws Exception {
		// add more content in order for content length to be OK
		ChannelBuffer buf = createChannelFromFile("alice_invite_bob.txt");
		SipMessageDecoder decoder = new SipMessageDecoder();
		DecoderEmbedder<SipMessage> embedder = new DecoderEmbedder<SipMessage>(decoder);
		embedder.offer(buf);
		SipMessage message = embedder.poll();
		Assert.assertNotNull(message);
		Assert.assertNull(message.getResponseStatus());
	}
}
