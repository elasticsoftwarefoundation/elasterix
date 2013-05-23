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

import org.apache.log4j.Logger;
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
 * test all different kinds of <code>Sip Register</code> messages 
 * see http://tools.ietf.org/html/rfc3261#section-10.2
 * 
 * @author Joost van de Wijgerd
 * @author Leonard Wolters
 */
public class SipRegisterTest extends AbstractSipTest {
	private static final Logger log = Logger.getLogger(SipRegisterTest.class);
		
	@Test(enabled = true)
	public void testRegisterUserNoFrom() throws Exception {
		SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.REGISTER, "sip:sip.localhost.com:5060");
		sipServer.sendMessage(req);
		// sleep sometime in order for message to be sent back.
		Thread.sleep(300);
		String message = sipServer.getMessage();
		Assert.assertNotNull(message);
		Assert.assertTrue(message.startsWith("SIP/2.0 400 Bad Request"));
	}

	@Test(enabled = true)
	public void testRegisterUserNoContact() throws Exception {
		SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.REGISTER, "sip:sip.localhost.com:5060");
		req.addHeader(SipHeader.FROM, "\"Leonard Wolters\"<sip:lwolters@sip.localhost.com:5060>");
		sipServer.sendMessage(req);
		// sleep sometime in order for message to be sent back.
		Thread.sleep(300);
		String message = sipServer.getMessage();
		Assert.assertNotNull(message);
		Assert.assertTrue(message.startsWith("SIP/2.0 400 Bad Request"));
	}

	@Test(enabled = true)
	public void testRegisterUserNoCallId() throws Exception {
		SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.REGISTER, "sip:sip.localhost.com:5060");
		req.addHeader(SipHeader.FROM, "\"Leonard Wolters\"<sip:lwolters@sip.localhost.com:5060>");
		req.addHeader(SipHeader.CONTACT, "<sip:124@62.163.143.30:60236;transport=UDP;rinstance=e6768ab86fdcf0b4>");
		setAuthorization(req, "lwolters", "-1", md5Encoder.encodePassword("test", null));
		sipServer.sendMessage(req);
		// sleep sometime in order for message to be sent back.
		Thread.sleep(300);
		String message = sipServer.getMessage();
		Assert.assertNotNull(message);
		log.info(message);
		Assert.assertTrue(message.startsWith("SIP/2.0 400 Bad Request"));
	}
	
	@Test(enabled = true)
	public void testRegisterUserNoVia() throws Exception {
		SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.REGISTER, "sip:sip.localhost.com:5060");
		req.addHeader(SipHeader.CALL_ID, UUID.randomUUID().toString());
		req.addHeader(SipHeader.FROM, "\"Leonard Wolters\"<sip:lwolters@sip.localhost.com:5060>");
		req.addHeader(SipHeader.CONTACT, "<sip:124@62.163.143.30:60236;transport=UDP;rinstance=e6768ab86fdcf0b4>");
		setAuthorization(req, "lwolters", "-1", md5Encoder.encodePassword("test", null));
		sipServer.sendMessage(req);
		// sleep sometime in order for message to be sent back.
		Thread.sleep(300);
		String message = sipServer.getMessage();
		Assert.assertNotNull(message);
		Assert.assertTrue(message.startsWith("SIP/2.0 400 Bad Request"));
	}
	
	@Test(enabled = true)
	public void testRegisterNonExistingUser() throws Exception {
		SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.REGISTER, "sip:sip.localhost.com:5060");
		req.addHeader(SipHeader.CALL_ID, UUID.randomUUID().toString());
		req.addHeader(SipHeader.CONTACT, "<sip:124@62.163.143.30:60236;transport=UDP;rinstance=e6768ab86fdcf0b4>");
		req.addHeader(SipHeader.FROM, "\"Leonard Wolters\"<sip:XXXXXX@sip.localhost.com:5060>");
		req.addHeader(SipHeader.VIA, "yyy");
		sipServer.sendMessage(req);
		// sleep sometime in order for message to be sent back.
		Thread.sleep(300);
		String message = sipServer.getMessage();
		Assert.assertNotNull(message);
		Assert.assertTrue(message.startsWith("SIP/2.0 404 Not Found"));
	}

	@Test(enabled = true)
	public void testRegisterUserNoAuthentication() throws Exception {
		SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.REGISTER, "sip:sip.localhost.com:5060");
		req.addHeader(SipHeader.CALL_ID, UUID.randomUUID().toString());
		req.addHeader(SipHeader.CONTACT, "<sip:124@62.163.143.30:60236;transport=UDP;rinstance=e6768ab86fdcf0b4>");
		req.addHeader(SipHeader.FROM, "\"Leonard Wolters\"<sip:lwolters@sip.localhost.com:5060>");
		req.addHeader(SipHeader.VIA, "yyy");
		sipServer.sendMessage(req);
		// sleep sometime in order for message to be sent back.
		Thread.sleep(300);
		String message = sipServer.getMessage();
		Assert.assertNotNull(message);
		Assert.assertTrue(message.startsWith("SIP/2.0 401 Unauthorized"));
		Assert.assertTrue(message.indexOf("WWW-Authenticate: Digest algorithm=MD5, realm=") != -1);
	}
	
	@Test(enabled = true)
	public void testRegisterUserWrongUsername() throws Exception {
		SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.REGISTER, "sip:sip.localhost.com:5060");
		req.addHeader(SipHeader.CALL_ID, UUID.randomUUID().toString());
		req.addHeader(SipHeader.CONTACT, "<sip:124@62.163.143.30:60236;transport=UDP;rinstance=e6768ab86fdcf0b4>");
		req.addHeader(SipHeader.FROM, "\"Leonard Wolters\"<sip:lwolters@sip.localhost.com:5060>");
		req.addHeader(SipHeader.VIA, "yyy");
		setAuthorization(req, "jwijgerd", "", "");
		sipServer.sendMessage(req);
		// sleep sometime in order for message to be sent back.
		Thread.sleep(300);
		String message = sipServer.getMessage();
		Assert.assertNotNull(message);
		Assert.assertTrue(message.startsWith("SIP/2.0 401 Unauthorized"));
		Assert.assertTrue(message.indexOf("WWW-Authenticate: Digest algorithm=MD5, realm=") != -1);
	}
	
	@Test(enabled = true)
	public void testRegisterUserWrongNonce() throws Exception {
		SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.REGISTER, "sip:sip.localhost.com:5060");
		req.addHeader(SipHeader.CALL_ID, UUID.randomUUID().toString());
		req.addHeader(SipHeader.CONTACT, "<sip:124@62.163.143.30:60236;transport=UDP;rinstance=e6768ab86fdcf0b4>");
		req.addHeader(SipHeader.FROM, "\"Leonard Wolters\"<sip:lwolters@sip.localhost.com:5060>");
		req.addHeader(SipHeader.VIA, "yyy");
		setAuthorization(req, "lwolters", "123456789", "");
		sipServer.sendMessage(req);
		// sleep sometime in order for message to be sent back.
		Thread.sleep(300);
		String message = sipServer.getMessage();
		Assert.assertNotNull(message);
		Assert.assertTrue(message.startsWith("SIP/2.0 401 Unauthorized"));
		Assert.assertTrue(message.indexOf("WWW-Authenticate: Digest algorithm=MD5, realm=") != -1);
	}
	
	@Test(enabled = true)
	public void testRegisterUserWrongHash() throws Exception {
		SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.REGISTER, "sip:sip.localhost.com:5060");
		req.addHeader(SipHeader.CALL_ID, UUID.randomUUID().toString());
		req.addHeader(SipHeader.CONTACT, "<sip:124@62.163.143.30:60236;transport=UDP;rinstance=e6768ab86fdcf0b4>");
		req.addHeader(SipHeader.FROM, "\"Leonard Wolters\"<sip:lwolters@sip.localhost.com:5060>");
		req.addHeader(SipHeader.VIA, "yyy");
		setAuthorization(req, "lwolters", "-1", "");
		sipServer.sendMessage(req);
		// sleep sometime in order for message to be sent back.
		Thread.sleep(300);
		String message = sipServer.getMessage();
		Assert.assertNotNull(message);
		Assert.assertTrue(message.startsWith("SIP/2.0 401 Unauthorized"));
	}
	
	@Test(enabled = true)
	public void testRegisterUserWrongCSeqCount() throws Exception {
		SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.REGISTER, "sip:sip.localhost.com:5060");
		req.addHeader(SipHeader.CALL_ID, UUID.randomUUID().toString());
		req.addHeader(SipHeader.CONTACT, "<sip:124@62.163.143.30:60236;transport=UDP;rinstance=e6768ab86fdcf0b4>");
		req.addHeader(SipHeader.CSEQ, "2 REGISTER");
		req.addHeader(SipHeader.FROM, "\"Leonard Wolters\"<sip:lwolters@sip.localhost.com:5060>");
		req.addHeader(SipHeader.VIA, "yyy");
		setAuthorization(req, "lwolters", "-1", "");
		sipServer.sendMessage(req);
		// sleep sometime in order for message to be sent back.
		Thread.sleep(300);
		String message = sipServer.getMessage();
		Assert.assertNotNull(message);
		Assert.assertTrue(message.startsWith("SIP/2.0 401 Unauthorized"));
	}

	@Test(enabled = true)
	public void testRegisterUserWrongCSeqMethod() throws Exception {
		SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.REGISTER, "sip:sip.localhost.com:5060");
		req.addHeader(SipHeader.CALL_ID, UUID.randomUUID().toString());
		req.addHeader(SipHeader.CONTACT, "<sip:124@62.163.143.30:60236;transport=UDP;rinstance=e6768ab86fdcf0b4>");
		req.addHeader(SipHeader.CSEQ, "1 INVITE");
		req.addHeader(SipHeader.FROM, "\"Leonard Wolters\"<sip:lwolters@sip.localhost.com:5060>");
		req.addHeader(SipHeader.VIA, "yyy");
		setAuthorization(req, "lwolters", "-1", "");
		sipServer.sendMessage(req);
		// sleep sometime in order for message to be sent back.
		Thread.sleep(300);
		String message = sipServer.getMessage();
		Assert.assertNotNull(message);
		Assert.assertTrue(message.startsWith("SIP/2.0 401 Unauthorized"));
	}

	@Test(enabled = true)
	public void testRegisterUserRealNonce() throws Exception {
		SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.REGISTER, "sip:sip.localhost.com:5060");
		req.addHeader(SipHeader.CALL_ID, UUID.randomUUID().toString());
		req.addHeader(SipHeader.CONTACT, "<sip:124@62.163.143.30:60236;transport=UDP;rinstance=e6768ab86fdcf0b4>");
		req.addHeader(SipHeader.FROM, "\"Leonard Wolters\"<sip:lwolters@sip.localhost.com:5060>");
		req.addHeader(SipHeader.VIA, "yyy");
		sipServer.sendMessage(req);
		// sleep sometime in order for message to be sent back.
		Thread.sleep(300);
		// get nonce!
		String message = sipServer.getMessage();
		int idx = message.indexOf("nonce=") + 7;
		String nonce = message.substring(idx, message.indexOf('\"', idx));
		setAuthorization(req, "lwolters", nonce, "");
		sipServer.sendMessage(req);
		// sleep sometime in order for message to be sent back.
		Thread.sleep(300);
		message = sipServer.getMessage();
		Assert.assertNotNull(message);
		Assert.assertTrue(message.startsWith("SIP/2.0 401 Unauthorized"));
	}

	@Test(enabled = true)
	public void testRegisterUserOk() throws Exception {
		SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.REGISTER, "sip:sip.localhost.com:5060");
		req.addHeader(SipHeader.CALL_ID, UUID.randomUUID().toString());
		req.addHeader(SipHeader.CONTACT, "<sip:124@62.163.143.30:60236;transport=UDP;rinstance=e6768ab86fdcf0b4>");
		req.addHeader(SipHeader.FROM, "\"Leonard Wolters\"<sip:lwolters@sip.localhost.com:5060>");
		req.addHeader(SipHeader.VIA, "yyy");
		sipServer.sendMessage(req);
		// sleep sometime in order for message to be sent back.
		Thread.sleep(300);
		// get nonce!
		String message = sipServer.getMessage();
		int idx = message.indexOf("nonce=") + 7;
		String nonce = message.substring(idx, message.indexOf('\"', idx));
		setAuthorization(req, "lwolters", nonce, md5Encoder.encodePassword("test", null));
		sipServer.sendMessage(req);
		// sleep sometime in order for message to be sent back.
		Thread.sleep(300);
		message = sipServer.getMessage();
		Assert.assertNotNull(message);
		Assert.assertTrue(message.startsWith("SIP/2.0 200 OK"));
	}
}

