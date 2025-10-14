package org.p2p.solanaj.ws;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.logging.Logger;

/**
 * Test class for SubscriptionWebSocketClient using real Solana network connections
 */
class SubscriptionWebSocketClientTest {

    private static final Logger LOGGER = Logger.getLogger(SubscriptionWebSocketClientTest.class.getName());
    private static final String DEVNET_WS_URL = "wss://api.devnet.solana.com";
    private static final String MAINNET_WS_URL = "wss://api.mainnet-beta.solana.com";
    
    // Well-known mainnet addresses for testing
    private static final String USDC_MINT = "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v";
    private static final String USDT_MINT = "Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB";
    private static final String TOKEN_PROGRAM = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA";
    
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
     * Tests that the client can subscribe to logs and unsubscribe using the returned subscription ID
     */
    @Test
    void testLogsSubscribe() throws Exception {
        CountDownLatch notificationLatch = new CountDownLatch(1);
        
        // Subscribe to a more active account that should generate logs
        CompletableFuture<Long> subscriptionFuture = client.logsSubscribe("So11111111111111111111111111111111111111112", data -> {
            LOGGER.info("Received notification: " + data);
            notificationLatch.countDown();
        });
        
        // Wait for subscription to be established and get the subscription ID
        Long subscriptionId = subscriptionFuture.get(10, TimeUnit.SECONDS);
        LOGGER.info("Subscription established with ID: " + subscriptionId);
        assertNotNull(subscriptionId, "Subscription ID should not be null");
        
        // Wait for a notification or timeout
        boolean received = notificationLatch.await(30, TimeUnit.SECONDS);
        LOGGER.info("Test completed. Received notification: " + received);
        
        // Test unsubscribing using the subscription ID
        client.unsubscribe(subscriptionId);
        LOGGER.info("Successfully unsubscribed from subscription: " + subscriptionId);
        
        // Verify we can get the subscription ID by account
        Long retrievedId = client.getSubscriptionId("So11111111111111111111111111111111111111112");
        assertNull(retrievedId, "Subscription should be removed after unsubscribe");
    }

    @Nested
    @DisplayName("Mainnet Tests")
    class MainnetTests {
        
        private SubscriptionWebSocketClient mainnetClient;
        
        @BeforeEach
        void setUpMainnet() throws Exception {
            mainnetClient = new SubscriptionWebSocketClient(MAINNET_WS_URL);
            assertTrue(mainnetClient.waitForConnection(10, TimeUnit.SECONDS), "Mainnet connection timed out");
        }
        
        @AfterEach
        void tearDownMainnet() {
            if (mainnetClient != null && mainnetClient.isOpen()) {
                mainnetClient.close();
            }
        }
        
        @Test
        @DisplayName("Test account subscription on mainnet with subscription ID")
        void testMainnetAccountSubscribe() throws Exception {
            CountDownLatch notificationLatch = new CountDownLatch(1);
            
            // Subscribe to USDC mint account (very active on mainnet)
            CompletableFuture<Long> subscriptionFuture = mainnetClient.accountSubscribe(
                USDC_MINT, 
                data -> {
                    LOGGER.info("Received USDC account update: " + data);
                    notificationLatch.countDown();
                }
            );
            
            // Wait for subscription to be established
            Long subscriptionId = subscriptionFuture.get(10, TimeUnit.SECONDS);
            LOGGER.info("Mainnet account subscription established with ID: " + subscriptionId);
            assertNotNull(subscriptionId, "Subscription ID should not be null");
            
            // Wait for a notification (USDC mint is very active)
            boolean received = notificationLatch.await(30, TimeUnit.SECONDS);
            LOGGER.info("Mainnet account test completed. Received notification: " + received);
            
            // Test unsubscribing
            mainnetClient.unsubscribe(subscriptionId);
            LOGGER.info("Successfully unsubscribed from mainnet account subscription: " + subscriptionId);
            
            // Verify subscription is removed
            Long retrievedId = mainnetClient.getSubscriptionId(USDC_MINT);
            assertNull(retrievedId, "Subscription should be removed after unsubscribe");
        }
        
