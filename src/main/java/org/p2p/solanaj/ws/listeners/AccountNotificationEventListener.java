package org.p2p.solanaj.ws.listeners;

import java.util.logging.Logger;

public class AccountNotificationEventListener implements NotificationEventListener {

    private static final Logger LOGGER = Logger.getLogger(AccountNotificationEventListener.class.getName());

    /**
     * Handle Account notification event (change in data or change in lamports). Type of "data" is a Map.
     * @param data Map
     */
    @Override
    public void onNotificationEvent(Object data) {
        LOGGER.info("Raw = " + data);
    }
}
