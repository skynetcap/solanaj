package org.p2p.solanaj.ws;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.p2p.solanaj.rpc.types.config.Commitment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.p2p.solanaj.rpc.types.RpcRequest;
import org.p2p.solanaj.ws.listeners.NotificationEventListener;

/**
 * SubscriptionWebSocketClient is a WebSocket client for managing subscriptions to various Solana events.
 * 
 * This class allows users to subscribe to different types of notifications from the Solana blockchain, 
 * such as account updates, block updates, program updates, and vote updates. Each subscription is 
 * identified by a unique subscription ID, which is generated when a subscription request is made. 
 * The client maintains a mapping of these subscription IDs to their corresponding parameters and 
 * notification listeners, enabling efficient management of active subscriptions.
 * 
 * Users can specify various parameters for their subscriptions, including the commitment level 
 * (e.g., FINALIZED, CONFIRMED) and the encoding format (e.g., jsonParsed, base64) for the data 
 * received in notifications. The client handles incoming WebSocket messages, processes notifications, 
 * and invokes the appropriate listener callbacks with the received data.
 * 
 * The class also provides methods for unsubscribing from notifications, ensuring that resources 
 * are properly released when subscriptions are no longer needed. Thread safety is maintained 
 * through the use of concurrent data structures and proper synchronization.
 * 
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * // Create a WebSocket client
 * SubscriptionWebSocketClient client = SubscriptionWebSocketClient.getInstance("wss://api.devnet.solana.com");
 * 
 * // Subscribe to logs and get the subscription ID
 * CompletableFuture<String> subscriptionFuture = client.logsSubscribe("So11111111111111111111111111111111111111112", 
 *     data -> {
 *         System.out.println("Received log: " + data);
 *     });
 * 
 * // Wait for subscription to be established
 * String subscriptionId = subscriptionFuture.get(10, TimeUnit.SECONDS);
 * System.out.println("Subscription ID: " + subscriptionId);
 * 
 * // Later, unsubscribe using the subscription ID
 * client.unsubscribe(subscriptionId);
 * 
 * // Or unsubscribe by account
 * String id = client.getSubscriptionId("So11111111111111111111111111111111111111112");
 * if (id != null) {
 *     client.unsubscribe(id);
 * }
 * }</pre>
 */
public class SubscriptionWebSocketClient {

    private static final Logger LOGGER = Logger.getLogger(SubscriptionWebSocketClient.class.getName());
    private static final int MAX_RECONNECT_DELAY = 30000;
    private static final int INITIAL_RECONNECT_DELAY = 1000;
    private static final int CONNECTION_TIMEOUT = 10;

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String endpoint;
    
    private WebSocket webSocket;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final AtomicBoolean isConnecting = new AtomicBoolean(false);
    private final AtomicBoolean shouldReconnect = new AtomicBoolean(true);
    
    private final Map<String, SubscriptionInfo> subscriptions = new ConcurrentHashMap<>();
    private final Map<Long, NotificationEventListener> subscriptionListeners = new ConcurrentHashMap<>();
    private final Map<Long, String> subscriptionToAccount = new ConcurrentHashMap<>();
    private final AtomicLong requestIdCounter = new AtomicLong(1);
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final CountDownLatch connectLatch = new CountDownLatch(1);
    
    private int reconnectDelay = INITIAL_RECONNECT_DELAY;

    /**
     * Inner class to hold subscription information.
     */
    private static class SubscriptionInfo {
        final RpcRequest request;
        final NotificationEventListener listener;
        final String method;
        final String unsubscribeMethod;
        final CompletableFuture<String> subscriptionFuture;

        SubscriptionInfo(RpcRequest request, NotificationEventListener listener, String method, String unsubscribeMethod, CompletableFuture<String> subscriptionFuture) {
            this.request = request;
            this.listener = listener;
            this.method = method;
            this.unsubscribeMethod = unsubscribeMethod;
            this.subscriptionFuture = subscriptionFuture;
        }
    }