        @Test
        @DisplayName("Test logs subscription on mainnet with subscription ID")
        void testMainnetLogsSubscribe() throws Exception {
            CountDownLatch notificationLatch = new CountDownLatch(1);
            
            // Subscribe to logs mentioning USDC mint (very active on mainnet)
            CompletableFuture<Long> subscriptionFuture = mainnetClient.logsSubscribe(
                List.of(USDC_MINT), 
                data -> {
                    LOGGER.info("Received USDC log: " + data);
                    notificationLatch.countDown();
                }
            );
            
            // Wait for subscription to be established
            Long subscriptionId = subscriptionFuture.get(10, TimeUnit.SECONDS);
            LOGGER.info("Mainnet logs subscription established with ID: " + subscriptionId);
            assertNotNull(subscriptionId, "Subscription ID should not be null");
            
            // Wait for a notification (USDC logs are very frequent on mainnet)
            boolean received = notificationLatch.await(30, TimeUnit.SECONDS);
            LOGGER.info("Mainnet logs test completed. Received notification: " + received);
            
            // Test unsubscribing
            mainnetClient.unsubscribe(subscriptionId);
            LOGGER.info("Successfully unsubscribed from mainnet logs subscription: " + subscriptionId);
            
            // Verify subscription is removed
            Long retrievedId = mainnetClient.getSubscriptionId(USDC_MINT);
            assertNull(retrievedId, "Subscription should be removed after unsubscribe");
        }
        
        @Test
        @DisplayName("Test program subscription on mainnet with subscription ID")
        void testMainnetProgramSubscribe() throws Exception {
            CountDownLatch notificationLatch = new CountDownLatch(1);
            
            // Subscribe to Token Program updates (very active on mainnet)
            CompletableFuture<Long> subscriptionFuture = mainnetClient.programSubscribe(
                TOKEN_PROGRAM, 
                data -> {
                    LOGGER.info("Received Token Program update: " + data);
                    notificationLatch.countDown();
                }
            );
            
            // Wait for subscription to be established
            Long subscriptionId = subscriptionFuture.get(10, TimeUnit.SECONDS);
            LOGGER.info("Mainnet program subscription established with ID: " + subscriptionId);
            assertNotNull(subscriptionId, "Subscription ID should not be null");
            
            // Wait for a notification (Token Program is very active)
            boolean received = notificationLatch.await(30, TimeUnit.SECONDS);
            LOGGER.info("Mainnet program test completed. Received notification: " + received);
            
            // Test unsubscribing
            mainnetClient.unsubscribe(subscriptionId);
            LOGGER.info("Successfully unsubscribed from mainnet program subscription: " + subscriptionId);
            
            // Verify subscription is removed
            Long retrievedId = mainnetClient.getSubscriptionId(TOKEN_PROGRAM);
            assertNull(retrievedId, "Subscription should be removed after unsubscribe");
        }
        
