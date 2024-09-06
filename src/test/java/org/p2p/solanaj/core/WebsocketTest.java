package org.p2p.solanaj.core;

import org.junit.Before;
import org.junit.Test;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.ws.SubscriptionWebSocketClient;
import org.p2p.solanaj.ws.listeners.NotificationEventListener;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.Map;
import java.util.LinkedHashMap;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

/**
 * Test class for WebSocket functionality in the Solana Java client.
 */
public class WebsocketTest {

    private static final Logger LOGGER = Logger.getLogger(WebsocketTest.class.getName());
    private static final String TEST_ACCOUNT = "4DoNfFBfF7UokCC2FQzriy7yHK6DY6NVdYpuekQ5pRgg";
    private static final String SYSVAR_CLOCK = "SysvarC1ock11111111111111111111111111111111";
    private static final long CONNECTION_TIMEOUT = 10;
    private static final long NOTIFICATION_TIMEOUT = 120;

    private SubscriptionWebSocketClient createClient() {
        URI serverURI;
        try {
            serverURI = new URI(Cluster.MAINNET.getEndpoint().replace("https", "wss"));
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URI", e);
        }
        return SubscriptionWebSocketClient.getInstance(serverURI.toString());
    }

    @Test
    public void testAccountSubscribe() throws Exception {
        SubscriptionWebSocketClient client = null;
        try {
            client = createClient();
            if (!client.waitForConnection(CONNECTION_TIMEOUT, TimeUnit.SECONDS)) {
                fail("Failed to establish WebSocket connection");
            }
            
            CountDownLatch latch = new CountDownLatch(1);
            CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
            
            client.accountSubscribe(TEST_ACCOUNT, (NotificationEventListener) data -> {
                LOGGER.info("Received notification: " + data);
                future.complete((Map<String, Object>) data);
                latch.countDown();
            });

            if (!latch.await(NOTIFICATION_TIMEOUT, TimeUnit.SECONDS)) {
                fail("Test timed out waiting for notification from " + TEST_ACCOUNT);
            }

            Map<String, Object> result = future.get(5, TimeUnit.SECONDS);
            assertNotNull("Notification should not be null", result);
            
            LOGGER.info("Received result structure: " + result);

            validateAccountData(result);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Test
    public void testMultipleSubscriptions() throws Exception {
        SubscriptionWebSocketClient client = null;
        CountDownLatch latch = new CountDownLatch(2);
        try {
            client = createClient();
            if (!client.waitForConnection(CONNECTION_TIMEOUT, TimeUnit.SECONDS)) {
                fail("Failed to establish WebSocket connection");
            }

            CompletableFuture<Map<String, Object>> future1 = new CompletableFuture<>();
            CompletableFuture<Map<String, Object>> future2 = new CompletableFuture<>();
            
            LOGGER.info("Starting multiple subscriptions test");

            NotificationEventListener listener1 = data -> {
                LOGGER.info("Received notification for subscription 1 (TEST_ACCOUNT): " + data);
                if (!future1.isDone()) {
                    future1.complete((Map<String, Object>) data);
                    latch.countDown();
                }
            };

            NotificationEventListener listener2 = data -> {
                LOGGER.info("Received notification for subscription 2 (SYSVAR_CLOCK): " + data);
                if (!future2.isDone()) {
                    future2.complete((Map<String, Object>) data);
                    latch.countDown();
                }
            };

            LOGGER.info("Subscribing to TEST_ACCOUNT (1st subscription)");
            client.accountSubscribe(TEST_ACCOUNT, listener1);
            LOGGER.info("Subscribing to SYSVAR_CLOCK (2nd subscription)");
            client.accountSubscribe(SYSVAR_CLOCK, listener2);

            LOGGER.info("Waiting for notifications...");
            if (!latch.await(NOTIFICATION_TIMEOUT, TimeUnit.SECONDS)) {
                LOGGER.warning("Test timed out waiting for notifications");
                if (!future1.isDone()) {
                    fail("Timed out waiting for notification from " + TEST_ACCOUNT);
                } else {
                    fail("Timed out waiting for notification from " + SYSVAR_CLOCK);
                }
            }

            LOGGER.info("Latch count reached zero, proceeding with assertions");

            Map<String, Object> result1 = future1.get(5, TimeUnit.SECONDS);
            Map<String, Object> result2 = future2.get(5, TimeUnit.SECONDS);

            assertNotNull("Notification 1 should not be null", result1);
            assertNotNull("Notification 2 should not be null", result2);
            
            LOGGER.info("Received data for subscription 1 (TEST_ACCOUNT): " + result1);
            LOGGER.info("Received data for subscription 2 (SYSVAR_CLOCK): " + result2);
            
            validateAccountData(result1);
            validateAccountData(result2);
        } catch (Exception e) {
            LOGGER.severe("Error occurred: " + e.getMessage());
            LOGGER.info("Latch count at error: " + latch.getCount());
            fail("Test failed: " + e.getMessage());
        } finally {
            if (client != null) {
                LOGGER.info("Closing WebSocket connection");
                client.close();
            }
        }
    }

    private void validateAccountData(Map<String, Object> data) {
        // Implement proper validation logic here
        assertNotNull("Account data should not be null", data);
        assertTrue("Account data should contain 'lamports'", data.containsKey("lamports"));
        assertTrue("Account data should contain 'data'", data.containsKey("data"));
        // Add more specific validations as needed
    }
}
