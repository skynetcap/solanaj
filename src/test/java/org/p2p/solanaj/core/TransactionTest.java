package org.p2p.solanaj.core;

import org.bitcoinj.core.Base58;
import org.junit.jupiter.api.Test;
import org.p2p.solanaj.programs.MemoProgram;
import org.p2p.solanaj.programs.SystemProgram;

import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

        assertEquals("nXkZvmiP3kzZbR7u95NSoK78Y3YqgSSthseuba99uBGsEBnR4RXugEhrAFmqhvWiN8k9aZNTZTE22NH6nBX3B7T", transaction.getTxHash());

        assertEquals(
                "ASdDdWBaKXVRA+6flVFiZokic9gK0+r1JWgwGg/GJAkLSreYrGF4rbTCXNJvyut6K6hupJtm72GztLbWNmRF1Q4BAAEDBhrZ0FOHFUhTft4+JhhJo9+3/QL6vHWyI8jkatuFPQzrerzQ2HXrwm2hsYGjM5s+8qMWlbt6vbxngnO8rc3lqgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAy+KIwZmU8DLmYglP3bPzrlpDaKkGu6VIJJwTOYQmRfUBAgIAAQwCAAAAuAsAAAAAAAA=",
                Base64.getEncoder().encodeToString(serializedTransaction));
    }


    @Test
    public void deserialize() {
        String serializedTxBase64 = "ASdDdWBaKXVRA+6flVFiZokic9gK0+r1JWgwGg/GJAkLSreYrGF4rbTCXNJvyut6K6hupJtm72GztLbWNmRF1Q4BAAEDBhrZ0FOHFUhTft4+JhhJo9+3/QL6vHWyI8jkatuFPQzrerzQ2HXrwm2hsYGjM5s+8qMWlbt6vbxngnO8rc3lqgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAy+KIwZmU8DLmYglP3bPzrlpDaKkGu6VIJJwTOYQmRfUBAgIAAQwCAAAAuAsAAAAAAAA=";
        byte[] serializedTransaction = Base64.getDecoder().decode(serializedTxBase64);
        Transaction transaction = Transaction.deserialize(serializedTransaction);

        assertEquals("nXkZvmiP3kzZbR7u95NSoK78Y3YqgSSthseuba99uBGsEBnR4RXugEhrAFmqhvWiN8k9aZNTZTE22NH6nBX3B7T", transaction.getTxHash());

        byte[] serializedNew = transaction.serialize();

        assertEquals(Base64.getEncoder().encodeToString(serializedNew), serializedTxBase64);
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

}
