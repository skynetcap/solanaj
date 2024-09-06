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
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ScheduledFuture;
import java.util.ArrayList;

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

    private static final int MAX_RECONNECT_ATTEMPTS = 10;
    private static final long INITIAL_RECONNECT_INTERVAL = 1000; // 1 second
    private static final long MAX_RECONNECT_INTERVAL = 60000; // 1 minute
    private final AtomicInteger reconnectAttempts = new AtomicInteger(0);

    private ScheduledExecutorService scheduler;

    private static final long PING_INTERVAL = 30000; // 30 seconds
    private ScheduledFuture<?> pingTask;

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
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    // Add this constructor for testing purposes
    protected SubscriptionWebSocketClient(URI serverURI, ScheduledExecutorService scheduler) {
        super(serverURI);
        this.scheduler = scheduler;
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
        LOGGER.fine("WebSocket connection opened");
        resetReconnectAttempts();
        triggerUpdateSubscriptions();
        startPingTask();
    }

    /**
     * Processes incoming WebSocket messages.
     * Handles subscription confirmations and notifications:
     * 1. For subscription confirmations, updates internal mappings.
     * 2. For notifications, calls the appropriate listener.
     * Logs various stages of message processing and any errors encountered.
     *
     * Message structure:
     * - Subscription confirmations: contain both "id" and "result" keys
     * - Notifications: contain both "method" and "params" keys
     *
     * @param message The received message as a JSON string
     */
    @Override
    public void onMessage(String message) {
        LOGGER.fine("Received message: " + message);
        try {
            JsonAdapter<Map<String, Object>> jsonAdapter = moshi.adapter(Types.newParameterizedType(Map.class, String.class, Object.class));
            Map<String, Object> jsonMessage = jsonAdapter.fromJson(message);

            if (jsonMessage != null) {
                if (jsonMessage.containsKey("id") && jsonMessage.containsKey("result")) {
                    handleSubscriptionConfirmation(jsonMessage);
                } else if (jsonMessage.containsKey("method") && jsonMessage.containsKey("params")) {
                    handleNotification(jsonMessage);
                } else {
                    LOGGER.warning("Unrecognized message format: " + message);
                }
            } else {
                LOGGER.warning("Failed to parse message: " + message);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing message: " + e.getMessage(), e);
        }
    }

    /**
     * Handles subscription confirmation messages.
     * Updates internal mappings with the subscription ID and associates the listener.
     *
     * @param jsonMessage The parsed JSON message containing subscription confirmation details
     */
    private void handleSubscriptionConfirmation(Map<String, Object> jsonMessage) {
        String id = (String) jsonMessage.get("id");
        Long subscriptionId = ((Number) jsonMessage.get("result")).longValue();
        LOGGER.fine("Subscription confirmed. ID: " + id + ", Subscription ID: " + subscriptionId);

        SubscriptionParams params = subscriptions.get(id);
        if (params != null) {
            subscriptionIds.put(id, subscriptionId);
            subscriptionListeners.put(subscriptionId, params.listener);
        } else {
            LOGGER.warning("Received confirmation for unknown subscription: " + id);
        }
    }

    /**
     * Handles notification messages for subscriptions.
     * Retrieves the appropriate listener for the subscription and invokes it with the notification data.
     *
     * @param jsonMessage The parsed JSON message containing notification details
     */
    private void handleNotification(Map<String, Object> jsonMessage) {
        Map<String, Object> params = (Map<String, Object>) jsonMessage.get("params");
        Long subscriptionId = ((Number) params.get("subscription")).longValue();
        LOGGER.fine("Received notification for subscription: " + subscriptionId);

        NotificationEventListener listener = subscriptionListeners.get(subscriptionId);
        if (listener != null) {
            listener.onNotificationEvent(params.get("result"));
        } else {
            LOGGER.warning("No listener found for subscription: " + subscriptionId);
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
        stopPingTask();
        
        if (remote && code != 1000) { // 1000 is the normal closure code
            LOGGER.info("Unexpected closure. Attempting to reconnect...");
            scheduleReconnectWithBackoff();
        } else {
            resetReconnectAttempts();
        }
    }

    /**
     * Handles WebSocket errors.
     * Logs the error and schedules a reconnection attempt.
     * @param ex The exception that occurred
     */
    @Override
    public void onError(Exception ex) {
        LOGGER.log(Level.SEVERE, "WebSocket error: " + ex.getMessage(), ex);
        scheduleReconnectWithBackoff();
    }

    /**
     * Schedules a reconnection attempt with exponential backoff.
     */
    private void scheduleReconnectWithBackoff() {
        int attempts = reconnectAttempts.incrementAndGet();
        if (attempts <= MAX_RECONNECT_ATTEMPTS) {
            long delay = Math.min(INITIAL_RECONNECT_INTERVAL * (long) Math.pow(2, attempts - 1), MAX_RECONNECT_INTERVAL);
            scheduler.schedule(this::reconnect, delay, TimeUnit.MILLISECONDS);
        } else {
            LOGGER.severe("Max reconnection attempts reached. Giving up.");
        }
    }

    /**
     * Resets the reconnection attempt counter.
     */
    private void resetReconnectAttempts() {
        reconnectAttempts.set(0);
    }

    /**
     * Attempts to reconnect the WebSocket.
     * @throws InterruptedException if the reconnection is interrupted
     */
    @Override
    public void reconnect() {
        if (!isOpen()) {
            try {
                connectBlocking();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.WARNING, "Reconnection interrupted", e);
            }
        }
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

    /**
     * Unsubscribes from a specific subscription.
     * @param subscriptionId The ID of the subscription to unsubscribe from
     * @return A CompletableFuture that completes when the unsubscription is confirmed
     */
    public CompletableFuture<Void> unsubscribe(Long subscriptionId) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        RpcRequest rpcRequest = new RpcRequest("unsubscribe", Arrays.asList(subscriptionId));
        send(moshi.adapter(RpcRequest.class).toJson(rpcRequest));
        
        // Remove the subscription from our maps
        subscriptionListeners.remove(subscriptionId);
        subscriptionIds.values().removeIf(id -> id.equals(subscriptionId));
        subscriptions.entrySet().removeIf(entry -> entry.getValue().request.getMethod().endsWith("unsubscribe"));

        future.complete(null);
        return future;
    }

    /**
     * Clears all subscriptions.
     */
    public void clearAllSubscriptions() {
        for (Long subscriptionId : new ArrayList<>(subscriptionListeners.keySet())) {
            unsubscribe(subscriptionId);
        }
        subscriptions.clear();
        subscriptionIds.clear();
        subscriptionListeners.clear();
        pendingSubscriptions.clear();
    }

    /**
     * Sends a custom ping message to keep the WebSocket connection alive.
     * This method wraps the inherited sendPing() method with error handling.
     */
    private void sendCustomPing() {  
        if (isOpen()) {
            try {
                sendPing();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to send ping", e);
            }
        }
    }

    /**
     * Starts a scheduled task to send periodic pings to the server.
     * This helps keep the WebSocket connection alive.
     */
    private void startPingTask() {
        stopPingTask();
        pingTask = scheduler.scheduleAtFixedRate(this::sendCustomPing, PING_INTERVAL, PING_INTERVAL, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops the scheduled ping task if it's running.
     */
    private void stopPingTask() {
        if (pingTask != null && !pingTask.isCancelled()) {
            pingTask.cancel(true);
        }
    }

    /**
     * Closes the WebSocket connection and performs cleanup.
     * This method overrides the close() method from WebSocketClient.
     */
    @Override
    public void close() {
        stopPingTask();
        clearAllSubscriptions();
        super.close();
    }
}
