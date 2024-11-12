package org.p2p.solanaj.ws;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import static org.junit.jupiter.api.Assertions.*;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Test class for SubscriptionWebSocketClient using a real devnet connection
 */
class SubscriptionWebSocketClientTest {

    private static final String DEVNET_WS_URL = "wss://api.devnet.solana.com";
    private SubscriptionWebSocketClient client;
    private CountDownLatch connectionLatch;

    /**
     * Set up the test environment
     */
    @BeforeEach
    void setUp() throws Exception {
        connectionLatch = new CountDownLatch(1);
        client = new SubscriptionWebSocketClient(new URI(DEVNET_WS_URL)) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                super.onOpen(handshakedata);
                connectionLatch.countDown();
            }
        };
        client.connect();
        assertTrue(connectionLatch.await(10, TimeUnit.SECONDS), "Connection timed out");
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
    void testSendAndReceiveMessage() throws Exception {
        CountDownLatch messageLatch = new CountDownLatch(1);
        final String[] receivedMessage = new String[1];

        client = new SubscriptionWebSocketClient(new URI(DEVNET_WS_URL)) {
            @Override
            public void onMessage(String message) {
                receivedMessage[0] = message;
                messageLatch.countDown();
            }
        };
        client.connect();
        assertTrue(connectionLatch.await(10, TimeUnit.SECONDS), "Connection timed out");

        // Ensure client is connected before sending message
        while (!client.isOpen()) {
            Thread.sleep(100);
        }

        String testMessage = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"getHealth\"}";
        client.send(testMessage);

        assertTrue(messageLatch.await(10, TimeUnit.SECONDS), "Message response timed out");
        assertNotNull(receivedMessage[0], "Received message should not be null");
        
        System.out.println("Received message: " + receivedMessage[0]);
        
        assertTrue(receivedMessage[0].contains("result") || receivedMessage[0].contains("error"), 
                   "Received message should contain 'result' or 'error'");
    }

    /**
     * Tests that the client can handle connection closure and reconnection
     */
    @Test
    void testConnectionCloseAndReconnect() throws Exception {
        client.close();
        assertFalse(client.isOpen(), "WebSocket should be closed");

        CountDownLatch reconnectLatch = new CountDownLatch(1);
        client = new SubscriptionWebSocketClient(new URI(DEVNET_WS_URL)) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                super.onOpen(handshakedata);
                reconnectLatch.countDown();
            }
        };
        client.connect();

        assertTrue(reconnectLatch.await(10, TimeUnit.SECONDS), "Reconnection timed out");
        assertTrue(client.isOpen(), "WebSocket should be open after reconnection");
    }
}
