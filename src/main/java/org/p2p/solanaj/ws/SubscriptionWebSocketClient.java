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

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
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
    private static final Logger log = Logger.getLogger(SubscriptionWebSocketClient.class.getName());

    @Getter
    private static URI serverURI;

    public static SubscriptionWebSocketClient getExactPathInstance(String endpoint) {
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
        log.info("Websocket connection opened");
        updateSubscriptions();
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
        log.warning(
                "Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: " + reason);

        reconnectAndSubscribe();
        log.info("Reconnected after onClose");
    }

    @Override
    public void onError(Exception ex) {
        log.warning("WSS onError: {}" + ex.getMessage());

        reconnectAndSubscribe();
        log.info("Reconnected after onError");
    }

    private void reconnectAndSubscribe() {
        // Reconnect
        SubscriptionWebSocketClient instance;
        instance = new SubscriptionWebSocketClient(serverURI);

        if (!instance.isOpen()) {
            instance.connect();
        }

        updateSubscriptions();
    }

    private void updateSubscriptions() {
        if (isOpen() && subscriptions.size() > 0) {
            JsonAdapter<RpcRequest> rpcRequestJsonAdapter = new Moshi.Builder().build().adapter(RpcRequest.class);

            for (SubscriptionParams sub : subscriptions.values()) {
                send(rpcRequestJsonAdapter.toJson(sub.request));
            }
        }
    }

}
