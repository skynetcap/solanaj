package org.p2p.solanaj.core;

import org.bitcoinj.core.Base58;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.p2p.solanaj.programs.SystemProgram;
import org.p2p.solanaj.programs.MemoProgram;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcApi;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.ConfirmedTransaction;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * Unit tests for TransactionBuilder.
 */
public class TransactionBuilderTest {

    private static final Account signer = new Account(Base58.decode("4Z7cXSyeFR8wNGMVXUE1TwtKn5D5Vu7FzEv69dokLv7KrQk7h6pu4LF8ZRR9yQBhc7uSM6RTTZtU1fmaxiNrxXrs"));

    /**
     * Tests building a legacy transaction using TransactionBuilder.
     */
    @Test
    public void buildLegacyTransaction() {
        PublicKey fromPublicKey = new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo");
        PublicKey toPublicKey = new PublicKey("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5");
        int lamports = 5000;

        Transaction transaction = new TransactionBuilder()
                .addInstruction(SystemProgram.transfer(fromPublicKey, toPublicKey, lamports))
                .setRecentBlockHash("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5")
                .setSigners(List.of(signer))
                .build();

        byte[] serialized = transaction.serialize();
        String serializedBase64 = Base64.getEncoder().encodeToString(serialized);

        assertNotNull(serializedBase64);
        assertFalse(serializedBase64.isEmpty());

        // Additional assertions can be added based on expected serialized output
    }

    /**
     * Tests adding an instruction to the TransactionBuilder.
     */
    @Test
    public void addInstructionTest() {
        PublicKey fromPublicKey = new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo");
        PublicKey toPublicKey = new PublicKey("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5");
        int lamports = 10000;

        TransactionInstruction transferInstruction = SystemProgram.transfer(fromPublicKey, toPublicKey, lamports);
        TransactionInstruction memoInstruction = MemoProgram.writeUtf8(signer.getPublicKey(), "Test Memo");

        Transaction transaction = new TransactionBuilder()
                .addInstruction(transferInstruction)
                .addInstruction(memoInstruction)
                .setRecentBlockHash("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo")
                .setSigners(List.of(signer))
                .build();

        byte[] serialized = transaction.serialize();
        String serializedBase64 = Base64.getEncoder().encodeToString(serialized);

        assertNotNull(serializedBase64);
        assertFalse(serializedBase64.isEmpty());

        // Example assertions to verify both instructions are included
        // These would need to match the expected serialized format
    }

    /**
     * Tests building a versioned transaction with Address Lookup Tables using TransactionBuilder.
     */
    @Test
    public void buildV0TransactionWithALT() {
        PublicKey fromPublicKey = new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo");
        PublicKey toPublicKey = new PublicKey("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5");
        int lamports = 7000;

        PublicKey lookupTableAddress = new PublicKey("BPFLoaderUpgradeab1e11111111111111111111111");
        List<Byte> writableIndexes = Arrays.asList((byte) 0, (byte) 1);
        List<Byte> readonlyIndexes = Arrays.asList((byte) 2);

        Transaction transaction = new TransactionBuilder()
                .addInstruction(SystemProgram.transfer(fromPublicKey, toPublicKey, lamports))
                .setRecentBlockHash("Eit7RCyhUixAe2hGBS8oqnw59QK3kgMMjfLME5bm9wRn")
                .setSigners(List.of(signer))
                .setVersion((byte) 0)
                .addAddressTableLookup(lookupTableAddress, writableIndexes, readonlyIndexes)
                .build();

        byte[] serializedTransaction = transaction.serialize();

        // Assert that the serialized transaction starts with version 0
        assertEquals(0, serializedTransaction[0]);

        // Verify the presence of address table lookup data
        assertTrue(serializedTransaction.length > 200); // Approximate length check
    }

    /**
     * Tests retrieving a version 0 transaction with an Address Lookup Table from the RPC API.
     *
     * @throws RpcException if there is an error during the RPC call.
     */
    @Test
    public void testRetrieveV0TransactionWithALT() throws RpcException {
        RpcClient rpcClient = new RpcClient(Cluster.MAINNET);
        RpcApi api = rpcClient.getApi();

        // This is a known v0 transaction with ALT on mainnet
        String signature = "3t4B38bCZWRxYktRjMEmzE6YdyaZaq2rX74QUHGU5sSxQmxsTL2guuQ6Nf9cfsQFavhpJNJDeDK6D9MKx3ojTw16";

        ConfirmedTransaction confirmedTx = api.getTransaction(signature);
        assertNotNull(confirmedTx);

        ConfirmedTransaction.Message message = confirmedTx.getTransaction().getMessage();
        assertNotNull(message);

        // Verify that this is a v0 transaction
        assertEquals(0, message.getVersion());

        // Verify the presence of account keys
        List<String> accountKeyStrings = message.getAccountKeyStrings();
        assertNotNull(accountKeyStrings);
        assertFalse(accountKeyStrings.isEmpty());

        // Verify the presence of address table lookups
        List<AddressTableLookup> addressTableLookups = message.getAddressTableLookups();
        assertNotNull(addressTableLookups);
        assertFalse(addressTableLookups.isEmpty());

        // Verify the first address table lookup
        AddressTableLookup firstLookup = addressTableLookups.get(0);
        assertNotNull(firstLookup);
        assertNotNull(firstLookup.getAccountKey());
        assertFalse(firstLookup.getWritableIndexes().isEmpty());
        assertFalse(firstLookup.getReadonlyIndexes().isEmpty());

        // Additional validations or logs can be added here
    }
}