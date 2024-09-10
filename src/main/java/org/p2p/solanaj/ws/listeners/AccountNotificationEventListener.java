package org.p2p.solanaj.ws.listeners;

import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Listener for account notification events in Solana.
 * Handles changes in account data or lamports.
 */
public class AccountNotificationEventListener implements NotificationEventListener {

    private static final Logger LOGGER = Logger.getLogger(AccountNotificationEventListener.class.getName());

    /**
     * Handles account notification events.
     * @param data The notification data, expected to be a Map.
     */
    @Override
    public void onNotificationEvent(Object data) {
        if (!(data instanceof Map)) {
            LOGGER.log(Level.WARNING, "Invalid data type received: {0}", data.getClass().getName());
            return;
        }

        Map<?, ?> rawMap = (Map<?, ?>) data;
        Map<String, Object> accountData = new HashMap<>();

        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            if (entry.getKey() instanceof String) {
                accountData.put((String) entry.getKey(), entry.getValue());
            }
        }

        String accountKey = (String) accountData.get("accountKey");
        Long lamports = (Long) accountData.get("lamports");
        String owner = (String) accountData.get("owner");

        LOGGER.log(Level.INFO, "Account notification received for account: {0}", accountKey);
        LOGGER.log(Level.INFO, "Lamports: {0}, Owner: {1}", new Object[]{lamports, owner});
    }
}
