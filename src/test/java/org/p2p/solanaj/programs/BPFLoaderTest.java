package org.p2p.solanaj.programs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import static org.junit.jupiter.api.Assertions.*;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;

import java.util.List;

/**
 * Test class for BPFLoader program instructions.
 */
public class BPFLoaderTest {

    private RpcClient client;
    private Account payer;
    private Account bufferAccount;
    private Account programAccount;
    private Account programDataAccount;

    @BeforeEach
    public void setUp() {
        client = new RpcClient(Cluster.DEVNET);
        payer = new Account();
        bufferAccount = new Account();
        programAccount = new Account();
        programDataAccount = new Account();
    }

    @Test
    public void testInitializeBuffer() {
        TransactionInstruction instruction = BPFLoader.initializeBuffer(
                bufferAccount.getPublicKey(),
                payer.getPublicKey()
        );

        assertEquals(BPFLoader.PROGRAM_ID, instruction.getProgramId());
        assertEquals(2, instruction.getKeys().size());
        assertEquals(1, instruction.getData().length);
        assertEquals(0, instruction.getData()[0]);
    }

    @Test
    public void testWrite() {
        byte[] data = new byte[]{1, 2, 3, 4, 5};
        TransactionInstruction instruction = BPFLoader.write(
                bufferAccount.getPublicKey(),
                payer.getPublicKey(),
                10,
                data
        );

        assertEquals(BPFLoader.PROGRAM_ID, instruction.getProgramId());
        assertEquals(2, instruction.getKeys().size());
        assertEquals(10, instruction.getData().length);
        assertEquals(1, instruction.getData()[0]);
    }

    @Test
    public void testDeployWithMaxDataLen() {
        TransactionInstruction instruction = BPFLoader.deployWithMaxDataLen(
                payer.getPublicKey(),
                programDataAccount.getPublicKey(),
                programAccount.getPublicKey(),
                bufferAccount.getPublicKey(),
                payer.getPublicKey(),
                1000
        );

        assertEquals(BPFLoader.PROGRAM_ID, instruction.getProgramId());
        assertEquals(8, instruction.getKeys().size());
        assertEquals(9, instruction.getData().length);
        assertEquals(2, instruction.getData()[0]);
    }

    @Test
    public void testUpgrade() {
        TransactionInstruction instruction = BPFLoader.upgrade(
                programDataAccount.getPublicKey(),
                programAccount.getPublicKey(),
                bufferAccount.getPublicKey(),
                payer.getPublicKey(),
                payer.getPublicKey()
        );

        assertEquals(BPFLoader.PROGRAM_ID, instruction.getProgramId());
        assertEquals(7, instruction.getKeys().size());
        assertEquals(1, instruction.getData().length);
        assertEquals(3, instruction.getData()[0]);
    }

    @Test
    public void testSetAuthority() {
        TransactionInstruction instruction = BPFLoader.setAuthority(
                programDataAccount.getPublicKey(),
                payer.getPublicKey(),
                new Account().getPublicKey()
        );

        assertEquals(BPFLoader.PROGRAM_ID, instruction.getProgramId());
        assertEquals(3, instruction.getKeys().size());
        assertEquals(1, instruction.getData().length);
        assertEquals(4, instruction.getData()[0]);
    }

    // ... existing code ...

    /**
     * Integration test for initializing a buffer.
     * Note: This test is ignored by default as it requires a connection to the Solana network.
     */
    @Test
    @Disabled
    public void initializeBufferIntegrationTest() throws RpcException {
        Account account = new Account(); // Replace with your actual account setup
        Transaction transaction = new Transaction();

        // Initialize buffer
        transaction.addInstruction(
                SystemProgram.createAccount(
                        account.getPublicKey(),
                        bufferAccount.getPublicKey(),
                        3290880,
                        165L,
                        BPFLoader.PROGRAM_ID
                )
        );

        transaction.addInstruction(
                BPFLoader.initializeBuffer(
                        bufferAccount.getPublicKey(),
                        account.getPublicKey()
                )
        );

        String hash = client.getApi().getLatestBlockhash().getValue().getBlockhash();
        transaction.setRecentBlockHash(hash);

        String txId = client.getApi().sendTransaction(transaction, List.of(account, bufferAccount), hash);
        assertNotNull(txId);
        System.out.println("Transaction ID: " + txId);
    }
}
