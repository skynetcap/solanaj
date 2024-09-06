package org.p2p.solanaj.ws;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ServerHandshake;
import org.p2p.solanaj.rpc.types.RpcNotificationResult;
import org.p2p.solanaj.rpc.types.RpcRequest;
import org.p2p.solanaj.rpc.types.RpcResponse;
import org.p2p.solanaj.rpc.types.config.Commitment;
import org.p2p.solanaj.ws.listeners.NotificationEventListener;

/**
 * A WebSocket client for managing subscriptions to various Solana events.
 */
public class SubscriptionWebSocketClient extends WebSocketClient {

    private static final Logger LOGGER = Logger.getLogger(SubscriptionWebSocketClient.class.getName());
    private static final int MAX_RECONNECT_DELAY = 30000;
    private static final int INITIAL_RECONNECT_DELAY = 1000;
    private static final int HEARTBEAT_INTERVAL = 30;

    private final Map<String, SubscriptionParams> subscriptions = new ConcurrentHashMap<>();
    private final Map<String, Long> subscriptionIds = new ConcurrentHashMap<>();
    private final Map<Long, NotificationEventListener> subscriptionListeners = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final CountDownLatch connectLatch = new CountDownLatch(1);

    private int reconnectDelay = INITIAL_RECONNECT_DELAY;
    private final Moshi moshi = new Moshi.Builder().build();

    /**
     * Inner class to hold subscription parameters.
     */
    private static class SubscriptionParams {
        final RpcRequest request;
        final NotificationEventListener listener;

        /**
         * Constructs a SubscriptionParams object.
         *
         * @param request The RPC request for the subscription
         * @param listener The listener for notification events
         */
        SubscriptionParams(RpcRequest request, NotificationEventListener listener) {
            this.request = request;
            this.listener = listener;
        }
    }

    /**
     * Creates a SubscriptionWebSocketClient instance with the exact path provided.
     *
     * @param endpoint The WebSocket endpoint URL
     * @return A new SubscriptionWebSocketClient instance
     */
    public static SubscriptionWebSocketClient getExactPathInstance(String endpoint) {
        try {
            URI serverURI = new URI(endpoint);
            SubscriptionWebSocketClient instance = new SubscriptionWebSocketClient(serverURI);
            if (!instance.isOpen()) {
                instance.connect();
            }
            return instance;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid endpoint URI", e);
        }
    }

    /**
     * Creates a SubscriptionWebSocketClient instance with a modified URI based on the provided endpoint.
     *
     * @param endpoint The endpoint URL
     * @return A new SubscriptionWebSocketClient instance
     */
    public static SubscriptionWebSocketClient getInstance(String endpoint) {
        try {
            URI endpointURI = new URI(endpoint);
            String scheme = "https".equals(endpointURI.getScheme()) ? "wss" : "ws";
            URI serverURI = new URI(scheme + "://" + endpointURI.getHost());
            SubscriptionWebSocketClient instance = new SubscriptionWebSocketClient(serverURI);
            instance.connect();
            return instance;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid endpoint URI", e);
        }
    }

    /**
     * Constructs a SubscriptionWebSocketClient with the given server URI.
     *
     * @param serverURI The URI of the WebSocket server
     */
    public SubscriptionWebSocketClient(URI serverURI) {
        super(serverURI);
    }

    /**
     * Subscribes to account updates for the given key.
     *
     * @param key The account key to subscribe to
     * @param listener The listener to handle notifications
     */
    public void accountSubscribe(String key, NotificationEventListener listener) {
        List<Object> params = new ArrayList<>();
        params.add(key);
        params.add(Map.of("encoding", "jsonParsed", "commitment", Commitment.PROCESSED.getValue()));

        RpcRequest rpcRequest = new RpcRequest("accountSubscribe", params);
        addSubscription(rpcRequest, listener);
    }

    /**
     * Subscribes to signature updates for the given signature.
     *
     * @param signature The signature to subscribe to
     * @param listener The listener to handle notifications
     */
    public void signatureSubscribe(String signature, NotificationEventListener listener) {
        List<Object> params = new ArrayList<>();
        params.add(signature);

        RpcRequest rpcRequest = new RpcRequest("signatureSubscribe", params);
        addSubscription(rpcRequest, listener);
    }

    /**
     * Subscribes to log updates for the given mention.
     *
     * @param mention The mention to subscribe to
     * @param listener The listener to handle notifications
     */
    public void logsSubscribe(String mention, NotificationEventListener listener) {
        logsSubscribe(List.of(mention), listener);
    }

    /**
     * Subscribes to log updates for the given mentions.
     *
     * @param mentions The mentions to subscribe to
     * @param listener The listener to handle notifications
     */
    public void logsSubscribe(List<String> mentions, NotificationEventListener listener) {
        List<Object> params = new ArrayList<>();
        params.add(Map.of("mentions", mentions));
        params.add(Map.of("commitment", "finalized"));

        RpcRequest rpcRequest = new RpcRequest("logsSubscribe", params);
        addSubscription(rpcRequest, listener);
    }

