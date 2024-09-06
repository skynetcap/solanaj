package org.p2p.solanaj.ws;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.concurrent.CountDownLatch;

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
    private static final int MAX_RECONNECT_DELAY = 30000;
    private static final int INITIAL_RECONNECT_DELAY = 1000;
    private int reconnectDelay = INITIAL_RECONNECT_DELAY;
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final CountDownLatch connectLatch = new CountDownLatch(1);

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
        instance.connect();

        return instance;
    }

    public SubscriptionWebSocketClient(URI serverURI) {
        super(serverURI);

    }

    /**
     * For example, used to "listen" to an private key's "tweets"
     * By accountSubscribing to their private key(s)
     *
     * @param key
     * @param listener
     */
    public void accountSubscribe(String key, NotificationEventListener listener) {
        List<Object> params = new ArrayList<>();
        params.add(key);
        params.add(Map.of("encoding", "jsonParsed", "commitment", Commitment.PROCESSED.getValue()));

        RpcRequest rpcRequest = new RpcRequest("accountSubscribe", params);

        subscriptions.put(rpcRequest.getId(), new SubscriptionParams(rpcRequest, listener));
        subscriptionIds.put(rpcRequest.getId(), 0L);

        updateSubscriptions();
    }

    public void signatureSubscribe(String signature, NotificationEventListener listener) {
        List<Object> params = new ArrayList<Object>();
        params.add(signature);

        RpcRequest rpcRequest = new RpcRequest("signatureSubscribe", params);

        subscriptions.put(rpcRequest.getId(), new SubscriptionParams(rpcRequest, listener));
        subscriptionIds.put(rpcRequest.getId(), 0L);

        updateSubscriptions();
    }

    public void logsSubscribe(String mention, NotificationEventListener listener) {
        List<Object> params = new ArrayList<Object>();
        params.add(Map.of("mentions", List.of(mention)));
        params.add(Map.of("commitment", "finalized"));

        RpcRequest rpcRequest = new RpcRequest("logsSubscribe", params);

        subscriptions.put(rpcRequest.getId(), new SubscriptionParams(rpcRequest, listener));
        subscriptionIds.put(rpcRequest.getId(), 0L);

        updateSubscriptions();
    }

    public void logsSubscribe(List<String> mentions, NotificationEventListener listener) {
        List<Object> params = new ArrayList<Object>();
        params.add(Map.of("mentions", mentions));
        params.add(Map.of("commitment", "finalized"));

        RpcRequest rpcRequest = new RpcRequest("logsSubscribe", params);

        subscriptions.put(rpcRequest.getId(), new SubscriptionParams(rpcRequest, listener));
        subscriptionIds.put(rpcRequest.getId(), null);

        updateSubscriptions();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        LOGGER.info("Websocket connection opened");
        reconnectDelay = INITIAL_RECONNECT_DELAY;
        updateSubscriptions();
        startHeartbeat();
        connectLatch.countDown();
    }

    @SuppressWarnings({ "rawtypes" })
    @Override
    public void onMessage(String message) {
        JsonAdapter<RpcResponse<Long>> resultAdapter = new Moshi.Builder().build()
                .adapter(Types.newParameterizedType(RpcResponse.class, Long.class));

        try {
            RpcResponse<Long> rpcResult = resultAdapter.fromJson(message);
            String rpcResultId = rpcResult.getId();
            if (rpcResultId != null) {
                if (subscriptionIds.containsKey(rpcResultId)) {
                    try {
                        subscriptionIds.put(rpcResultId, rpcResult.getResult());
                        subscriptionListeners.put(rpcResult.getResult(), subscriptions.get(rpcResultId).listener);
                        subscriptions.remove(rpcResultId);
                    } catch (NullPointerException ignored) {

                    }
                }
            } else {
                JsonAdapter<RpcNotificationResult> notificationResultAdapter = new Moshi.Builder().build()
                        .adapter(RpcNotificationResult.class);
                RpcNotificationResult result = notificationResultAdapter.fromJson(message);
                NotificationEventListener listener = subscriptionListeners.get(result.getParams().getSubscription());

                Map value = (Map) result.getParams().getResult().getValue();

                switch (result.getMethod()) {
                    case "signatureNotification":
                        listener.onNotificationEvent(new SignatureNotification(value.get("err")));
                        break;
                    case "accountNotification":
                    case "logsNotification":
                        if (listener != null) {
                            listener.onNotificationEvent(value);
                        }
                        break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        LOGGER.info("Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: " + reason);
        stopHeartbeat();
        if (remote || code != CloseFrame.NORMAL) {
            scheduleReconnect();
        }
    }

    @Override
    public void onError(Exception ex) {
        LOGGER.severe("WebSocket error occurred: " + ex.getMessage());
        LOGGER.fine(() -> "Stack trace: " + java.util.Arrays.toString(ex.getStackTrace()));
    }

    public void reconnect() {
        LOGGER.info("Attempting to reconnect...");
        try {
            reconnectBlocking();
        } catch (InterruptedException e) {
            LOGGER.warning("Reconnection interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private void startHeartbeat() {
        executor.scheduleAtFixedRate(this::sendPing, 30, 30, TimeUnit.SECONDS);
    }

    private void stopHeartbeat() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    private void updateSubscriptions() {
        if (isOpen()) {
            JsonAdapter<RpcRequest> rpcRequestJsonAdapter = new Moshi.Builder().build().adapter(RpcRequest.class);
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

    private void scheduleReconnect() {
        executor.schedule(() -> {
            reconnect();
            reconnectDelay = Math.min(reconnectDelay * 2, MAX_RECONNECT_DELAY);
        }, reconnectDelay, TimeUnit.MILLISECONDS);
    }

    /**
     * Waits for the WebSocket connection to be established.
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return true if the connection was successfully established, false if the timeout was reached
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    public boolean waitForConnection(long timeout, TimeUnit unit) throws InterruptedException {
        return connectLatch.await(timeout, unit);
    }

}