    /**
     * Custom RpcRequest that allows setting the ID.
     */
    private static class CustomRpcRequest extends RpcRequest {
        private String id;

        public CustomRpcRequest(String method, List<Object> params) {
            super(method, params);
        }

        public void setId(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id != null ? id : super.getId();
        }
    }

    /**
     * Creates a SubscriptionWebSocketClient instance with the exact path provided.
     *
     * @param endpoint The WebSocket endpoint URL
     * @return A new SubscriptionWebSocketClient instance
     */
    public static SubscriptionWebSocketClient getExactPathInstance(String endpoint) {
        return new SubscriptionWebSocketClient(endpoint);
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
            String wsEndpoint = scheme + "://" + endpointURI.getHost();
            return new SubscriptionWebSocketClient(wsEndpoint);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid endpoint URI", e);
        }
    }

    /**
     * Constructs a SubscriptionWebSocketClient with the given endpoint.
     *
     * @param endpoint The WebSocket endpoint URL
     */
    public SubscriptionWebSocketClient(String endpoint) {
        this.endpoint = endpoint;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.SECONDS) // No read timeout for WebSocket
                .writeTimeout(0, TimeUnit.SECONDS) // No write timeout for WebSocket
                .build();
        
        connect();
    }

    /**
     * Connects to the WebSocket server.
     */
    public void connect() {
        if (isConnecting.get() || isConnected.get()) {
            return;
        }
        
        isConnecting.set(true);
        LOGGER.info("Connecting to WebSocket endpoint: " + endpoint);
        
        Request request = new Request.Builder()
                .url(endpoint)
                .build();
                
        webSocket = httpClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                LOGGER.info("WebSocket connection opened");
                isConnected.set(true);
                isConnecting.set(false);
                reconnectDelay = INITIAL_RECONNECT_DELAY;
                connectLatch.countDown();
                startHeartbeat();
                resubscribeAll();
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                handleMessage(text);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                LOGGER.info("WebSocket closing: " + code + " - " + reason);
                isConnected.set(false);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                LOGGER.info("WebSocket closed: " + code + " - " + reason);
                isConnected.set(false);
                stopHeartbeat();
                if (shouldReconnect.get()) {
                    scheduleReconnect();
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                LOGGER.log(Level.SEVERE, "WebSocket connection failed", t);
                isConnected.set(false);
                isConnecting.set(false);
                stopHeartbeat();
                if (shouldReconnect.get()) {
                    scheduleReconnect();
                }
            }
        });
    }

    /**
     * Handles incoming WebSocket messages.
     *
     * @param message The received message
     */
    private void handleMessage(String message) {
        try {
            JsonNode messageNode = objectMapper.readTree(message);
            
            // Check if this is a subscription confirmation
            if (messageNode.has("result") && messageNode.get("result").isInt()) {
                int subscriptionId = messageNode.get("result").asInt();
                String requestId = messageNode.has("id") ? messageNode.get("id").asText() : null;
                
                if (requestId != null) {
                    SubscriptionInfo info = subscriptions.get(requestId);
                    if (info != null) {
                        subscriptionListeners.put((long) subscriptionId, info.listener);
                        subscriptions.remove(requestId);
                        
                        // Track the account for this subscription
                        String account = extractAccountFromRequest((CustomRpcRequest) info.request);
                        if (account != null) {
                            subscriptionToAccount.put((long) subscriptionId, account);
                        }
                        
                        // Complete the future with the subscription ID
                        info.subscriptionFuture.complete(String.valueOf(subscriptionId));
                        
                        LOGGER.info("Subscription established with ID: " + subscriptionId + " for account: " + account);
                    }
                }
                return;
            }
            
            // Check if this is an error
            if (messageNode.has("error")) {
                JsonNode error = messageNode.get("error");
                LOGGER.severe("RPC Error: " + error);
                return;
            }
            
            // Check if this is a notification
            if (messageNode.has("method")) {
                handleNotification(messageNode);
            }
            
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error processing message", ex);
        }
    }


    /**
     * Extracts the account from a subscription request.
     *
     * @param request The subscription request
     * @return The account address, or null if not found
     */
    private String extractAccountFromRequest(CustomRpcRequest request) {
        try {
            String method = request.getMethod();
            List<Object> params = request.getParams();
            
            if (params == null || params.isEmpty()) {
                return null;
            }
            
            switch (method) {
                case "accountSubscribe":
                    return (String) params.get(0);
                case "logsSubscribe":
                    if (params.get(0) instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> mentionsMap = (Map<String, Object>) params.get(0);
                        if (mentionsMap.containsKey("mentions")) {
                            @SuppressWarnings("unchecked")
                            List<String> mentions = (List<String>) mentionsMap.get("mentions");
                            return mentions.isEmpty() ? null : mentions.get(0);
                        }
                    }
                    return null;
                case "signatureSubscribe":
                    return (String) params.get(0);
                case "programSubscribe":
                    return (String) params.get(0);
                default:
                    return null;
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error extracting account from request", ex);
            return null;
        }
    }

    /**
     * Handles notification messages.
     *
     * @param messageNode The notification message as JsonNode
     */
    private void handleNotification(JsonNode messageNode) {
        try {
            String method = messageNode.get("method").asText();
            JsonNode params = messageNode.get("params");
            
            if (params != null && params.has("subscription")) {
                Long subscriptionId = params.get("subscription").asLong();
                NotificationEventListener listener = subscriptionListeners.get(subscriptionId);
                
                if (listener != null) {
                    JsonNode result = params.get("result");
                    
                    switch (method) {
                        case "signatureNotification":
                            if (result != null && result.has("value")) {
                                JsonNode value = result.get("value");
                                Object err = value.has("err") ? value.get("err") : null;
                                listener.onNotificationEvent(new SignatureNotification(err));
                            }
                            break;
                        case "accountNotification":
                        case "logsNotification":
                        case "blockNotification":
                        case "programNotification":
                        case "rootNotification":
                        case "slotNotification":
                        case "slotsUpdatesNotification":
                        case "voteNotification":
                            if (result != null && result.has("value")) {
                                JsonNode value = result.get("value");
                                // Convert JsonNode to Map for compatibility
                                @SuppressWarnings("unchecked")
                                Map<String, Object> valueMap = objectMapper.convertValue(value, Map.class);
                                listener.onNotificationEvent(valueMap);
                            }
                            break;
                        default:
                            LOGGER.warning("Unknown notification method: " + method);
                    }
                } else {
                    LOGGER.warning("No listener found for subscription ID: " + subscriptionId);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error handling notification", ex);
        }
    }

    /**
     * Subscribes to account updates for the given key with specified commitment level and encoding.
     *
     * @param key The account key to subscribe to
     * @param listener The listener to handle notifications
     * @param commitment The commitment level for the subscription
     * @param encoding The encoding format for Account data
     * @return A CompletableFuture that will complete with the subscription ID when the subscription is established
     */
    public CompletableFuture<String> accountSubscribe(String key, NotificationEventListener listener, Commitment commitment, String encoding) {
        List<Object> params = new ArrayList<>();
        params.add(key);
        params.add(Map.of("encoding", encoding, "commitment", commitment.getValue()));

        CustomRpcRequest rpcRequest = new CustomRpcRequest("accountSubscribe", params);
        return addSubscription(rpcRequest, listener, "accountSubscribe", "accountUnsubscribe");
    }

    // Overload methods to maintain backwards compatibility
    public CompletableFuture<String> accountSubscribe(String key, NotificationEventListener listener, Commitment commitment) {
        return accountSubscribe(key, listener, commitment, "jsonParsed");
    }

    public CompletableFuture<String> accountSubscribe(String key, NotificationEventListener listener) {
        return accountSubscribe(key, listener, Commitment.FINALIZED, "jsonParsed");
    }

    /**
     * Subscribes to signature updates for the given signature.
     *
     * @param signature The signature to subscribe to
     * @param listener The listener to handle notifications
     * @return A CompletableFuture that will complete with the subscription ID when the subscription is established
     */
    public CompletableFuture<String> signatureSubscribe(String signature, NotificationEventListener listener) {
        List<Object> params = new ArrayList<>();
        params.add(signature);

        CustomRpcRequest rpcRequest = new CustomRpcRequest("signatureSubscribe", params);
        return addSubscription(rpcRequest, listener, "signatureSubscribe", "signatureUnsubscribe");
    }

    /**
     * Subscribes to log updates for the given mention.
     *
     * @param mention The mention to subscribe to
     * @param listener The listener to handle notifications
     * @return A CompletableFuture that will complete with the subscription ID when the subscription is established
     */
    public CompletableFuture<String> logsSubscribe(String mention, NotificationEventListener listener) {
        return logsSubscribe(List.of(mention), listener);
    }

    /**
     * Subscribes to log updates for the given mentions.
     *
     * @param mentions The mentions to subscribe to
     * @param listener The listener to handle notifications
     * @return A CompletableFuture that will complete with the subscription ID when the subscription is established
     */
    public CompletableFuture<String> logsSubscribe(List<String> mentions, NotificationEventListener listener) {
        List<Object> params = new ArrayList<>();
        params.add(Map.of("mentions", mentions));
        params.add(Map.of("commitment", "processed"));

        CustomRpcRequest rpcRequest = new CustomRpcRequest("logsSubscribe", params);
        return addSubscription(rpcRequest, listener, "logsSubscribe", "logsUnsubscribe");
    }

    /**
     * Subscribes to block updates.
     *
     * @param listener The listener to handle notifications
     * @param commitment The commitment level for the subscription
     * @param encoding The encoding format for block data
     * @return A CompletableFuture that will complete with the subscription ID when the subscription is established
     */
    public CompletableFuture<String> blockSubscribe(NotificationEventListener listener, Commitment commitment, String encoding) {
        List<Object> params = new ArrayList<>();
        params.add(Map.of("encoding", encoding, "commitment", commitment.getValue()));

        CustomRpcRequest rpcRequest = new CustomRpcRequest("blockSubscribe", params);
        return addSubscription(rpcRequest, listener, "blockSubscribe", "blockUnsubscribe");
    }

    public CompletableFuture<String> blockSubscribe(NotificationEventListener listener, Commitment commitment) {
        return blockSubscribe(listener, commitment, "json");
    }

    public CompletableFuture<String> blockSubscribe(NotificationEventListener listener) {
        return blockSubscribe(listener, Commitment.FINALIZED, "json");
    }

    /**
     * Subscribes to program updates.
     *
     * @param programId The program ID to subscribe to
     * @param listener The listener to handle notifications
     * @param commitment The commitment level for the subscription
     * @param encoding The encoding format for program data
     * @return A CompletableFuture that will complete with the subscription ID when the subscription is established
     */
    public CompletableFuture<String> programSubscribe(String programId, NotificationEventListener listener, Commitment commitment, String encoding) {
        List<Object> params = new ArrayList<>();
        params.add(programId);
        params.add(Map.of("encoding", encoding, "commitment", commitment.getValue()));

        CustomRpcRequest rpcRequest = new CustomRpcRequest("programSubscribe", params);
        return addSubscription(rpcRequest, listener, "programSubscribe", "programUnsubscribe");
    }

    public CompletableFuture<String> programSubscribe(String programId, NotificationEventListener listener, Commitment commitment) {
        return programSubscribe(programId, listener, commitment, "base64");
    }

    public CompletableFuture<String> programSubscribe(String programId, NotificationEventListener listener) {
        return programSubscribe(programId, listener, Commitment.FINALIZED, "base64");
    }

    /**
     * Subscribes to root updates.
     *
     * @param listener The listener to handle notifications
     * @return A CompletableFuture that will complete with the subscription ID when the subscription is established
     */
    public CompletableFuture<String> rootSubscribe(NotificationEventListener listener) {
        CustomRpcRequest rpcRequest = new CustomRpcRequest("rootSubscribe", new ArrayList<>());
        return addSubscription(rpcRequest, listener, "rootSubscribe", "rootUnsubscribe");
    }

    /**
     * Subscribes to slot updates.
     *
     * @param listener The listener to handle notifications
     * @return A CompletableFuture that will complete with the subscription ID when the subscription is established
     */
    public CompletableFuture<String> slotSubscribe(NotificationEventListener listener) {
        CustomRpcRequest rpcRequest = new CustomRpcRequest("slotSubscribe", new ArrayList<>());
        return addSubscription(rpcRequest, listener, "slotSubscribe", "slotUnsubscribe");
    }

    /**
     * Subscribes to slots updates.
     *
     * @param listener The listener to handle notifications
     * @return A CompletableFuture that will complete with the subscription ID when the subscription is established
     */
    public CompletableFuture<String> slotsUpdatesSubscribe(NotificationEventListener listener) {
        CustomRpcRequest rpcRequest = new CustomRpcRequest("slotsUpdatesSubscribe", new ArrayList<>());
        return addSubscription(rpcRequest, listener, "slotsUpdatesSubscribe", "slotsUpdatesUnsubscribe");
    }

    /**
     * Subscribes to vote updates.
     *
     * @param listener The listener to handle notifications
     * @return A CompletableFuture that will complete with the subscription ID when the subscription is established
     */
    public CompletableFuture<String> voteSubscribe(NotificationEventListener listener) {
        CustomRpcRequest rpcRequest = new CustomRpcRequest("voteSubscribe", new ArrayList<>());
        return addSubscription(rpcRequest, listener, "voteSubscribe", "voteUnsubscribe");
    }

    /**
     * Adds a subscription to the client.
     *
     * @param rpcRequest The RPC request for the subscription
     * @param listener The listener for notification events
     * @param method The subscription method name
     * @param unsubscribeMethod The unsubscribe method name
     * @return A CompletableFuture that will complete with the subscription ID when the subscription is established
     */
    private CompletableFuture<String> addSubscription(CustomRpcRequest rpcRequest, NotificationEventListener listener, String method, String unsubscribeMethod) {
        String requestId = String.valueOf(requestIdCounter.getAndIncrement());
        rpcRequest.setId(requestId);
        
        CompletableFuture<String> subscriptionFuture = new CompletableFuture<>();
        
        SubscriptionInfo info = new SubscriptionInfo(rpcRequest, listener, method, unsubscribeMethod, subscriptionFuture);
        subscriptions.put(requestId, info);
        
        if (isConnected.get()) {
            sendRequest(rpcRequest);
        }
        
        return subscriptionFuture;
    }

    /**
     * Sends an RPC request over the WebSocket connection.
     *
     * @param request The RPC request to send
     */
    private void sendRequest(CustomRpcRequest request) {
        if (webSocket != null && isConnected.get()) {
            try {
                String json = objectMapper.writeValueAsString(request);
                LOGGER.info("Sending WebSocket request: " + json);
                webSocket.send(json);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Error sending WebSocket request", ex);
            }
        }
    }

    /**
     * Unsubscribes from a subscription.
     *
     * @param subscriptionId The subscription ID to unsubscribe from
     */
    public void unsubscribe(String subscriptionId) {
        Long subId = Long.parseLong(subscriptionId);
        NotificationEventListener listener = subscriptionListeners.remove(subId);
        String account = subscriptionToAccount.remove(subId);
        
        if (listener != null) {
            List<Object> params = new ArrayList<>();
            params.add(subId);
            
            // Find the unsubscribe method for this subscription
            String unsubscribeMethod = "accountUnsubscribe"; // Default
            for (SubscriptionInfo info : subscriptions.values()) {
                if (info.listener == listener) {
                    unsubscribeMethod = info.unsubscribeMethod;
                    break;
                }
            }
            
            CustomRpcRequest unsubRequest = new CustomRpcRequest(unsubscribeMethod, params);
            unsubRequest.setId(String.valueOf(requestIdCounter.getAndIncrement()));
            
            sendRequest(unsubRequest);
            LOGGER.info("Unsubscribed from subscription: " + subscriptionId + " for account: " + account);
        } else {
            LOGGER.warning("Attempted to unsubscribe from non-existent subscription: " + subscriptionId);
        }
    }

    /**
     * Gets the subscription ID for a given account.
     *
     * @param account The account to get the subscription ID for
     * @return The subscription ID, or null if not found
     */
    public String getSubscriptionId(String account) {
        for (Map.Entry<Long, String> entry : subscriptionToAccount.entrySet()) {
            if (account.equals(entry.getValue())) {
                return String.valueOf(entry.getKey());
            }
        }
        return null;
    }

    /**
     * Starts the heartbeat mechanism to keep the connection alive.
     */
    private void startHeartbeat() {
        // Solana RPC doesn't support ping/pong, so we don't send heartbeat messages
        // The connection will be kept alive by OkHttp's built-in mechanisms
        LOGGER.info("Heartbeat started (using OkHttp's built-in keep-alive)");
    }

    /**
     * Stops the heartbeat mechanism.
     */
    private void stopHeartbeat() {
        // Heartbeat is managed by the scheduler, no specific stop needed
    }

    /**
     * Schedules a reconnection attempt with exponential backoff.
     */
    private void scheduleReconnect() {
        scheduler.schedule(() -> {
            if (shouldReconnect.get() && !isConnected.get()) {
                LOGGER.info("Attempting to reconnect...");
                connect();
                reconnectDelay = Math.min(reconnectDelay * 2, MAX_RECONNECT_DELAY);
            }
        }, reconnectDelay, TimeUnit.MILLISECONDS);
    }

    /**
     * Resubscribes to all active subscriptions after reconnection.
     */
    private void resubscribeAll() {
        LOGGER.info("Resubscribing to all active subscriptions");
        Map<String, SubscriptionInfo> currentSubscriptions = new ConcurrentHashMap<>(subscriptions);
        subscriptions.clear();
        
        for (SubscriptionInfo info : currentSubscriptions.values()) {
            String requestId = String.valueOf(requestIdCounter.getAndIncrement());
            CustomRpcRequest newRequest = new CustomRpcRequest(info.request.getMethod(), info.request.getParams());
            newRequest.setId(requestId);
            
            // Create a new CompletableFuture for the resubscription
            CompletableFuture<String> newFuture = new CompletableFuture<>();
            SubscriptionInfo newInfo = new SubscriptionInfo(newRequest, info.listener, info.method, info.unsubscribeMethod, newFuture);
            subscriptions.put(requestId, newInfo);
            sendRequest(newRequest);
        }
    }

    /**
     * Attempts to reconnect to the WebSocket server.
     */
    public void reconnect() {
        shouldReconnect.set(true);
        if (webSocket != null) {
            webSocket.close(1000, "Reconnecting");
        }
        connect();
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

    /**
     * Checks if the WebSocket connection is open.
     *
     * @return true if the connection is open, false otherwise
     */
    public boolean isOpen() {
        return isConnected.get();
    }

    /**
     * Closes the WebSocket connection.
     */
    public void close() {
        shouldReconnect.set(false);
        isConnected.set(false);
        
        if (webSocket != null) {
            webSocket.close(1000, "Client closing");
            webSocket = null;
        }
        
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        httpClient.dispatcher().executorService().shutdown();
        try {
            if (!httpClient.dispatcher().executorService().awaitTermination(5, TimeUnit.SECONDS)) {
                httpClient.dispatcher().executorService().shutdownNow();
            }
        } catch (InterruptedException e) {
            httpClient.dispatcher().executorService().shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}