    /**
     * Adds a subscription to the client.
     *
     * @param rpcRequest The RPC request for the subscription
     * @param listener The listener for notification events
     */
    private void addSubscription(RpcRequest rpcRequest, NotificationEventListener listener) {
        subscriptions.put(rpcRequest.getId(), new SubscriptionParams(rpcRequest, listener));
        subscriptionIds.put(rpcRequest.getId(), 0L);
        updateSubscriptions();
    }

    /**
     * Handles the WebSocket connection opening.
     *
     * @param handshakedata The server handshake data
     */
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        LOGGER.info("WebSocket connection opened");
        reconnectDelay = INITIAL_RECONNECT_DELAY;
        updateSubscriptions();
        startHeartbeat();
        connectLatch.countDown();
    }

    /**
     * Handles incoming WebSocket messages.
     *
     * @param message The received message
     */
    @Override
    public void onMessage(String message) {
        try {
            JsonAdapter<RpcResponse<Long>> resultAdapter = moshi.adapter(
                    Types.newParameterizedType(RpcResponse.class, Long.class));
            RpcResponse<Long> rpcResult = resultAdapter.fromJson(message);

            if (rpcResult != null && rpcResult.getId() != null) {
                handleSubscriptionResponse(rpcResult);
            } else {
                handleNotification(message);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error processing message", ex);
        }
    }

    /**
     * Handles subscription responses.
     *
     * @param rpcResult The RPC response
     */
    private void handleSubscriptionResponse(RpcResponse<Long> rpcResult) {
        String rpcResultId = rpcResult.getId();
        if (subscriptionIds.containsKey(rpcResultId)) {
            subscriptionIds.put(rpcResultId, rpcResult.getResult());
            SubscriptionParams params = subscriptions.get(rpcResultId);
            if (params != null) {
                subscriptionListeners.put(rpcResult.getResult(), params.listener);
                subscriptions.remove(rpcResultId);
            }
        }
    }

    /**
     * Handles notification messages.
     *
     * @param message The notification message
     * @throws Exception If an error occurs while processing the notification
     */
    private void handleNotification(String message) throws Exception {
        JsonAdapter<RpcNotificationResult> notificationResultAdapter = moshi.adapter(RpcNotificationResult.class);
        RpcNotificationResult result = notificationResultAdapter.fromJson(message);
        if (result != null) {
            NotificationEventListener listener = subscriptionListeners.get(result.getParams().getSubscription());
            if (listener != null) {
                Map<String, Object> value = (Map<String, Object>) result.getParams().getResult().getValue();
                switch (result.getMethod()) {
                    case "signatureNotification":
                        listener.onNotificationEvent(new SignatureNotification(value.get("err")));
                        break;
                    case "accountNotification":
                    case "logsNotification":
                        listener.onNotificationEvent(value);
                        break;
                }
            }
        }
    }

    /**
     * Handles WebSocket connection closure.
     *
     * @param code The status code indicating why the connection was closed
     * @param reason A human-readable explanation for the closure
     * @param remote Whether the closure was initiated by the remote endpoint
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        LOGGER.info("Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: " + reason);
        stopHeartbeat();
        if (remote || code != CloseFrame.NORMAL) {
            scheduleReconnect();
        }
    }

    /**
     * Handles WebSocket errors.
     *
     * @param ex The exception that describes the error
     */
    @Override
    public void onError(Exception ex) {
        LOGGER.log(Level.SEVERE, "WebSocket error occurred", ex);
    }

    /**
     * Attempts to reconnect to the WebSocket server.
     */
    public void reconnect() {
        LOGGER.info("Attempting to reconnect...");
        try {
            reconnectBlocking();
        } catch (InterruptedException e) {
            LOGGER.warning("Reconnection interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Starts the heartbeat mechanism to keep the connection alive.
     */
    private void startHeartbeat() {
        executor.scheduleAtFixedRate(this::sendPing, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
    }

    /**
     * Stops the heartbeat mechanism.
     */
    private void stopHeartbeat() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Updates all active subscriptions.
     */
    private void updateSubscriptions() {
        if (isOpen()) {
            JsonAdapter<RpcRequest> rpcRequestJsonAdapter = moshi.adapter(RpcRequest.class);
            for (SubscriptionParams sub : subscriptions.values()) {
                send(rpcRequestJsonAdapter.toJson(sub.request));
            }
            for (Map.Entry<String, Long> entry : subscriptionIds.entrySet()) {
                if (entry.getValue() != 0L) {
                    SubscriptionParams params = subscriptions.get(entry.getKey());
                    if (params != null) {
                        send(rpcRequestJsonAdapter.toJson(params.request));
                    }
                }
            }
        }
    }

    /**
     * Schedules a reconnection attempt with exponential backoff.
     */
    private void scheduleReconnect() {
        executor.schedule(() -> {
            reconnect();
            reconnectDelay = Math.min(reconnectDelay * 2, MAX_RECONNECT_DELAY);
        }, reconnectDelay, TimeUnit.MILLISECONDS);
    }

    /**
     * Waits for the WebSocket connection to be established.
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return true if the connection was successfully established, false if the timeout was reached
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    public boolean waitForConnection(long timeout, TimeUnit unit) throws InterruptedException {
        return connectLatch.await(timeout, unit);
    }
}