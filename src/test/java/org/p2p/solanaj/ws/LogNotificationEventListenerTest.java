package org.p2p.solanaj.ws;

import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.ws.listeners.LogNotificationEventListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class LogNotificationEventListenerTest {

    @Mock
    private RpcClient mockRpcClient;

    private PublicKey testPublicKey;
    private LogNotificationEventListener listener;
    private TestLogHandler logHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        testPublicKey = new PublicKey("PhoeNiXZ8ByJGLkxNfZRnkUfjvmuYqLR89jjFHGqdXY");
        listener = new LogNotificationEventListener(mockRpcClient, testPublicKey);

        logHandler = new TestLogHandler();
        Logger logger = Logger.getLogger(LogNotificationEventListener.class.getName());
        logger.addHandler(logHandler);
        logger.setLevel(Level.ALL);
    }

    @After
    public void tearDown() {
        Logger logger = Logger.getLogger(LogNotificationEventListener.class.getName());
        logger.removeHandler(logHandler);
    }

    /**
     * Tests the onNotificationEvent method with valid data.
     * Verifies that the listener processes the data correctly without throwing exceptions.
     */
    @Test
    public void testOnNotificationEvent_ValidData() {
        Map<String, Object> testData = new HashMap<>();
        // Using a realistic Solana transaction signature
        String realSignature = "5wHu1qwD4kLwYvKNyZzjuoMYpGHSreYitBUJb7TQx3hngzs7jq6hBwZWwGcRQK3H9rw7Fxgb3zBYLXqjrDkDvnqf";
        testData.put("signature", realSignature);
        List<String> logs = Arrays.asList(
            "Program 11111111111111111111111111111111 invoke [1]",
            "Program 11111111111111111111111111111111 success"
        );
        testData.put("logs", logs);

        listener.onNotificationEvent(testData);

        assertTrue(logHandler.hasMessage("Received notification for transaction: " + realSignature));
        assertTrue(logHandler.hasMessage("Log: Program 11111111111111111111111111111111 invoke [1]"));
        assertTrue(logHandler.hasMessage("Log: Program 11111111111111111111111111111111 success"));
    }

    /**
     * Tests the onNotificationEvent method with null data.
     * Verifies that the listener handles null input gracefully.
     */
    @Test
    public void testOnNotificationEvent_NullData() {
        listener.onNotificationEvent(null);
        assertTrue(logHandler.hasMessage("Received null data in onNotificationEvent"));
    }

    /**
     * Tests the onNotificationEvent method with invalid data type.
     * Verifies that the listener handles unexpected input types correctly.
     */
    @Test
    public void testOnNotificationEvent_InvalidDataType() {
        listener.onNotificationEvent("Invalid data type");
        assertTrue(logHandler.hasMessage("Received invalid data type in onNotificationEvent: java.lang.String"));
    }

    /**
     * Tests the onNotificationEvent method with missing required fields.
     * Verifies that the listener handles incomplete data appropriately.
     */
    @Test
    public void testOnNotificationEvent_MissingFields() {
        Map<String, Object> testData = new HashMap<>();
        testData.put("signature", "someSignature");
        // Missing 'logs' field
        listener.onNotificationEvent(testData);
        assertTrue(logHandler.hasMessage("Missing required fields in notification data"));
    }

    /**
     * Tests the getter methods of the listener.
     * Verifies that the getClient and getListeningPubkey methods return the expected values.
     */
    @Test
    public void testGetters() {
        assertEquals("RpcClient should match", mockRpcClient, listener.getClient());
        assertEquals("PublicKey should match", testPublicKey, listener.getListeningPubkey());
    }

    private static class TestLogHandler extends Handler {
        private final List<LogRecord> logs = new ArrayList<>();

        @Override
        public void publish(LogRecord record) {
            logs.add(record);
        }

        @Override
        public void flush() {}

        @Override
        public void close() throws SecurityException {}

        public boolean hasMessage(String message) {
            return logs.stream().anyMatch(record -> record.getMessage().contains(message));
        }
    }
}
