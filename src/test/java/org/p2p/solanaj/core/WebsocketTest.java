package org.p2p.solanaj.core;

import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.ws.SubscriptionWebSocketClient;
import org.p2p.solanaj.ws.listeners.AccountNotificationEventListener;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.Assert.assertTrue;

@Slf4j
public class WebsocketTest {

    private final SubscriptionWebSocketClient devnetClient = SubscriptionWebSocketClient.getInstance(
            Cluster.DEVNET.getEndpoint()
    );

    @Test
    @Ignore
    public void pythWebsocketTest() {
        devnetClient.accountSubscribe(
                PublicKey.valueOf("H6ARHf6YXhGYeQfUzQNGk6rDNnLBQKrenN712K4AQJEG").toBase58(),
                new AccountNotificationEventListener()
        );

        try {
            Thread.sleep(10020000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(true);
    }
}
