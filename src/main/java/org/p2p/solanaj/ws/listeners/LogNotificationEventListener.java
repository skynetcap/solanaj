package org.p2p.solanaj.ws.listeners;

import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.RpcClient;

import java.util.AbstractMap;
import java.util.logging.Logger;

public class LogNotificationEventListener implements NotificationEventListener {

    private static final Logger LOGGER = Logger.getLogger(LogNotificationEventListener.class.getName());
    private final RpcClient client;
    private final PublicKey listeningPubkey;

    public LogNotificationEventListener(RpcClient client, PublicKey listeningPubkey) {
        this.client = client;
        this.listeningPubkey = listeningPubkey;
    }

    /**
     * Handle Account notification event (change in data or change in lamports). Type of "data" is a Map.
     * @param data Map
     */
    @Override
    public void onNotificationEvent(Object data) {
        if (data != null) {
            AbstractMap<String, String> map = (AbstractMap<String, String>) data;
            LOGGER.info(String.format("Data = %s", map));
            String signature = map.get("signature");
            LOGGER.info("Signature = " + signature);
        }
    }
}
