package org.p2p.solanaj.rpc;

import java.io.Serial;

public class RpcException extends Exception {
    @Serial
    private final static long serialVersionUID = 8315999767009642193L;

    public RpcException(String message) {
        super(message);
    }
}
