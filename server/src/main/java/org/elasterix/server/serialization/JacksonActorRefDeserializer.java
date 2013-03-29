package org.elasterix.server.serialization;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.deser.std.StdScalarDeserializer;
import org.elasterix.elasticactors.ActorRef;
import org.elasterix.elasticactors.cluster.ActorRefFactory;

/**
 * @author Leonard Wolters
 */
public final class JacksonActorRefDeserializer extends StdScalarDeserializer<ActorRef> {
    private final ActorRefFactory actorRefFactory;

    public JacksonActorRefDeserializer(ActorRefFactory actorRefFactory) {
        super(ActorRef.class);
        this.actorRefFactory = actorRefFactory;
    }

    @Override
    public ActorRef deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonToken curr = jp.getCurrentToken();
        // Usually should just get string value:
        if (curr == JsonToken.VALUE_STRING) {
            return actorRefFactory.create(jp.getText());
        }
        throw ctxt.mappingException(_valueClass, curr);
    }
}
