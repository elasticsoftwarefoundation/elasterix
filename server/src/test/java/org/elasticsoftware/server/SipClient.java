package org.elasticsoftware.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
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
	private String socketType;
	private Socket socket;
	private Stack<String> messages = new Stack<String>();
	
	public SipClient(int port) {
		try {
			socketType = "LOCAL";
			socket = initSocket(new ServerSocket(port).accept());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public SipClient(String host, int port) {
		try {
			socketType = "REMOTE";
			socket = initSocket(new Socket("localhost", port));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public Socket initSocket(final Socket socket) throws Exception {
		try {
			new Thread(new Runnable() {
				@Override
				public void run() {
					StringBuffer buffer = new StringBuffer();
					try {
						BufferedReader in = new BufferedReader(
								new InputStreamReader(socket.getInputStream()));
						String line;
						while (null != ((line = in.readLine()))) {
							if(StringUtils.hasLength(line)) {
								buffer.append(line).append("\n");
							} else {
								log.info(String.format("Message received[%s]", socketType));
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
		return socket;
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
		DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		if(data != null) {
			log.debug(String.format("Sending data\n%s", new String(data, Charset.forName("UTF-8"))));
			dos.write(data);
		} else {
			log.debug(String.format("Sending data\n%s", content));
			dos.write(content.getBytes());
		}
		dos.flush();
	}
	
	public void close() throws Exception {
		if(socket != null) {
			socket.shutdownInput();
			socket.shutdownOutput();
			socket.close();
		}
	}
}
