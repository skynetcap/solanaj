package org.p2p.solanaj.rpc;

/**
 * Exception class representing an RPC error with a message and an error code.
 */
public class RpcException extends Exception {
    private static final long serialVersionUID = 8315999767009642193L;
    private final Long errorCode;

    /**
     * Constructs a new RpcException with the specified detail message and error code.
     *
     * @param message   the detail message
     * @param errorCode the error code associated with the exception
     */
    public RpcException(String message, Long errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Constructs a new RpcException with the specified detail message and a default error code of -1.
     *
     * @param message the detail message
     */
    public RpcException(String message) {
        super(message);
        this.errorCode = -1L; // Default error code
    }

    public Long getErrorCode() {
        return errorCode;
    }
}
