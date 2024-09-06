package org.p2p.solanaj.ws;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Test class for SubscriptionWebSocketClient using a real devnet connection
 */
public class SubscriptionWebSocketClientTest {

    private static final String DEVNET_WS_URL = "wss://api.devnet.solana.com";
    private SubscriptionWebSocketClient client;
    private CountDownLatch connectionLatch;

    /**
     * Set up the test environment
     */
    @Before
    public void setUp() throws Exception {
        connectionLatch = new CountDownLatch(1);
        client = new SubscriptionWebSocketClient(new URI(DEVNET_WS_URL)) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                super.onOpen(handshakedata);
                connectionLatch.countDown();
            }
        };
        client.connect();
        assertTrue("Connection timed out", connectionLatch.await(10, TimeUnit.SECONDS));
    }

    /**
     * Clean up after each test
     */
    @After
    public void tearDown() {
        if (client != null && client.isOpen()) {
            client.close();
        }
    }

    /**
     * Tests that the connection can be established successfully
     */
    @Test
    public void testConnectionEstablished() {
        assertTrue("WebSocket should be open", client.isOpen());
    }

    /**
     * Tests that the client can send and receive messages
     */
    @Test
    public void testSendAndReceiveMessage() throws Exception {
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
        assertTrue("Connection timed out", connectionLatch.await(10, TimeUnit.SECONDS));

        // Ensure client is connected before sending message
        while (!client.isOpen()) {
            Thread.sleep(100);
        }

        String testMessage = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"getHealth\"}";
        client.send(testMessage);

        assertTrue("Message response timed out", messageLatch.await(10, TimeUnit.SECONDS));
        assertNotNull("Received message should not be null", receivedMessage[0]);
        
        System.out.println("Received message: " + receivedMessage[0]);
        
        assertTrue("Received message should contain 'result' or 'error'", 
                   receivedMessage[0].contains("result") || receivedMessage[0].contains("error"));
    }

    /**
     * Tests that the client can handle connection closure and reconnection
     */
    @Test
    public void testConnectionCloseAndReconnect() throws Exception {
        client.close();
        assertFalse("WebSocket should be closed", client.isOpen());

        CountDownLatch reconnectLatch = new CountDownLatch(1);
        client = new SubscriptionWebSocketClient(new URI(DEVNET_WS_URL)) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                super.onOpen(handshakedata);
                reconnectLatch.countDown();
            }
        };
        client.connect();

        assertTrue("Reconnection timed out", reconnectLatch.await(10, TimeUnit.SECONDS));
        assertTrue("WebSocket should be open after reconnection", client.isOpen());
    }
}
