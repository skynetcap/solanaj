package org.p2p.solanaj.core;

import org.p2p.solanaj.programs.MemoProgram;
import org.p2p.solanaj.programs.SystemProgram;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcApi;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.ConfirmedTransaction;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.bitcoinj.core.Base58;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionTest {

    private static final Logger logger = LoggerFactory.getLogger(TransactionTest.class);

    private final static Account signer = new Account(Base58
            .decode("4Z7cXSyeFR8wNGMVXUE1TwtKn5D5Vu7FzEv69dokLv7KrQk7h6pu4LF8ZRR9yQBhc7uSM6RTTZtU1fmaxiNrxXrs"));

    @Test
    public void signAndSerialize() {
        PublicKey fromPublicKey = new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo");
        PublicKey toPublickKey = new PublicKey("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5");
        int lamports = 3000;

        Transaction transaction = new Transaction();
        transaction.addInstruction(SystemProgram.transfer(fromPublicKey, toPublickKey, lamports));
        transaction.setRecentBlockHash("Eit7RCyhUixAe2hGBS8oqnw59QK3kgMMjfLME5bm9wRn");
        transaction.sign(signer);
        byte[] serializedTransaction = transaction.serialize();

        assertEquals(
                "ASdDdWBaKXVRA+6flVFiZokic9gK0+r1JWgwGg/GJAkLSreYrGF4rbTCXNJvyut6K6hupJtm72GztLbWNmRF1Q4BAAEDBhrZ0FOHFUhTft4+JhhJo9+3/QL6vHWyI8jkatuFPQzrerzQ2HXrwm2hsYGjM5s+8qMWlbt6vbxngnO8rc3lqgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAy+KIwZmU8DLmYglP3bPzrlpDaKkGu6VIJJwTOYQmRfUBAgIAAQwCAAAAuAsAAAAAAAA=",
                Base64.getEncoder().encodeToString(serializedTransaction));
    }

    @Test
    public void transactionBuilderTest() {
        final String memo = "Test memo";
        final Transaction transaction = new TransactionBuilder()
                .addInstruction(
                        MemoProgram.writeUtf8(
                                signer.getPublicKey(),
                                memo
                        )
                )
                .setRecentBlockHash("Eit7RCyhUixAe2hGBS8oqnw59QK3kgMMjfLME5bm9wRn")
                .setSigners(List.of(signer))
                .build();

        assertEquals(
                "AV6w4Af9PSHhNsTSal4vlPF7Su9QXgCVyfDChHImJITLcS5BlNotKFeMoGw87VwjS3eNA2JCL+MEoReynCNbWAoBAAECBhrZ0FOHFUhTft4+JhhJo9+3/QL6vHWyI8jkatuFPQwFSlNQ+F3IgtYUpVZyeIopbd8eq6vQpgZ4iEky9O72oMviiMGZlPAy5mIJT92z865aQ2ipBrulSCScEzmEJkX1AQEBAAlUZXN0IG1lbW8=",
                Base64.getEncoder().encodeToString(transaction.serialize())
        );
    }

    @Test
    public void testV0TransactionWithAddressLookupTable() {
        Transaction transaction = new Transaction();
        transaction.setVersion((byte) 0);

        PublicKey fromPublicKey = new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo");
        PublicKey toPublicKey = new PublicKey("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5");
        int lamports = 3000;

        transaction.addInstruction(SystemProgram.transfer(fromPublicKey, toPublicKey, lamports));
        transaction.setRecentBlockHash("Eit7RCyhUixAe2hGBS8oqnw59QK3kgMMjfLME5bm9wRn");

        PublicKey lookupTableAddress = new PublicKey("BPFLoaderUpgradeab1e11111111111111111111111");
        List<Byte> writableIndexes = Arrays.asList((byte) 0, (byte) 1);
        List<Byte> readonlyIndexes = Arrays.asList((byte) 2);
        transaction.addAddressTableLookup(lookupTableAddress, writableIndexes, readonlyIndexes);

        transaction.sign(signer);
        byte[] serializedTransaction = transaction.serialize();

        // Assert that the serialized transaction starts with version 0
        assertEquals(0, serializedTransaction[0]);

        // Verify the presence of address table lookup data
        assertTrue(serializedTransaction.length > 200); // Approximate length check
    }

    @Test
    public void testV0TransactionBuilder() {
        PublicKey fromPublicKey = new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo");
        PublicKey toPublicKey = new PublicKey("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5");
        int lamports = 3000;

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

        // Log information about the transaction
        logger.info("Transaction version: {}", message.getVersion());
        logger.info("Number of account keys: {}", accountKeyStrings.size());
        logger.info("Number of address table lookups: {}", addressTableLookups.size());
        logger.info("First ALT pubkey: {}", firstLookup.getAccountKey());
        logger.info("First ALT writable indexes: {}", firstLookup.getWritableIndexes());
        logger.info("First ALT readonly indexes: {}", firstLookup.getReadonlyIndexes());
    }

}
