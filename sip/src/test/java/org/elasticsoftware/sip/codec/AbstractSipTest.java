package org.elasticsoftware.sip.codec;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.testng.annotations.BeforeTest;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

/**
 * @author Leonard Wolters
 */
public abstract class AbstractSipTest {

    @BeforeTest
    public void init() {
        Logger.getLogger("org.elasticsoftware").setLevel(Level.DEBUG);
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
        if (StringUtils.hasLength(additionalContent)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(bytes);
            baos.write(additionalContent.getBytes());
            bytes = baos.toByteArray();
        }
        if (removeBytes <= 0) {
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

    protected boolean checkCharacters(String input, String output) {
        input = input.trim();
        output = output.trim();

        if (input.length() != output.length()) {
            System.err.println(String.format("Length differs. input[%d] != output[%d]",
                    input.length(), output.length()));
            return false;
        }
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) != output.charAt(i)) {
                System.err.println(String.format("Characters differ. Index[%d]. \n%s\n======\n%s",
                        i, input.subSequence(0, i), output.subSequence(0, i)));
                return false;
            }
        }
        return true;
    }
}