        @Test
        @DisplayName("Test multiple subscriptions and unsubscriptions on mainnet")
        void testMainnetMultipleSubscriptions() throws Exception {
            CountDownLatch usdcLatch = new CountDownLatch(1);
            CountDownLatch usdtLatch = new CountDownLatch(1);
            
            // Subscribe to USDC account
            CompletableFuture<Long> usdcFuture = mainnetClient.accountSubscribe(
                USDC_MINT, 
                data -> {
                    LOGGER.info("USDC account update: " + data);
                    usdcLatch.countDown();
                }
            );
            
            // Subscribe to USDT account
            CompletableFuture<Long> usdtFuture = mainnetClient.accountSubscribe(
                USDT_MINT, 
                data -> {
                    LOGGER.info("USDT account update: " + data);
                    usdtLatch.countDown();
                }
            );
            
            // Wait for both subscriptions to be established
            Long usdcId = usdcFuture.get(10, TimeUnit.SECONDS);
            Long usdtId = usdtFuture.get(10, TimeUnit.SECONDS);
            
            LOGGER.info("USDC subscription ID: " + usdcId);
            LOGGER.info("USDT subscription ID: " + usdtId);
            
            assertNotNull(usdcId, "USDC subscription ID should not be null");
            assertNotNull(usdtId, "USDT subscription ID should not be null");
            assertNotEquals(usdcId, usdtId, "Subscription IDs should be different");
            
            // Wait for notifications
            boolean usdcReceived = usdcLatch.await(30, TimeUnit.SECONDS);
            boolean usdtReceived = usdtLatch.await(30, TimeUnit.SECONDS);
            
            LOGGER.info("USDC notification received: " + usdcReceived);
            LOGGER.info("USDT notification received: " + usdtReceived);
            
            // Test unsubscribing from one subscription
            mainnetClient.unsubscribe(usdcId);
            LOGGER.info("Unsubscribed from USDC subscription: " + usdcId);
            
            // Verify USDC subscription is removed but USDT still exists
            Long retrievedUsdcId = mainnetClient.getSubscriptionId(USDC_MINT);
            Long retrievedUsdtId = mainnetClient.getSubscriptionId(USDT_MINT);
            
            assertNull(retrievedUsdcId, "USDC subscription should be removed");
            assertEquals(usdtId, retrievedUsdtId, "USDT subscription should still exist");
            
            // Clean up remaining subscription
            mainnetClient.unsubscribe(usdtId);
            LOGGER.info("Unsubscribed from USDT subscription: " + usdtId);
        }
        
        @Test
        @DisplayName("Test slot subscription on mainnet")
        void testMainnetSlotSubscribe() throws Exception {
            CountDownLatch notificationLatch = new CountDownLatch(3); // Wait for 3 slot updates
            
            // Subscribe to slot updates
            CompletableFuture<Long> subscriptionFuture = mainnetClient.slotSubscribe(
                data -> {
                    LOGGER.info("Received slot update: " + data);
                    notificationLatch.countDown();
                }
            );
            
            // Wait for subscription to be established
            Long subscriptionId = subscriptionFuture.get(10, TimeUnit.SECONDS);
            LOGGER.info("Mainnet slot subscription established with ID: " + subscriptionId);
            assertNotNull(subscriptionId, "Subscription ID should not be null");
            
            // Wait for slot notifications (should be very frequent on mainnet)
            boolean received = notificationLatch.await(10, TimeUnit.SECONDS);
            LOGGER.info("Mainnet slot test completed. Received notifications: " + received);
            
            // Test unsubscribing
            mainnetClient.unsubscribe(subscriptionId);
            LOGGER.info("Successfully unsubscribed from mainnet slot subscription: " + subscriptionId);
        }
        
        @Test
        @DisplayName("Test root subscription on mainnet")
        void testMainnetRootSubscribe() throws Exception {
            CountDownLatch notificationLatch = new CountDownLatch(3); // Wait for 3 root updates
            
            // Subscribe to root updates
            CompletableFuture<Long> subscriptionFuture = mainnetClient.rootSubscribe(
                data -> {
                    LOGGER.info("Received root update: " + data);
                    notificationLatch.countDown();
                }
            );
            
            // Wait for subscription to be established
            Long subscriptionId = subscriptionFuture.get(10, TimeUnit.SECONDS);
            LOGGER.info("Mainnet root subscription established with ID: " + subscriptionId);
            assertNotNull(subscriptionId, "Subscription ID should not be null");
            
            // Wait for root notifications (should be frequent on mainnet)
            boolean received = notificationLatch.await(10, TimeUnit.SECONDS);
            LOGGER.info("Mainnet root test completed. Received notifications: " + received);
            
            // Test unsubscribing
            mainnetClient.unsubscribe(subscriptionId);
            LOGGER.info("Successfully unsubscribed from mainnet root subscription: " + subscriptionId);
        }
    }
}
