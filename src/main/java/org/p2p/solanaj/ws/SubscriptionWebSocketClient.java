package org.p2p.solanaj.ws;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.p2p.solanaj.rpc.types.RpcNotificationResult;
import org.p2p.solanaj.rpc.types.RpcRequest;
import org.p2p.solanaj.rpc.types.RpcResponse;
import org.p2p.solanaj.rpc.types.config.Commitment;
import org.p2p.solanaj.ws.listeners.NotificationEventListener;

public class SubscriptionWebSocketClient extends WebSocketClient {

    private static class SubscriptionParams {
        RpcRequest request;
        NotificationEventListener listener;

        SubscriptionParams(RpcRequest request, NotificationEventListener listener) {
            this.request = request;
            this.listener = listener;
        }

        SubscriptionParams(RpcRequest request) {
            this.request = request;
        }
    }

    private final Map<String, SubscriptionParams> subscriptions = new ConcurrentHashMap<>();
    private final Map<String, Long> subscriptionIds = new ConcurrentHashMap<>();
    private final Map<Long, NotificationEventListener> subscriptionListeners = new ConcurrentHashMap<>();
    private static final Logger LOGGER = Logger.getLogger(SubscriptionWebSocketClient.class.getName());

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

        if (!instance.isOpen()) {
            instance.connect();
        }

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
        List<Object> params = new ArrayList<>();
        params.add(signature);

        RpcRequest rpcRequest = new RpcRequest("signatureSubscribe", params);

        subscriptions.put(rpcRequest.getId(), new SubscriptionParams(rpcRequest, listener));
        subscriptionIds.put(rpcRequest.getId(), 0L);

        updateSubscriptions();
    }

    public void transactionSubscribe(String key, NotificationEventListener listener) {
        List<Object> params = new ArrayList<>();
        params.add(Map.of("vote", false,
                "failed", false,
                "accountRequired", key));
        params.add(Map.of("encoding", "jsonParsed",
                "commitment", Commitment.PROCESSED.getValue(),
                "transaction_details", "full"));

        RpcRequest rpcRequest = new RpcRequest("transactionSubscribe", params);

        subscriptions.put(rpcRequest.getId(), new SubscriptionParams(rpcRequest, listener));
        subscriptionIds.put(rpcRequest.getId(), 0L);

        updateSubscriptions();
    }

    public void logsSubscribe(String mention, NotificationEventListener listener) {
        List<Object> params = new ArrayList<>();
        params.add(Map.of("mentions", List.of(mention)));
        params.add(Map.of("commitment", Commitment.PROCESSED.getValue()));

        RpcRequest rpcRequest = new RpcRequest("logsSubscribe", params);

        subscriptions.put(rpcRequest.getId(), new SubscriptionParams(rpcRequest, listener));
        subscriptionIds.put(rpcRequest.getId(), 0L);

        updateSubscriptions();
    }

    public String logsSubscribeWithId(String mention, NotificationEventListener listener) {
        List<Object> params = new ArrayList<>();
        params.add(Map.of("mentions", List.of(mention)));
        params.add(Map.of("commitment", Commitment.PROCESSED.getValue()));

        RpcRequest rpcRequest = new RpcRequest("logsSubscribe", params);

        subscriptions.put(rpcRequest.getId(), new SubscriptionParams(rpcRequest, listener));
        subscriptionIds.put(rpcRequest.getId(), 0L);

        updateSubscriptions();
        return rpcRequest.getId();
    }

    public void logsSubscribe(List<String> mentions, NotificationEventListener listener) {
        List<Object> params = new ArrayList<>();
        params.add(Map.of("mentions", mentions));
        params.add(Map.of("commitment", "finalized"));

        RpcRequest rpcRequest = new RpcRequest("logsSubscribe", params);

        subscriptions.put(rpcRequest.getId(), new SubscriptionParams(rpcRequest, listener));
        subscriptionIds.put(rpcRequest.getId(), null);

        updateSubscriptions();
    }

    public void logsUnSubscribe(String subscriptionIdKey) {
        List<Object> params = new ArrayList<>();

        RpcRequest rpcRequest = new RpcRequest("logsUnSubscribe", params);
        Long subscriptionId = subscriptionIds.get(subscriptionIdKey);
        params.add(subscriptionId);

        subscriptions.remove(subscriptionIdKey);
        subscriptionIds.remove(subscriptionIdKey);
        subscriptionListeners.remove(subscriptionId);

        subscriptions.put(rpcRequest.getId(), new SubscriptionParams(rpcRequest));
        subscriptionIds.put(rpcRequest.getId(), 0L);

        updateSubscriptions();

        subscriptions.remove(rpcRequest.getId());
        subscriptionIds.remove(rpcRequest.getId());
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        LOGGER.info("Websocket connection opened");
        updateSubscriptions();
        LOGGER.info("Size of subscriptions after onOpen: " + subscriptions.size());
    }

    @SuppressWarnings({ "rawtypes" })
    @Override
    public void onMessage(String message) {
        JsonAdapter<RpcResponse<Long>> resultAdapter = new Moshi.Builder().build()
                .adapter(Types.newParameterizedType(RpcResponse.class, Long.class));

        try {
            RpcResponse<Long> rpcResult = resultAdapter.fromJson(message);
            assert rpcResult != null;
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
                assert result != null;
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
        System.out.println(
                "Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: " + reason);
        LOGGER.info("Size of subscriptions after closing: " + subscriptions.size());
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

    private void updateSubscriptions() {
        if (isOpen() && !subscriptions.isEmpty()) {
            JsonAdapter<RpcRequest> rpcRequestJsonAdapter = new Moshi.Builder().build().adapter(RpcRequest.class);

            for (SubscriptionParams sub : subscriptions.values()) {
                send(rpcRequestJsonAdapter.toJson(sub.request));
            }
        }
    }

}
