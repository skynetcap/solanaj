package org.p2p.solanaj.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import static org.junit.jupiter.api.Assertions.*;

import org.p2p.solanaj.programs.MemoProgram;
import org.p2p.solanaj.programs.anchor.AnchorBasicTutorialProgram;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;

import java.util.List;

public class AnchorTest extends AccountBasedTest {

    private final RpcClient client = new RpcClient(Cluster.TESTNET);

    /**
     * Calls a testnet Anchor program: (tutorials/basic-0)'s 'initialize" call.
     * Also attaches a memo.
     */
    @Test
    @Disabled
    public void basicInitializeTest() {
        final Account feePayer = testAccount;

        final Transaction transaction = new Transaction();
        transaction.addInstruction(
                AnchorBasicTutorialProgram.initialize(feePayer)
        );

        transaction.addInstruction(
                MemoProgram.writeUtf8(feePayer.getPublicKey(), "I just called an Anchor program from SolanaJ.")
        );

        final List<Account> signers = List.of(feePayer);
        String result = null;
        try {
            result = client.getApi().sendTransaction(transaction, signers, null);
        } catch (RpcException e) {
            e.printStackTrace();
        }

        assertNotNull(result);
    }



}
