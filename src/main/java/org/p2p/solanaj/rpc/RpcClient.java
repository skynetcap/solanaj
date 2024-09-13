package org.p2p.solanaj.rpc;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import org.p2p.solanaj.rpc.types.RpcRequest;
import org.p2p.solanaj.rpc.types.RpcResponse;
import org.p2p.solanaj.rpc.types.WeightedEndpoint;

import javax.net.ssl.*;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * RpcClient is responsible for making RPC calls to a Solana cluster.
 */
public class RpcClient {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private String endpoint;
    private OkHttpClient httpClient;
    private RpcApi rpcApi;
    private WeightedCluster cluster;
    private final Moshi moshi; // Reuse Moshi instance

    /**
     * Constructs an RpcClient with a specified weighted cluster.
     *
     * @param cluster the weighted cluster to use for RPC calls
     */
    public RpcClient(WeightedCluster cluster) {
        this.cluster = cluster;
        this.endpoint = cluster.getEndpoints().get(0).getUrl(); // Initialize endpoint from the cluster
        this.httpClient = new OkHttpClient.Builder().readTimeout(20, TimeUnit.SECONDS).build();
        this.rpcApi = new RpcApi(this);
        this.moshi = new Moshi.Builder().build(); // Initialize Moshi
    }

    /**
     * Constructs an RpcClient with a specified cluster.
     *
     * @param endpoint the cluster endpoint
     */
    public RpcClient(Cluster endpoint) {
        this(endpoint.getEndpoint());
    }

    /**
     * Constructs an RpcClient with a specified endpoint.
     *
     * @param endpoint the RPC endpoint
     */
    public RpcClient(String endpoint) {
        this(endpoint, new OkHttpClient.Builder().readTimeout(20, TimeUnit.SECONDS).build());
    }

    /**
     * Constructs an RpcClient with a specified endpoint and user agent.
     *
     * @param endpoint  the RPC endpoint
     * @param userAgent the user agent to set in the request header
     */
    public RpcClient(String endpoint, String userAgent) {
        this(endpoint, new OkHttpClient.Builder()
                .addNetworkInterceptor(chain -> chain.proceed(
                        chain.request().newBuilder().header("User-Agent", userAgent).build()))
                .readTimeout(20, TimeUnit.SECONDS)
                .build());
    }

    /**
     * Constructs an RpcClient with a specified endpoint and timeout.
     *
     * @param endpoint the RPC endpoint
     * @param timeout  the read timeout in seconds
     */
    public RpcClient(String endpoint, int timeout) {
        this(endpoint, new OkHttpClient.Builder().readTimeout(timeout, TimeUnit.SECONDS).build());
    }

    /**
     * Constructs an RpcClient with specified timeouts for read, connect, and write.
     *
     * @param endpoint        the RPC endpoint
     * @param readTimeoutMs   the read timeout in milliseconds
     * @param connectTimeoutMs the connect timeout in milliseconds
     * @param writeTimeoutMs  the write timeout in milliseconds
     */
    public RpcClient(String endpoint, int readTimeoutMs, int connectTimeoutMs, int writeTimeoutMs) {
        this.endpoint = endpoint;
        this.httpClient = new OkHttpClient.Builder()
                .readTimeout(readTimeoutMs, TimeUnit.MILLISECONDS)
                .connectTimeout(connectTimeoutMs, TimeUnit.MILLISECONDS)
                .writeTimeout(writeTimeoutMs, TimeUnit.MILLISECONDS)
                .build();
        this.rpcApi = new RpcApi(this);
        this.moshi = new Moshi.Builder().build(); // Initialize Moshi
    }

    /**
     * Constructs an RpcClient with a specified endpoint and OkHttpClient.
     *
     * @param endpoint   the RPC endpoint
     * @param httpClient the OkHttpClient to use for requests
     */
    public RpcClient(String endpoint, OkHttpClient httpClient) {
        this.endpoint = endpoint;
        this.httpClient = httpClient;
        this.rpcApi = new RpcApi(this);
        this.moshi = new Moshi.Builder().build(); // Initialize Moshi
    }

    /**
     * Constructs an RpcClient with a specified endpoint and SOCKS proxy.
     *
     * @param endpoint the RPC endpoint
     * @param proxyHost the SOCKS proxy host
     * @param proxyPort the SOCKS proxy port
     */
    public RpcClient(String endpoint, String proxyHost, int proxyPort) {
        this.endpoint = endpoint;
        this.httpClient = new OkHttpClient.Builder()
                .proxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyHost, proxyPort))) // Set SOCKS proxy
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
        this.rpcApi = new RpcApi(this);
        this.moshi = new Moshi.Builder().build(); // Initialize Moshi
    }

    /**
     * Calls the specified RPC method with the given parameters.
     *
     * @param method the RPC method to call
     * @param params the parameters for the RPC method
     * @param clazz  the class type of the expected result
     * @return the RpcResponse containing the result or error
     */
    public <T> RpcResponse<T> call(String method, List<Object> params, Class<T> clazz) {
        RpcRequest rpcRequest = new RpcRequest(method, params);

        JsonAdapter<RpcRequest> rpcRequestJsonAdapter = moshi.adapter(RpcRequest.class);
        JsonAdapter<RpcResponse<T>> resultAdapter = moshi.adapter(Types.newParameterizedType(RpcResponse.class, clazz));

        Request request = new Request.Builder().url(getEndpoint())
                .post(RequestBody.create(rpcRequestJsonAdapter.toJson(rpcRequest), JSON)).build();

        try {
            Response response = httpClient.newCall(request).execute();
            final String result = response.body().string();
            RpcResponse<T> rpcResult = resultAdapter.fromJson(result);

            if (rpcResult == null) {
                return new RpcResponse<T>(null, new RpcException("RPC response is null"));
            }

            return rpcResult;
        } catch (SSLHandshakeException e) {
            this.httpClient = new OkHttpClient.Builder().build();
            return new RpcResponse<T>(null, new RpcException("SSL Handshake failed: " + e.getMessage()));
        } catch (IOException e) {
            return new RpcResponse<T>(null, new RpcException("IO error during RPC call: " + e.getMessage()));
        }
    }

    /**
     * Returns the RpcApi instance associated with this client.
     *
     * @return the RpcApi instance
     */
    public RpcApi getApi() {
        return rpcApi;
    }

    /**
     * Returns the current RPC endpoint.
     *
     * @return the RPC endpoint
     */
    public String getEndpoint() {
        return (cluster != null) ? getWeightedEndpoint() : endpoint;
    }

    /**
     * Returns RPC Endpoint based on a list of weighted endpoints.
     * Weighted endpoints can be given an integer weight, with higher weights used more than lower weights.
     * Total weights across all endpoints do not need to sum up to any specific number.
     *
     * @return String RPCEndpoint
     */
    private String getWeightedEndpoint() {
        int totalWeight = cluster.endpoints.stream().mapToInt(WeightedEndpoint::getWeight).sum();
        double randomNumber = Math.random() * totalWeight;
        int currentWeight = 0;

        for (WeightedEndpoint endpoint : cluster.endpoints) {
            currentWeight += endpoint.getWeight();
            if (randomNumber < currentWeight) {
                return endpoint.getUrl();
            }
        }
        return ""; // Return empty string if no endpoint is found
    }
}
