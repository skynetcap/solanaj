package org.p2p.solanaj.ws;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CompletableFuture;
import java.util.Queue;
import java.util.Arrays;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.p2p.solanaj.rpc.types.RpcRequest;
import org.p2p.solanaj.rpc.types.config.Commitment;
import org.p2p.solanaj.ws.listeners.NotificationEventListener;

public class SubscriptionWebSocketClient extends WebSocketClient {

    private class SubscriptionParams {
        RpcRequest request;
        NotificationEventListener listener;

        SubscriptionParams(RpcRequest request, NotificationEventListener listener) {
            this.request = request;
            this.listener = listener;
        }
    }

    private Map<String, SubscriptionParams> subscriptions = new ConcurrentHashMap<>();
    private Map<String, Long> subscriptionIds = new ConcurrentHashMap<>();
    private Map<Long, NotificationEventListener> subscriptionListeners = new ConcurrentHashMap<>();
    private static final Logger LOGGER = Logger.getLogger(SubscriptionWebSocketClient.class.getName());
    private final Queue<SubscriptionParams> pendingSubscriptions = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isUpdatingSubscriptions = new AtomicBoolean(false);
    private final Moshi moshi = new Moshi.Builder().build();

    /**
     * Creates a WebSocket client instance with the exact endpoint provided.
     * @param endpoint The exact WebSocket endpoint URL
     * @return A connected SubscriptionWebSocketClient instance
     * @throws IllegalArgumentException if the endpoint is invalid
     */
    public static SubscriptionWebSocketClient getExactPathInstance(String endpoint) {
        URI serverURI;
        SubscriptionWebSocketClient instance;

        try {
            serverURI = new URI(endpoint);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }

        instance = new SubscriptionWebSocketClient(serverURI);

        if (!instance.isOpen()) {
            instance.connect();
        }

        return instance;
    }

    /**
     * Creates a WebSocket client instance by converting an HTTP(S) endpoint to its WebSocket equivalent.
     * @param endpoint The HTTP(S) endpoint to convert and connect to
     * @return A connected SubscriptionWebSocketClient instance
     * @throws IllegalArgumentException if the endpoint is invalid
     */
    public static SubscriptionWebSocketClient getInstance(String endpoint) {
        URI serverURI;
        URI endpointURI;
        SubscriptionWebSocketClient instance;

        try {
            endpointURI = new URI(endpoint);
            serverURI = new URI(endpointURI.getScheme() == "https" ? "wss" : "ws" + "://" + endpointURI.getHost());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }

        instance = new SubscriptionWebSocketClient(serverURI);

        if (!instance.isOpen()) {
            instance.connect();
        }

        return instance;
    }

    /**
     * Constructs a SubscriptionWebSocketClient with the given server URI.
     * @param serverURI The URI of the WebSocket server
     */
    public SubscriptionWebSocketClient(URI serverURI) {
        super(serverURI);
    }

    /**
     * Subscribes to account updates for a specific public key.
     * @param key The public key of the account to monitor
     * @param listener The callback to handle incoming notifications
     */
    public void accountSubscribe(String key, NotificationEventListener listener) {
        queueSubscription("accountSubscribe", Arrays.asList(
            key,
            Map.of("encoding", "jsonParsed", "commitment", Commitment.PROCESSED.getValue())
        ), listener);
    }

    /**
     * Subscribes to updates for a specific transaction signature.
     * @param signature The transaction signature to monitor
     * @param listener The callback to handle incoming notifications
     */
    public void signatureSubscribe(String signature, NotificationEventListener listener) {
        queueSubscription("signatureSubscribe", Arrays.asList(signature), listener);
    }

    /**
     * Subscribes to log messages mentioning a specific address.
     * @param mention The address to monitor in log messages
     * @param listener The callback to handle incoming notifications
     */
    public void logsSubscribe(String mention, NotificationEventListener listener) {
        queueSubscription("logsSubscribe", Arrays.asList(
            Map.of("mentions", List.of(mention)),
            Map.of("commitment", "finalized")
        ), listener);
    }

    /**
     * Subscribes to log messages mentioning any of the provided addresses.
     * @param mentions List of addresses to monitor in log messages
     * @param listener The callback to handle incoming notifications
     */
    public void logsSubscribe(List<String> mentions, NotificationEventListener listener) {
        queueSubscription("logsSubscribe", Arrays.asList(
            Map.of("mentions", mentions),
            Map.of("commitment", "finalized")
        ), listener);
    }

