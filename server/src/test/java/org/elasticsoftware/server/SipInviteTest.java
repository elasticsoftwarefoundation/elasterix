/*
 * Copyright 2013 Joost van de Wijgerd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.elasticsoftware.server;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.elasticsoftware.server.actors.User;
import org.elasticsoftware.server.actors.UserAgentClient;
import org.elasticsoftware.sip.codec.SipHeader;
import org.elasticsoftware.sip.codec.SipMethod;
import org.elasticsoftware.sip.codec.SipRequest;
import org.elasticsoftware.sip.codec.SipRequestImpl;
import org.elasticsoftware.sip.codec.SipVersion;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test that creates a test actor system and loads the ElasterixServer<br>
 * 
 * send a sip register message to the Sip Server
 * see http://tools.ietf.org/html/rfc3261#section-10.2
 * 
 * @author Joost van de Wijgerd
 * @author Leonard Wolters
 */
@Test(enabled=true)
public class SipInviteTest extends AbstractSipTest {
	private static final Logger log = Logger.getLogger(SipInviteTest.class);
	
	@Test(enabled = true)
	public void testInviteNonExistingCaller() throws Exception {
		SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.INVITE, "sip:sip.localhost.com:5060");
		req.addHeader(SipHeader.CALL_ID, UUID.randomUUID().toString());
		req.addHeader(SipHeader.CONTACT, "<sip:lwolters@62.163.143.30:49844;transport=UDP;rinstance=6f8dc969b62d1466>");
		req.addHeader(SipHeader.FROM, "\"Leonard Wolters\"<xxx@sip.localhost.com:5060>;tag=6d473a67");
		req.addHeader(SipHeader.TO, "\"Joost vd Wijgerd\"<sip:jwijgerd@sip.localhost.com:5060>");
		setAuthorization(req, "lwolters", "1", md5Encoder.encodePassword("test", null));
		sipServer.sendMessage(req);
		// sleep sometime in order for message to be sent back.
		Thread.sleep(300);
		String message = sipServer.getMessage();
		Assert.assertNotNull(message);
		Assert.assertTrue(message.startsWith("SIP/2.0 404 Not Found"));
	}
	
	@Test(enabled = true)
	public void testInviteNonExistingCallee() throws Exception {
		SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.INVITE, "sip:sip.localhost.com:5060");
		req.addHeader(SipHeader.CALL_ID, UUID.randomUUID().toString());
		req.addHeader(SipHeader.CONTACT, "<sip:lwolters@62.163.143.30:49844;transport=UDP;rinstance=6f8dc969b62d1466>");
		req.addHeader(SipHeader.FROM, "\"Leonard Wolters\"<sip:lwolters@sip.localhost.com:5060>;tag=6d473a67");
		req.addHeader(SipHeader.TO, "\"Joost vd Wijgerd\"<sip:xxx@sip.localhost.com:5060>");
		setAuthorization(req, "lwolters", "1", md5Encoder.encodePassword("test", null));
		sipServer.sendMessage(req);
		// sleep sometime in order for message to be sent back.
		Thread.sleep(300);
		String message = sipServer.getMessage();
		Assert.assertNotNull(message);
		Assert.assertTrue(message.startsWith("SIP/2.0 404 Not Found"));
	}
	
	@Test(enabled = true)
	public void testInviteNoRegistedUAC() throws Exception {
		SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.INVITE, "sip:sip.localhost.com:5060");
		req.addHeader(SipHeader.CALL_ID, UUID.randomUUID().toString());
		req.addHeader(SipHeader.CONTACT, "<sip:lwolters@62.163.143.30:49844;transport=UDP;rinstance=6f8dc969b62d1466>");
		req.addHeader(SipHeader.FROM, "\"Leonard Wolters\"<sip:lwolters@sip.localhost.com:5060>;tag=6d473a67");
		req.addHeader(SipHeader.TO, "\"Joost vd Wijgerd\"<sip:jwijgerd@sip.localhost.com:5060>");
		setAuthorization(req, "lwolters", "1", md5Encoder.encodePassword("test", null));
		sipServer.sendMessage(req);
		// sleep sometime in order for message to be sent back.
		Thread.sleep(300);
		String message = sipServer.getMessage();
		Assert.assertNotNull(message);
		Assert.assertTrue(message.startsWith("SIP/2.0 410 Gone"));
	}
	
	@Test(enabled = true)
	public void testInviteWithoutRegisteredUAC() throws Exception {
		// register piet
		String userId = "piet0";
		String uacId = "uac/aaabbbccc0";
		User.State state = new User.State(userId + "@elasticsoftware.org", userId, 
				md5Encoder.encodePassword("test", null));
		state.addUserAgentClient(uacId, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10));
		actorSystem.actorOf("user/" + userId, User.class, state);
		
		// do not register UAC
		
		// then let leonard invite piet
		SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.INVITE, "sip:sip.localhost.com:5060");
		req.addHeader(SipHeader.CALL_ID, UUID.randomUUID().toString());
		req.addHeader(SipHeader.CONTACT, "<sip:lwolters@127.0.0.1:8989;transport=UDP;rinstance=6f8dc969b62d1466>");
		req.addHeader(SipHeader.FROM, "\"Leonard Wolters\"<sip:lwolters@sip.localhost.com:5060>;tag=6d473a67");
		req.addHeader(SipHeader.TO, "\"Piet\"<sip:" + userId + "@sip.localhost.com:5060>");
		setAuthorization(req, "lwolters", "1", md5Encoder.encodePassword("test", null));
		sipServer.sendMessage(req);
		// sleep sometime in order for message(s) to be sent back. Order of message is not assured
		Thread.sleep(1000);
		String message1 = sipServer.getMessage();
		Assert.assertNotNull(message1);
		String message2 = sipServer.getMessage();
		if(message1.startsWith("SIP/2.0 410 Gone")) {
			Assert.assertTrue(message2.startsWith("SIP/2.0 100 Trying"));
		} else if(message1.startsWith("SIP/2.0 100 Trying")) {
			Assert.assertTrue(message2.startsWith("SIP/2.0 410 Gone"));
		} else {
			log.warn("\n\n\n\n\n\n\n\n\n\n\n" + message1);
			Assert.fail("Wrong messages");
		}
	}
	
	@Test(enabled = true)
	public void testInviteWithRegisteredUAC() throws Exception {
		// register piet
		String userId = "piet1";
		String uacId = "uac/aaabbbccc1";
		User.State state = new User.State(userId + "@elasticsoftware.org", userId, 
				md5Encoder.encodePassword("test", null));
		state.addUserAgentClient(uacId, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10));
		actorSystem.actorOf("user/" + userId, User.class, state);
		
		// register UAC
		// due to multi threading turned off for single tests !@#!@#! 
		// we can't use the local sip client...
		UserAgentClient.State uacState = new UserAgentClient.State(uacId, "127.0.0.1", 9090);
		actorSystem.actorOf(uacId, UserAgentClient.class, uacState);
		
		// then let leonard invite piet
		SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.INVITE, "sip:sip.localhost.com:5060");
		req.addHeader(SipHeader.CALL_ID, UUID.randomUUID().toString());
		req.addHeader(SipHeader.CONTACT, "<sip:lwolters@127.0.0.1:8989;transport=UDP;rinstance=6f8dc969b62d1466>");
		req.addHeader(SipHeader.FROM, "\"Leonard Wolters\"<sip:lwolters@sip.localhost.com:5060>;tag=6d473a67");
		req.addHeader(SipHeader.TO, "\"Piet\"<sip:" + userId +"@sip.localhost.com:5060>");
		setAuthorization(req, "lwolters", "1", md5Encoder.encodePassword("test", null));
		sipServer.sendMessage(req);
		// sleep sometime in order for message(s) to be sent back.
		Thread.sleep(300);
		String message = sipServer.getMessage();
		Assert.assertNotNull(message);
		Assert.assertTrue(message.startsWith("SIP/2.0 100 Trying"));
		// sleep sometime in order for message(s) to be sent back.
		Thread.sleep(5000);		
	}
	
	@Test(enabled = true)
	public void testInviteWithRegisterMessage() throws Exception {
		// first let joost register himself ..
		SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.REGISTER, "sip:sip.localhost.com:5060");
		req.addHeader(SipHeader.CALL_ID, "joost_uac");
		req.addHeader(SipHeader.CONTACT, "<sip:jwijgerd@127.0.0.1:8990;transport=UDP;rinstance=6f8dc969b62d1466>");
		req.addHeader(SipHeader.EXPIRES, "120");
		req.addHeader(SipHeader.FROM, "\"Joost vd Wijgerd\"<sip:jwijgerd@sip.localhost.com:5060>;tag=6d473a67");
		req.addHeader(SipHeader.TO, "\"Joost vd Wijgerd\"<sip:jwijgerd@sip.localhost.com:5060>");
		req.addHeader(SipHeader.VIA, "yyy");
		setAuthorization(req, "jwijgerd", "1", md5Encoder.encodePassword("test", null));
		sipServer.sendMessage(req);
		// sleep sometime in order for message to be sent back.
		Thread.sleep(300);
		sipServer.getMessage(); // call this method in order to remove message..
		
		// then let leonard invite joost
		req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.INVITE, "sip:sip.localhost.com:5060");
		req.addHeader(SipHeader.CALL_ID, UUID.randomUUID().toString());
		req.addHeader(SipHeader.CONTACT, "<sip:lwolters@127.0.0.1:8989;transport=UDP;rinstance=6f8dc969b62d1466>");
		req.addHeader(SipHeader.FROM, "\"Leonard Wolters\"<sip:lwolters@sip.localhost.com:5060>;tag=6d473a67");
		req.addHeader(SipHeader.TO, "\"Joost vd Wijgerd\"<sip:jwijgerd@sip.localhost.com:5060>");
		setAuthorization(req, "lwolters", "1", md5Encoder.encodePassword("test", null));
		sipServer.sendMessage(req);
		// sleep sometime in order for message to be sent back.
		Thread.sleep(300);
		String message = sipServer.getMessage();
		Assert.assertNotNull(message);
		log.info(message);
//		Assert.assertTrue(message.startsWith("SIP/2.0 100 Trying"));
		Thread.sleep(300);
		message = sipServer.getMessage();
		Assert.assertNotNull(message);
		log.info(message);
	}
}