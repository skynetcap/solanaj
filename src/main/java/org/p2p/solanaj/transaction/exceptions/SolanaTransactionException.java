package org.p2p.solanaj.transaction.exceptions;


import lombok.Getter;

import java.util.Optional;

public class SolanaTransactionException extends Exception {

    @Getter
    private Optional<String> txid = Optional.empty();

    public SolanaTransactionException(String message) {
        super(message);
    }

    public SolanaTransactionException(String message, String txid) {
        super(message);
        this.txid = Optional.ofNullable(txid);
    }

    public SolanaTransactionException(Throwable cause) {
        super(cause);
    }


}