    /**
     * Handles the WebSocket connection opening.
     * Logs the event and triggers subscription updates.
     * @param handshakedata Server handshake data
     */
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        LOGGER.fine("Websocket connection opened");
        triggerUpdateSubscriptions();
    }

    /**
     * Processes incoming WebSocket messages.
     * Handles subscription confirmations and notifications:
     * 1. For subscription confirmations, updates internal mappings.
     * 2. For notifications, calls the appropriate listener.
     * Logs various stages of message processing and any errors encountered.
     * @param message The received message as a JSON string
     */
    @Override
    public void onMessage(String message) {
        LOGGER.fine("Received message: " + message);

        try {
            JsonAdapter<Map<String, Object>> jsonAdapter = moshi.adapter(Types.newParameterizedType(Map.class, String.class, Object.class));
            Map<String, Object> jsonMessage = jsonAdapter.fromJson(message);

            if (jsonMessage.containsKey("id")) {
                // This is a subscription confirmation
                String rpcResultId = (String) jsonMessage.get("id");
                LOGGER.fine("Processing subscription confirmation for ID: " + rpcResultId);
                if (subscriptionIds.containsKey(rpcResultId)) {
                    Long subscriptionId = ((Number) jsonMessage.get("result")).longValue();
                    subscriptionIds.put(rpcResultId, subscriptionId);
                    SubscriptionParams params = subscriptions.get(rpcResultId);
                    if (params != null) {
                        subscriptionListeners.put(subscriptionId, params.listener);
                        LOGGER.fine("Subscription confirmed. ID: " + rpcResultId + ", Subscription: " + subscriptionId);
                    } else {
                        LOGGER.warning("No subscription params found for ID: " + rpcResultId);
                    }
                } else {
                    LOGGER.warning("Received confirmation for unknown subscription ID: " + rpcResultId);
                }
            } else if (jsonMessage.containsKey("method") && jsonMessage.containsKey("params")) {
                // This is a notification
                LOGGER.fine("Processing notification");
                Map<String, Object> params = (Map<String, Object>) jsonMessage.get("params");
                Long subscriptionId = ((Number) params.get("subscription")).longValue();
                NotificationEventListener listener = subscriptionListeners.get(subscriptionId);

                if (listener != null) {
                    LOGGER.fine("Calling listener for subscription: " + subscriptionId);
                    Map<String, Object> value = (Map<String, Object>) ((Map<String, Object>) params.get("result")).get("value");
                    listener.onNotificationEvent(value);
                } else {
                    LOGGER.warning("No listener found for subscription: " + subscriptionId);
                }
            } else {
                LOGGER.warning("Received unknown message format: " + message);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error processing message: " + ex.getMessage(), ex);
        }
    }

    /**
     * Handles WebSocket connection closure.
     * Logs the closure details including the reason and whether it was initiated remotely.
     * @param code The status code indicating the reason for closure
     * @param reason A human-readable explanation for the closure
     * @param remote Whether the closure was initiated by the remote endpoint
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        LOGGER.fine("Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: " + reason);
    }

    /**
     * Handles WebSocket errors.
     * Logs the error and schedules a reconnection attempt.
     * @param ex The exception that occurred
     */
    @Override
    public void onError(Exception ex) {
        LOGGER.log(Level.SEVERE, "WebSocket error: " + ex.getMessage(), ex);
        scheduleReconnect();
    }

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * Schedules a reconnection attempt after a delay.
     */
    private void scheduleReconnect() {
        scheduler.schedule(this::reconnect, 5, TimeUnit.SECONDS);
    }

    /**
     * Attempts to reconnect the WebSocket.
     */
    public void reconnect() {
        connect();
    }

    /**
     * Triggers the processing of pending subscriptions.
     * Ensures that only one thread processes subscriptions at a time.
     */
    private void triggerUpdateSubscriptions() {
        if (isOpen() && isUpdatingSubscriptions.compareAndSet(false, true)) {
            CompletableFuture.runAsync(this::processSubscriptions);
        }
    }

    /**
     * Processes pending subscriptions asynchronously.
     * Sends each pending subscription request to the server.
     * If a send fails, it re-queues the subscription and stops processing.
     * After processing, it checks if there are more pending subscriptions and triggers another update if necessary.
     */
    private void processSubscriptions() {
        try {
            JsonAdapter<RpcRequest> rpcRequestJsonAdapter = moshi.adapter(RpcRequest.class);
            SubscriptionParams subParams;
            while ((subParams = pendingSubscriptions.poll()) != null) {
                try {
                    send(rpcRequestJsonAdapter.toJson(subParams.request));
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to send subscription: " + e.getMessage(), e);
                    pendingSubscriptions.offer(subParams); // Re-queue failed subscriptions
                    break; // Stop processing on first error
                }
            }
        } finally {
            isUpdatingSubscriptions.set(false);
            if (!pendingSubscriptions.isEmpty()) {
                triggerUpdateSubscriptions(); // Retry if there are still pending subscriptions
            }
        }
    }

    /**
     * Queues a new subscription request.
     * Creates an RPC request for the subscription and adds it to the pending queue.
     * Updates internal mappings and triggers subscription processing.
     * @param method The RPC method for the subscription
     * @param params The parameters for the subscription
     * @param listener The listener to handle notifications for this subscription
     */
    private void queueSubscription(String method, List<Object> params, NotificationEventListener listener) {
        RpcRequest rpcRequest = new RpcRequest(method, params);
        SubscriptionParams subParams = new SubscriptionParams(rpcRequest, listener);
        pendingSubscriptions.offer(subParams);
        subscriptions.put(rpcRequest.getId(), subParams);
        subscriptionIds.put(rpcRequest.getId(), 0L);
        triggerUpdateSubscriptions();
    }

    /**
     * Getter for the subscriptions map.
     * @return The map of subscription IDs to SubscriptionParams
     */
    public Map<String, SubscriptionParams> getSubscriptions() {
        return subscriptions;
    }

    /**
     * Getter for the subscriptionIds map.
     * @return The map of RPC request IDs to subscription IDs
     */
    public Map<String, Long> getSubscriptionIds() {
        return subscriptionIds;
    }

    /**
     * Getter for the subscriptionListeners map.
     * @return The map of subscription IDs to NotificationEventListeners
     */
    public Map<Long, NotificationEventListener> getSubscriptionListeners() {
        return subscriptionListeners;
    }
}
