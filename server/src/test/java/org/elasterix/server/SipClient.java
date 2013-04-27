package org.elasterix.server;

import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.elasterix.sip.codec.SipRequest;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.util.CharsetUtil;
import org.jboss.netty.util.internal.StringUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

/**
 * A native SIP Client *not* based on netty 
 * 
 * @author Leonard Wolters
 */
public class SipClient {
	private static final Logger log = Logger.getLogger(SipClient.class);
	private Socket socket;
	
	public SipClient() {
	}
	
	public String sendMessage(SipRequest request) throws Exception {
		
		// initial line
		StringBuffer buf = new StringBuffer(String.format("%s %s %s", request.getMethod().name(), 
				request.getUri(), request.getProtocolVersion().toString()));
		
		// headers
        for (Map.Entry<String, List<String>> header : request.getHeaders().entrySet()) {
        	buf.append(header.getKey());
        	buf.append(": ");
        	buf.append(StringUtils.arrayToDelimitedString(header.getValue().toArray(), "\n"));
        	buf.append(StringUtil.NEWLINE);
        }
        buf.append("").append(StringUtil.NEWLINE);

        // content
        ChannelBuffer content = request.getContent();
        if (content.readable()) {
        	buf.append(content.toString(CharsetUtil.UTF_8)).append(StringUtil.NEWLINE);
        }
        
        return sendMessage(null, buf.toString());
	}
	
	public String sendMessage(String fileName) throws Exception {
		Resource resource = new ClassPathResource(fileName);
		return sendMessage(FileCopyUtils.copyToByteArray(resource.getInputStream()), null);
	}
	
	protected String sendMessage(byte[] data, String content) throws Exception {
		socket = new Socket("localhost", 5060);
		
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		log.debug(String.format("Sending data\n%s", new String(data, Charset.forName("UTF-8"))));
		if(data != null) {
			dos.write(data);
		} else {
			dos.writeUTF(content);
		}
		dos.flush();
		
//		DataInputStream dis = new DataInputStream(socket.getInputStream());
		StringBuffer buffer = new StringBuffer();
//		String line;
//		while (null != ((line = dis.readLine()))) {
//			buffer.append(line);
//		}
		
		return buffer.toString();
	}
	
//	protected String sendMessage(URL url, byte[] data) throws Exception {
//		try {
//			// setup connection
//			URLConnection conn = url.openConnection();
//			log.info("Opening connection: " + url);
//			conn.setDoOutput(true);			
//			conn.setUseCaches(false);
//			
//			conn.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");			
//			//conn.setRequestProperty("Accept", "application/xml");
//
//			// POST data
//			DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
//			log.debug(String.format("Sending data\n%s", new String(data, Charset.forName("UTF-8"))));
//			dos.write(data);
//			dos.flush();
//			dos.close();
//
//			// GET response
//			DataInputStream dis = new DataInputStream(conn.getInputStream());
//			StringBuffer buffer = new StringBuffer();
//			String line;
//			while (null != ((line = dis.readLine()))) {
//				buffer.append(line);
//			}
//			dis.close();
//
//			return buffer.toString();
//		} catch (Exception e) {
//			log.error(e.getMessage(), e);
//		}
//		return null;
//	}
	
	public void close() throws Exception {
		if(socket != null) {
			socket.shutdownInput();
			socket.shutdownOutput();
			socket.close();
		}
	}
}
