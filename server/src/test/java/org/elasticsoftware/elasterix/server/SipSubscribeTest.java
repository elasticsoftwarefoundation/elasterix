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

package org.elasticsoftware.elasterix.server;

import org.apache.log4j.Logger;
import org.elasticsoftware.sip.codec.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.UUID;

/**
 * Test that creates a test actor system and loads the ElasterixServer<br>
 * <p/>
 * test all different kinds of <code>Sip Register</code> messages
 * see http://tools.ietf.org/html/rfc3261#section-10.2
 *
 * @author Joost van de Wijgerd
 * @author Leonard Wolters
 */
public class SipSubscribeTest extends AbstractSipTest {
    private static final Logger log = Logger.getLogger(SipSubscribeTest.class);

    @Test(enabled = true)
    public void testSubscribeUnsupported() throws Exception {
        SipRequest req = new SipRequestImpl(SipVersion.SIP_2_0, SipMethod.SUBSCRIBE, "sip:sip.localhost.com:5060");
        req.addHeader(SipHeader.CALL_ID, UUID.randomUUID().toString());
        req.addHeader(SipHeader.CONTACT, "<sip:124@62.163.143.30:60236;transport=UDP;rinstance=e6768ab86fdcf0b4>");
        req.addHeader(SipHeader.FROM, "\"Leonard Wolters\"<sip:lwolters@sip.localhost.com:5060>");
        req.addHeader(SipHeader.VIA, "yyy");
        req.addHeader(SipHeader.CSEQ, "1 REGISTER");
        sipServer.sendMessage(req);
        // sleep sometime in order for message to be sent back.
        Thread.sleep(300);

        String message = sipServer.getMessage();
        Assert.assertNotNull(message);
        //Assert.assertTrue(message.startsWith("SIP/2.0 501 Not Implemented"));
        Assert.assertTrue(message.startsWith("SIP/2.0 404 Not Found"));
    }
}

