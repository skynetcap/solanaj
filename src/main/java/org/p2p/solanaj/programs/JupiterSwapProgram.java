package org.p2p.solanaj.programs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.net.URIBuilder;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;

public final class JupiterSwapProgram {

    public static final String SOL_QUOTE_TOKEN = "So11111111111111111111111111111111111111112";

    private static final CloseableHttpClient httpClient = HttpClients.createDefault();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final HttpClientResponseHandler<JsonNode> handler = response -> {
        int status = response.getCode();
        if (status >= 200 && status < 300) {
            HttpEntity entity = response.getEntity();
            return entity != null ? objectMapper.readTree(EntityUtils.toString(entity)) : null;
        } else {
            throw new IOException("Unexpected response status: " + status);
        }
    };

    public static URI createQuoteUri(String inputToken, String outputToken, String amount, String slippage) {
        try {
            return new URIBuilder("https://quote-api.jup.ag/v6/quote")
                    .addParameter("inputMint", inputToken)
                    .addParameter("outputMint", outputToken)
                    .addParameter("amount", amount)
                    .addParameter("slippageBps", slippage)
                    .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to create quote URI: ", e);
        }
    }

    public static JsonNode getJupiterQuote(URI quoteUri) {
        HttpGet quoteRequest = new HttpGet(quoteUri);

        JsonNode quote;
        try {
            quote = httpClient.execute(quoteRequest, handler);
        } catch (IOException e) {
            throw new RuntimeException("Failed to retrieve quote from jupiter for " + quoteUri, e);
        }
        return quote;
    }

    public static String swapToken(RpcClient rpcClient, Account account, JsonNode quote) {
        SwapRequest swapRequestBody = new SwapRequest(quote, account.getPublicKey().toString());
        String jsonSwapRequestBody;
        try {
            jsonSwapRequestBody = objectMapper.writeValueAsString(swapRequestBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        HttpPost swapRequest = new HttpPost("https://quote-api.jup.ag/v6/swap");
        swapRequest.setHeader("Content-Type", "application/json");
        swapRequest.setEntity(new StringEntity(jsonSwapRequestBody));

        JsonNode swapRes;
        try {
            swapRes = httpClient.execute(swapRequest, handler);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String swapTransaction = swapRes.get("swapTransaction").asText();

        byte[] base64Decoded = Base64.getDecoder().decode(swapTransaction);
        Transaction transaction = Transaction.deserialize(base64Decoded);

        try {
            return rpcClient.getApi().sendTransaction(transaction, account);
        } catch (RpcException e) {
            throw new RuntimeException("Failed to send swap transaction", e);
        }
    }

    public static String swapToken(RpcClient rpcClient, Account account, String inputToken, String outputToken, String amount, String slippage) {
        URI quoteUri = createQuoteUri(inputToken, outputToken, amount, slippage);
        return swapToken(rpcClient, account, quoteUri);
    }

    public static String swapToken(RpcClient rpcClient, Account account, URI quoteUri) {
        JsonNode quote = getJupiterQuote(quoteUri);
        return swapToken(rpcClient, account, quote);
    }

    public record SwapRequest(Object quoteResponse, String userPublicKey) {}
}
