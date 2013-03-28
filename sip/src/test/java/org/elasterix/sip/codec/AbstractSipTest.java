package org.elasterix.sip.codec;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.testng.annotations.BeforeTest;

/**
 * 
 * @author Leonard Wolters
 */
public abstract class AbstractSipTest {
	
	@BeforeTest
	public void init() {
		Logger.getLogger("org.elasterix").setLevel(Level.DEBUG);
	}
	
	protected ChannelBuffer createChannelFromFile(String fileName) throws Exception {
		return createChannelFromFile(fileName, null, -1);
	}
	
	protected ChannelBuffer createChannelFromFile(String fileName, int removeBytes) throws Exception {
		return createChannelFromFile(fileName, null, removeBytes);
	}
	
	protected ChannelBuffer createChannelFromFile(String fileName, String additionalContent,
			int removeBytes) throws Exception {
		Resource resource = new ClassPathResource(fileName);
		byte[] bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
		if(StringUtils.hasLength(additionalContent)) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			baos.write(bytes);
			baos.write(additionalContent.getBytes());
			bytes = baos.toByteArray();
		}
		if(removeBytes <= 0) {
			return ChannelBuffers.copiedBuffer(bytes);
		}
		// remove some bytes at the end...
		int newLength = bytes.length - removeBytes;
		byte[] dest = new byte[newLength];
		System.arraycopy(bytes, 0, dest, 0, newLength);
		return ChannelBuffers.copiedBuffer(dest);
	}
	
	protected String getFileContent(String fileName) throws Exception {
		return getFileContent(fileName, Charset.forName("UTF-8"));
	}
	protected String getFileContent(String fileName, Charset charSet) throws Exception {
		Resource resource = new ClassPathResource(fileName);
		byte[] bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
		return new String(bytes, charSet);
	}
	
	protected String prepare(String value) {
		return value.replace("\r\n", "\n");
	}
}
