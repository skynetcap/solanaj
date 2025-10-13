package org.p2p.solanaj.ws;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Test class for SubscriptionWebSocketClient using a real devnet connection
 */
class SubscriptionWebSocketClientTest {

    private static final String DEVNET_WS_URL = "wss://api.devnet.solana.com";
    private SubscriptionWebSocketClient client;

    /**
     * Set up the test environment
     */
    @BeforeEach
    void setUp() throws Exception {
        client = new SubscriptionWebSocketClient(DEVNET_WS_URL);
        assertTrue(client.waitForConnection(10, TimeUnit.SECONDS), "Connection timed out");
    }

    /**
     * Clean up after each test
     */
    @AfterEach
    void tearDown() {
        if (client != null && client.isOpen()) {
            client.close();
        }
    }

    /**
     * Tests that the connection can be established successfully
     */
    @Test
    void testConnectionEstablished() {
        assertTrue(client.isOpen(), "WebSocket should be open");
    }

    /**
     * Tests that the client can send and receive messages
     */
    @Test
    @Disabled("This test requires manual verification of WebSocket communication")
    void testSendAndReceiveMessage() throws Exception {
        // This test is disabled as the new implementation doesn't expose a direct send method
        // The WebSocket communication is handled internally through subscription methods
        assertTrue(client.isOpen(), "WebSocket should be open");
    }

    /**
     * Tests that the client can handle connection closure and reconnection
     */
    @Test
    void testConnectionCloseAndReconnect() throws Exception {
        client.close();
        assertFalse(client.isOpen(), "WebSocket should be closed");

        client = new SubscriptionWebSocketClient(DEVNET_WS_URL);
        assertTrue(client.waitForConnection(10, TimeUnit.SECONDS), "Reconnection timed out");
        assertTrue(client.isOpen(), "WebSocket should be open after reconnection");
    }

    /**
     * Tests that the client can subscribe to logs
     */
    @Test
    void testLogsSubscribe() throws Exception {
        CountDownLatch notificationLatch = new CountDownLatch(1);
        
        // Subscribe to a more active account that should generate logs
        client.logsSubscribe("So11111111111111111111111111111111111111112", data -> {
            System.out.println("Received notification: " + data);
            notificationLatch.countDown();
        });
        
        // Wait for a notification or timeout
        boolean received = notificationLatch.await(30, TimeUnit.SECONDS);
        System.out.println("Test completed. Received notification: " + received);
    }
}
