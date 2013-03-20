package org.elasterix.sip.codec;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Tests for decoder (incoming) SIP Request
 * 
 * @author Leonard Wolters
 */
public class SipRequestDecoderTest {
	@Mock ChannelHandlerContext context;
	@Mock ChannelEvent event;
	//@InjectMocks private SipRequestDecoder decoder = new SipRequestDecoder();
	
	@BeforeTest
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testInviteMessage() throws Exception {
		//SipServerCodec codec = new SipServerCodec();
		//codec.handleDownstream(ctx, e)
		
		//ChannelHandlerContext context = new DummyChannelHandlerContext();
		//ChannelEvent event = new DummyChannelEvent();
		
		//
		
		SipRequestDecoder decoder = new SipRequestDecoder();
		decoder.handleUpstream(context, event);
		throw new Exception("");
	}
}
