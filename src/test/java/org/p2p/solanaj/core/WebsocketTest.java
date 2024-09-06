package org.p2p.solanaj.core;

import org.junit.Before;
import org.junit.Test;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.ws.SubscriptionWebSocketClient;
import org.p2p.solanaj.ws.listeners.NotificationEventListener;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class WebsocketTest {

    private SubscriptionWebSocketClient devnetClient;
    private static final Logger LOGGER = Logger.getLogger(WebsocketTest.class.getName());
    private static final String POPULAR_ACCOUNT = "SysvarC1ock11111111111111111111111111111111";

    @Before
    public void setUp() {
        devnetClient = SubscriptionWebSocketClient.getInstance(Cluster.DEVNET.getEndpoint());
    }

    @Test
    public void testAccountSubscribe() throws Exception {
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        
        devnetClient.accountSubscribe(POPULAR_ACCOUNT, (NotificationEventListener) data -> {
            LOGGER.info("Received notification: " + data);
            future.complete((Map<String, Object>) data);
        });

        Map<String, Object> result = future.get(30, TimeUnit.SECONDS);
        assertNotNull("Notification should not be null", result);
        assertTrue("Notification should contain 'lamports'", result.containsKey("lamports"));
    }

    @Test
    public void testMultipleSubscriptions() throws Exception {
        CompletableFuture<Map<String, Object>> future1 = new CompletableFuture<>();
        CompletableFuture<Map<String, Object>> future2 = new CompletableFuture<>();
        
        devnetClient.accountSubscribe(POPULAR_ACCOUNT, (NotificationEventListener) data -> {
            LOGGER.info("Received notification for subscription 1: " + data);
            future1.complete((Map<String, Object>) data);
        });

        devnetClient.accountSubscribe(POPULAR_ACCOUNT, (NotificationEventListener) data -> {
            LOGGER.info("Received notification for subscription 2: " + data);
            future2.complete((Map<String, Object>) data);
        });

        Map<String, Object> result1 = future1.get(30, TimeUnit.SECONDS);
        Map<String, Object> result2 = future2.get(30, TimeUnit.SECONDS);

        assertNotNull("Notification 1 should not be null", result1);
        assertNotNull("Notification 2 should not be null", result2);
        assertTrue("Notification 1 should contain 'lamports'", result1.containsKey("lamports"));
        assertTrue("Notification 2 should contain 'lamports'", result2.containsKey("lamports"));
    }
}
