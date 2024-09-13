package org.p2p.solanaj.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.p2p.solanaj.rpc.RpcApi;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.LatestBlockhash;
import org.p2p.solanaj.rpc.types.config.Commitment;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the getLatestBlockhash method in RpcApi using real Solana network data
 */
public class BlockhashTest {

    private RpcApi rpcApi;

    @BeforeEach
    public void setup() {
        // Use a public Solana testnet or devnet endpoint
        String endpoint = "https://api.devnet.solana.com";
        RpcClient rpcClient = new RpcClient(endpoint);
        rpcApi = new RpcApi(rpcClient);
    }

    /**
     * Test getLatestBlockhash without commitment
     */
    @Test
    public void testGetLatestBlockhashWithoutCommitment() throws RpcException {
        // Act
        LatestBlockhash latestBlockhash = rpcApi.getLatestBlockhash();

        // Assert
        assertNotNull(latestBlockhash);
        assertNotNull(latestBlockhash.getValue());
        assertNotNull(latestBlockhash.getValue().getBlockhash());
        assertEquals(44, latestBlockhash.getValue().getBlockhash().length()); // Solana blockhashes are 32 bytes, base58 encoded
        assertTrue(latestBlockhash.getValue().getBlockhash().matches("^[1-9A-HJ-NP-Za-km-z]{44}$")); // Base58 character set
        assertTrue(latestBlockhash.getValue().getLastValidBlockHeight() > 0);
    }

    /**
     * Test getLatestBlockhash with commitment
     */
    @Test
    public void testGetLatestBlockhashWithCommitment() throws RpcException {
        // Arrange
        Commitment commitment = Commitment.FINALIZED;

        // Act
        LatestBlockhash latestBlockhash = rpcApi.getLatestBlockhash(commitment);

        // Assert
        assertNotNull(latestBlockhash);
        assertNotNull(latestBlockhash.getValue());
        assertNotNull(latestBlockhash.getValue().getBlockhash());
        assertEquals(44, latestBlockhash.getValue().getBlockhash().length());
        assertTrue(latestBlockhash.getValue().getBlockhash().matches("^[1-9A-HJ-NP-Za-km-z]{44}$"));
        assertTrue(latestBlockhash.getValue().getLastValidBlockHeight() > 0);
    }

    /**
     * Test that getLatestBlockhash returns different values over time
     */
    @Test
    public void testGetLatestBlockhashChangesOverTime() throws RpcException, InterruptedException {
        // Act
        LatestBlockhash firstBlockhash = rpcApi.getLatestBlockhash();
        Thread.sleep(5000); // Wait for 5 seconds
        LatestBlockhash secondBlockhash = rpcApi.getLatestBlockhash();

        // Assert
        assertNotEquals(firstBlockhash.getValue().getBlockhash(), secondBlockhash.getValue().getBlockhash(), "Blockhashes should be different after waiting");
        assertTrue(secondBlockhash.getValue().getLastValidBlockHeight() >= firstBlockhash.getValue().getLastValidBlockHeight(), "Last valid block height should not decrease");
    }

    /**
     * Test getLatestBlockhash with different commitment levels
     */
    @Test
    public void testGetLatestBlockhashWithDifferentCommitments() throws RpcException {
        // Act
        LatestBlockhash processedBlockhash = rpcApi.getLatestBlockhash(Commitment.PROCESSED);
        LatestBlockhash confirmedBlockhash = rpcApi.getLatestBlockhash(Commitment.CONFIRMED);
        LatestBlockhash finalizedBlockhash = rpcApi.getLatestBlockhash(Commitment.FINALIZED);

        // Assert
        assertNotNull(processedBlockhash);
        assertNotNull(confirmedBlockhash);
        assertNotNull(finalizedBlockhash);

        // Note: These might be the same in some cases, but generally should be different
        System.out.println("Processed blockhash: " + processedBlockhash.getValue().getBlockhash() + ", Last valid block height: " + processedBlockhash.getValue().getLastValidBlockHeight());
        System.out.println("Confirmed blockhash: " + confirmedBlockhash.getValue().getBlockhash() + ", Last valid block height: " + confirmedBlockhash.getValue().getLastValidBlockHeight());
        System.out.println("Finalized blockhash: " + finalizedBlockhash.getValue().getBlockhash() + ", Last valid block height: " + finalizedBlockhash.getValue().getLastValidBlockHeight());

       assertTrue(confirmedBlockhash.getValue().getLastValidBlockHeight() >= finalizedBlockhash.getValue().getLastValidBlockHeight());
    }
}
