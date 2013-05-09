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

import org.apache.log4j.Logger;
import org.elasticsoftware.sip.codec.SipHeader;
import org.elasticsoftware.sip.codec.SipMethod;
import org.elasticsoftware.sip.codec.SipRequest;
import org.elasticsoftware.sip.codec.SipVersion;
import org.elasticsoftware.sip.codec.impl.SipRequestImpl;
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
public class SipInviteTest extends AbstractSipTest {
	private static final Logger log = Logger.getLogger(SipInviteTest.class);
	private static final boolean testEnabled = true;
	
	@Test(enabled = testEnabled)
	public void testInvite() throws Exception {
		
//		SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.INVITE, "sip:sip.localhost.com:5060");
//		req.addHeader(SipHeader.CALL_ID, "xxx");
//		req.addHeader(SipHeader.FROM, "\"Leonard Wolters\"<sip:lwolters@sip.localhost.com:5060>");
//		req.addHeader(SipHeader.TO, "\"Joost vd Wijgerd\"<sip:jwijgerd@sip.localhost.com:5060>");
//		sipClient.sendMessage(req);
//		// sleep sometime in order for message to be sent back.
//		Thread.sleep(300);
//		// get nonce!
//		String message = sipClient.getMessage();
//		int idx = message.indexOf("nonce=") + 7;
//		long nonce = Long.parseLong(message.substring(idx, message.indexOf('\"', idx)));
//		setAuthorization(req, "lwolters", Long.toString(nonce), md5Encoder.encodePassword("test", null));
//		sipClient.sendMessage(req);
//		// sleep sometime in order for message to be sent back.
//		Thread.sleep(300);
//		message = sipClient.getMessage();
//		Assert.assertNotNull(message);
//		Assert.assertTrue(message.startsWith("SIP/2.0 200 OK"));
		
		SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.REGISTER, "sip:sip.localhost.com:5060");
		sipClient.sendMessage(req);
		// sleep sometime in order for message to be sent back.
		Thread.sleep(300);
		String message = sipClient.getMessage();
		Assert.assertNotNull(message);
		Assert.assertTrue(message.startsWith("SIP/2.0 400 Bad Request"));
	}
}