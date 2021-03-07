package io.cucumber.messages;

import io.cucumber.messages.types.Envelope;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NdjsonSerializationTest extends MessageSerializationContract {
    @Override
    protected MessageWriter makeMessageWriter(OutputStream output) {
        return new MessageToNdjsonWriter(output);
    }

    @Override
    protected Iterable<Envelope> makeMessageIterable(InputStream input) {
        return new NdjsonToMessageIterable(input);
    }

    @Test
    void ignores_missing_fields() {
        InputStream input = new ByteArrayInputStream("{\"unused\": 99}\n".getBytes(UTF_8));
        Iterable<Envelope> incomingMessages = makeMessageIterable(input);
        Iterator<Envelope> iterator = incomingMessages.iterator();
        assertTrue(iterator.hasNext());
        Envelope envelope = iterator.next();
        assertEquals(new Envelope(), envelope);
        assertFalse(iterator.hasNext());
    }

    @Test
    void ignores_empty_lines() {
        InputStream input = new ByteArrayInputStream("{}\n{}\n\n{}\n".getBytes(UTF_8));
        Iterable<Envelope> incomingMessages = makeMessageIterable(input);
        Iterator<Envelope> iterator = incomingMessages.iterator();
        for(int i = 0; i < 3; i++) {
            assertTrue(iterator.hasNext());
            Envelope envelope = iterator.next();
            assertEquals(new Envelope(), envelope);
        }
        assertFalse(iterator.hasNext());
    }

    @Test
    void includes_offending_line_in_error_message() {
        InputStream input = new ByteArrayInputStream("BLA BLA".getBytes(UTF_8));
        Iterable<Envelope> incomingMessages = makeMessageIterable(input);
        Iterator<Envelope> iterator = incomingMessages.iterator();

        RuntimeException exception = assertThrows(RuntimeException.class, () -> assertTrue(iterator.hasNext()));
        assertEquals(exception.getMessage(), "Not JSON: BLA BLA");
    }
}
