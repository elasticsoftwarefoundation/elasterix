package org.elasticsoftware.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.elasticsoftware.sip.codec.SipRequest;
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
	/** Socket to communicate with SIP server */
	private Socket serverSocket;
	
	private Stack<String> messages = new Stack<String>();

	public SipClient() {
		try {
			serverSocket = new Socket("localhost", 5060);
			new Thread(new Runnable() {
				@Override
				public void run() {
					StringBuffer buffer = new StringBuffer();
					try {
						BufferedReader in = new BufferedReader(
								new InputStreamReader(serverSocket.getInputStream()));
						String line;
						while (null != ((line = in.readLine()))) {
							if(StringUtils.hasLength(line)) {
								buffer.append(line).append("\n");
							} else {
								log.info("Message received");
								messages.add(buffer.toString());
								buffer.setLength(0);
							}
						}
					} catch (Exception e) {
						log.error(e.getMessage());
					}
				}
			}).start();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public String getMessage() {
		return messages.pop();
	}
	
	public void sendMessage(SipRequest request) throws Exception {
		
		// initial line
		StringBuffer buf = new StringBuffer(String.format("%s %s %s", request.getMethod().name(),
                request.getUri(), request.getVersion().toString()));
        buf.append(StringUtil.NEWLINE);
		
		// headers
        for (Map.Entry<String, List<String>> header : request.getHeaders().entrySet()) {
        	buf.append(header.getKey());
        	buf.append(": ");
        	buf.append(StringUtils.arrayToDelimitedString(header.getValue().toArray(), "\n"));
            buf.append(StringUtil.NEWLINE);
        }
        buf.append(StringUtil.NEWLINE);

        // content
        ChannelBuffer content = request.getContent();
        if (content.readable()) {
        	buf.append(content.toString(CharsetUtil.UTF_8)).append(StringUtil.NEWLINE);
        }
        
        sendMessage(null, buf.toString());
	}
	
	public void sendMessage(String fileName) throws Exception {
		Resource resource = new ClassPathResource(fileName);
		sendMessage(FileCopyUtils.copyToByteArray(resource.getInputStream()), null);
	}
	
	protected void sendMessage(byte[] data, String content) throws Exception {
		DataOutputStream dos = new DataOutputStream(serverSocket.getOutputStream());
		if(data != null) {
			log.debug(String.format("Sending data\n%s", new String(data, Charset.forName("UTF-8"))));
			dos.write(data);
		} else {
			log.debug(String.format("Sending data\n%s", content));
			dos.write(content.getBytes());
		}
		dos.flush();
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
		if(serverSocket != null) {
			serverSocket.shutdownInput();
			serverSocket.shutdownOutput();
			serverSocket.close();
		}
	}
}
