package org.p2p.solanaj.core;

import org.p2p.solanaj.programs.MemoProgram;
import org.p2p.solanaj.programs.SystemProgram;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import org.bitcoinj.core.Base58;

public class TransactionTest {

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

}
