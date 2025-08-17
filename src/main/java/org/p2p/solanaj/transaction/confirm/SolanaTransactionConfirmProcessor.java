package org.p2p.solanaj.transaction.confirm;

import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.ConfirmedTransaction;
import org.p2p.solanaj.rpc.types.config.Commitment;
import org.p2p.solanaj.transaction.exceptions.SolanaTransactionException;


/**
 * solana交易状态确认检查处理
 */
public abstract class SolanaTransactionConfirmProcessor {
    private final RpcClient rpcClient;
    private final Commitment commitment;

    public SolanaTransactionConfirmProcessor(RpcClient rpcClient, Commitment commitment) {
        this.rpcClient = rpcClient;
        this.commitment = commitment;
    }

    public abstract ConfirmedTransaction waitForTransactionConfirm(String txid)
            throws SolanaTransactionException, RpcException;

    ConfirmedTransaction sendConfirmTransactionRequest(String txid)
            throws RpcException, SolanaTransactionException {
        ConfirmedTransaction confirmedTransaction = rpcClient.getApi().getTransaction(txid, commitment);

        if (confirmedTransaction == null) {
            return null;
        }
        if (confirmedTransaction.getMeta().getErr() != null) {
            throw new SolanaTransactionException(
                    "Error processing request: " + confirmedTransaction.getMeta().getErr());
        }

        return confirmedTransaction;
    }
}
