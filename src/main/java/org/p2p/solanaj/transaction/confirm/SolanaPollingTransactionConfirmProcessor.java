package org.p2p.solanaj.transaction.confirm;

import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.ConfirmedTransaction;
import org.p2p.solanaj.rpc.types.config.Commitment;
import org.p2p.solanaj.transaction.exceptions.SolanaTransactionException;


/**
 * 使用每个提供的事务id，轮询直到我们获到确认交易
 */
public class SolanaPollingTransactionConfirmProcessor extends SolanaTransactionConfirmProcessor {
    protected final long sleepDuration;
    protected final int attempts;

    public SolanaPollingTransactionConfirmProcessor(RpcClient rpcClient, Commitment commitment, long sleepDuration, int attempts) {
        super(rpcClient, commitment);
        this.sleepDuration = sleepDuration;
        this.attempts = attempts;
    }

    public SolanaPollingTransactionConfirmProcessor(RpcClient rpcClient, Commitment commitment) {
        this(rpcClient, commitment, 1000, 120);
    }

    public SolanaPollingTransactionConfirmProcessor(RpcClient rpcClient) {
        this(rpcClient, Commitment.FINALIZED);
    }

    public SolanaPollingTransactionConfirmProcessor() {
        this(new RpcClient(Cluster.MAINNET));
    }

    @Override
    public ConfirmedTransaction waitForTransactionConfirm(String txid) throws SolanaTransactionException, RpcException {
        return getTransactionReceipt(txid, sleepDuration, attempts);
    }

    private ConfirmedTransaction getTransactionReceipt(
            String txid, long sleepDuration, int attempts)
            throws SolanaTransactionException, RpcException {

        for (int i = 0; i < attempts; i++) {
            ConfirmedTransaction confirmedTransaction = sendConfirmTransactionRequest(txid);

            if (confirmedTransaction != null) {
                return confirmedTransaction;
            }

            // Sleep unless it is the last attempt.
            if (i < attempts - 1) {
                try {
                    Thread.sleep(sleepDuration);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        throw new SolanaTransactionException(
                "Transaction confirm was not generated after "
                        + ((sleepDuration * attempts) / 1000
                        + " seconds for txid: "
                        + txid),
                txid);
    }
}
