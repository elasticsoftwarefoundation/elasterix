package org.elasterix.server.serialization;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.codehaus.jackson.map.ObjectMapper;
import org.elasterix.elasticactors.serialization.MessageDeserializer;

/**
 * @author Leonard Wolters
 */
public final class JacksonMessageDeserializer<T> implements MessageDeserializer<T> {
    private final ObjectMapper objectMapper;
    private final Class<T> objectClass;

    public JacksonMessageDeserializer(Class<T> objectClass,ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.objectClass = objectClass;
    }

    @Override
    public T deserialize(ByteBuffer serializedObject) throws IOException {
        byte[] buf = new byte[serializedObject.remaining()];
        serializedObject.get(buf);
        return objectMapper.readValue(buf, objectClass);
    }
}
