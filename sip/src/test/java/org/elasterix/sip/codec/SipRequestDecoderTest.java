package org.elasterix.sip.codec;

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
	
	@Test
	public void testInitialLineNoCarriageReturn() throws Exception {
		ChannelBuffer buf = ChannelBuffers.copiedBuffer("bla bla bla", CharsetUtil.UTF_8);
		SipRequestDecoder decoder = new SipRequestDecoder();
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
		SipRequestDecoder decoder = new SipRequestDecoder();
		DecoderEmbedder<SipMessage> embedder = new DecoderEmbedder<SipMessage>(decoder);
		embedder.offer(buf);
		SipMessage message = embedder.poll();
		Assert.assertNotNull(message);
		Assert.assertTrue(SipResponseStatus.VERSION_NOT_SUPPORTED == message.getResponseStatus());
	}
	
	@Test
	public void testInitialLineInvalidSipMethod() throws Exception {
		ChannelBuffer buf = ChannelBuffers.copiedBuffer("bla bla SIP/2.0", CharsetUtil.UTF_8);
		SipRequestDecoder decoder = new SipRequestDecoder();
		DecoderEmbedder<SipMessage> embedder = new DecoderEmbedder<SipMessage>(decoder);
		embedder.offer(buf);
		SipMessage message = embedder.poll();
		Assert.assertNotNull(message);
		Assert.assertTrue(SipResponseStatus.METHOD_NOT_ALLOWED == message.getResponseStatus());
	}
	
	@Test
	public void testInitialLineInvalidUri() throws Exception {
		ChannelBuffer buf = ChannelBuffers.copiedBuffer("INVITE bla SIP/2.0", CharsetUtil.UTF_8);
		SipRequestDecoder decoder = new SipRequestDecoder();
		DecoderEmbedder<SipMessage> embedder = new DecoderEmbedder<SipMessage>(decoder);
		embedder.offer(buf);
		SipMessage message = embedder.poll();
		Assert.assertNotNull(message);
		Assert.assertTrue(SipResponseStatus.BAD_REQUEST == message.getResponseStatus());
	}
	
	@Test
	public void testInitialLineValid() throws Exception {
		ChannelBuffer buf = ChannelBuffers.copiedBuffer("INVITE sip:bob@biloxi.com SIP/2.0", CharsetUtil.UTF_8);
		SipRequestDecoder decoder = new SipRequestDecoder();
		DecoderEmbedder<SipMessage> embedder = new DecoderEmbedder<SipMessage>(decoder);
		embedder.offer(buf);
		SipMessage message = embedder.poll();
		Assert.assertNotNull(message);
		Assert.assertNull(message.getResponseStatus());
	}
	
	@Test
	public void testInitialLineValidWithPort() throws Exception {
		ChannelBuffer buf = ChannelBuffers.copiedBuffer("INVITE sip:bob@biloxi.com:5060 SIP/2.0", CharsetUtil.UTF_8);
		SipRequestDecoder decoder = new SipRequestDecoder();
		DecoderEmbedder<SipMessage> embedder = new DecoderEmbedder<SipMessage>(decoder);
		embedder.offer(buf);
		SipMessage message = embedder.poll();
		Assert.assertNotNull(message);
		Assert.assertNull(message.getResponseStatus());
	}

	@Test
	public void testInitialLineValidWithoutUsername() throws Exception {
		ChannelBuffer buf = ChannelBuffers.copiedBuffer("INVITE sip:test.biloxi.com SIP/2.0", CharsetUtil.UTF_8);
		SipRequestDecoder decoder = new SipRequestDecoder();
		DecoderEmbedder<SipMessage> embedder = new DecoderEmbedder<SipMessage>(decoder);
		embedder.offer(buf);
		SipMessage message = embedder.poll();
		Assert.assertNotNull(message);
		Assert.assertNull(message.getResponseStatus());
	}
	
	@Test
	public void testInitialLineValidWithoutUsernameWithPort() throws Exception {
		ChannelBuffer buf = ChannelBuffers.copiedBuffer("INVITE sip:test.biloxi.com:5060 SIP/2.0", CharsetUtil.UTF_8);
		SipRequestDecoder decoder = new SipRequestDecoder();
		DecoderEmbedder<SipMessage> embedder = new DecoderEmbedder<SipMessage>(decoder);
		embedder.offer(buf);
		SipMessage message = embedder.poll();
		Assert.assertNotNull(message);
		Assert.assertNull(message.getResponseStatus());
	}
	
	@Test
	public void testInitialLineAsterisk() throws Exception {
		ChannelBuffer buf = ChannelBuffers.copiedBuffer("INVITE sip:sip.outerteams.com:5060 SIP/2.0", CharsetUtil.UTF_8);
		SipRequestDecoder decoder = new SipRequestDecoder();
		DecoderEmbedder<SipMessage> embedder = new DecoderEmbedder<SipMessage>(decoder);
		embedder.offer(buf);
		SipMessage message = embedder.poll();
		Assert.assertNotNull(message);
		Assert.assertNull(message.getResponseStatus());
	}

	@Test
	public void testIncorrectContentLength() throws Exception {
		ChannelBuffer buf = createChannelFromFile("alice_invite_bob.txt", 10);
		SipRequestDecoder decoder = new SipRequestDecoder();
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
		SipRequestDecoder decoder = new SipRequestDecoder();
		DecoderEmbedder<SipMessage> embedder = new DecoderEmbedder<SipMessage>(decoder);
		embedder.offer(buf);
		SipMessage message = embedder.poll();
		Assert.assertNotNull(message);
		Assert.assertNull(message.getResponseStatus());
	}
}
