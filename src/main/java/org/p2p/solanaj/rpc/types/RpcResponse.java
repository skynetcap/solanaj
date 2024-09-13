package org.p2p.solanaj.rpc.types;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;
import org.p2p.solanaj.rpc.RpcException;

@Getter
@ToString
public class RpcResponse<T> {

    @Getter
    @ToString
    public static class Error {
        @Json(name = "code")
        private long code;

        @Json(name = "message")
        private String message;

        /**
         * Constructs an Error object with the specified code and message.
         *
         * @param code    the error code
         * @param message the error message
         */
        public Error(long code, String message) {
            this.code = code;
            this.message = message;
        }
    }

    @Json(name = "jsonrpc")
    private String jsonrpc;

    @Json(name = "result")
    private T result;

    @Json(name = "error")
    private Error error;

    @Json(name = "id")
    private String id;

    /**
     * Represents a response from an RPC call, including the result and potential error information.
     *
     * @param <T> the type of the result
     */
    public RpcResponse(T result, RpcException error) {
        this.result = result;
        this.error = error != null ? new Error(error.getErrorCode(), error.getMessage()) : null;
    }

    /**
     * Checks if the RPC response is successful (i.e., no error).
     *
     * @return true if the response is successful, false otherwise
     */
    public boolean isSuccessful() {
        return error == null;
    }
}
