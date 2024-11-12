package org.p2p.solanaj.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.Mockito;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.LegacyTransaction;
import org.p2p.solanaj.programs.SystemProgram;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcApi;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.token.TokenManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the TokenManager class, which handles token-related operations
 * in the Solana blockchain.
 */
public class TokenManagerTest {

    private RpcClient mockRpcClient;
    private RpcApi mockRpcApi;
    private TokenManager tokenManager;
    private Account owner;
    private PublicKey source;
    private PublicKey destination;
    private PublicKey tokenMint;

    /**
     * Sets up the test environment before each test case.
     * Initializes mock objects and the TokenManager instance.
     */
    @BeforeEach
    public void setUp() {
        mockRpcClient = Mockito.mock(RpcClient.class);
        mockRpcApi = Mockito.mock(RpcApi.class);
        when(mockRpcClient.getApi()).thenReturn(mockRpcApi);
        tokenManager = new TokenManager(mockRpcClient);

        owner = new Account();
        source = new PublicKey("DezXAZ8z7PnrnRJjz3wXBoRgixCa6xjnB7YaB1pPB263");
        destination = new PublicKey("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v");
        tokenMint = new PublicKey("So11111111111111111111111111111111111111112");
    }

    /**
     * Tests the transfer method of the TokenManager.
     * Verifies that the transaction ID returned matches the expected ID.
     *
     * @throws RpcException if there is an error during the RPC call.
     */
    @Test
    public void testTransfer() throws RpcException {
        long amount = 1000L;
        String expectedTxId = "MockTransactionId";

        when(mockRpcApi.sendLegacyTransaction(any(LegacyTransaction.class), eq(owner))).thenReturn(expectedTxId);

        String result = tokenManager.transfer(owner, source, destination, tokenMint, amount);

        assertEquals(expectedTxId, result);
        verify(mockRpcApi).sendLegacyTransaction(any(LegacyTransaction.class), eq(owner));
    }

    /**
     * Tests the transferCheckedToSolAddress method of the TokenManager.
     * Verifies that the transaction ID returned matches the expected ID.
     *
     * @throws RpcException if there is an error during the RPC call.
     */
    @Test
    public void testTransferCheckedToSolAddress() throws RpcException {
        long amount = 1000L;
        byte decimals = 9;
        PublicKey destinationATA = new PublicKey("5h3Q4g1g1g1g1g1g1g1g1g1g1g1g1g1g1g1g1g1g1"); // Example destination ATA
        String expectedTxId = "MockTransactionId";

        when(mockRpcApi.getTokenAccountsByOwner(eq(destination), eq(tokenMint))).thenReturn(destinationATA);
        when(mockRpcApi.sendLegacyTransaction(any(LegacyTransaction.class), eq(owner))).thenReturn(expectedTxId);

        String result = tokenManager.transferCheckedToSolAddress(owner, source, destination, tokenMint, amount, decimals);

        assertEquals(expectedTxId, result);
        verify(mockRpcApi).getTokenAccountsByOwner(eq(destination), eq(tokenMint));
        verify(mockRpcApi).sendLegacyTransaction(any(LegacyTransaction.class), eq(owner));
    }

    /**
     * Tests the initializeAccount method of the TokenManager.
     * Verifies that the transaction ID returned matches the expected ID.
     *
     * @throws RpcException if there is an error during the RPC call.
     */
    @Test
    public void testInitializeAccount() throws RpcException {
        Account newAccount = new Account();
        PublicKey usdcTokenMint = new PublicKey("A4k3Dyjzvzp8e1Z1g1g1g1g1g1g1g1g1g1g1g1g1g1"); // Example USDC mint
        String expectedTxId = "MockTransactionId";

        when(mockRpcApi.sendLegacyTransaction(any(LegacyTransaction.class), eq(owner))).thenReturn(expectedTxId);

        String result = tokenManager.initializeAccount(newAccount, usdcTokenMint, owner);

        assertEquals(expectedTxId, result);
        verify(mockRpcApi).sendLegacyTransaction(any(LegacyTransaction.class), eq(owner));
    }

    /**
     * Tests the transferCheckedToSolAddress method for transferring arbitrary tokens.
     * Verifies that the transaction ID returned matches the expected ID.
     *
     * @throws RpcException if there is an error during the RPC call.
     */
    @Test
    public void testTransferArbitraryToken() throws RpcException {
        // Example for transferring BONK tokens
        PublicKey bonkMint = new PublicKey("DezXAZ8z7PnrnRJjz3wXBoRgixCa6xjnB7YaB1pPB263");
        long amount = 1_000_000L; // 1 BONK (assuming 5 decimals)
        byte decimals = 5;

        PublicKey sourceATA = new PublicKey("J3dxNj7nDRRqRRXuEMynDG57DkZK4jYRuv3Garmb1i99");
        PublicKey destinationATA = new PublicKey("AyGCwnwxQMCqaU4ixReHt8h5W4dwmxU7eM3BEQBdWVca");
        String expectedTxId = "MockTransactionId";

        when(mockRpcApi.getTokenAccountsByOwner(eq(destination), eq(bonkMint))).thenReturn(destinationATA);
        when(mockRpcApi.sendLegacyTransaction(any(LegacyTransaction.class), eq(owner))).thenReturn(expectedTxId);

        String result = tokenManager.transferCheckedToSolAddress(owner, sourceATA, destination, bonkMint, amount, decimals);

        assertEquals(expectedTxId, result);
        verify(mockRpcApi).getTokenAccountsByOwner(eq(destination), eq(bonkMint));
        verify(mockRpcApi).sendLegacyTransaction(any(LegacyTransaction.class), eq(owner));
    }

    /**
     * Tests the transfer method for transferring native SOL.
     * Verifies that the transaction ID returned matches the expected ID.
     *
     * @throws RpcException if there is an error during the RPC call.
     */
    @Test
    public void testTransferSOL() throws RpcException {
        // For transferring native SOL, we use SystemProgram instead of TokenProgram
        PublicKey recipient = new PublicKey("3n1k1g1g1g1g1g1g1g1g1g1g1g1g1g1g1g1g1g1g1"); // Example recipient address
        long amount = 1_000_000_000L; // 1 SOL (lamports)
        String expectedTxId = "MockTransactionId";

        when(mockRpcApi.sendLegacyTransaction(any(LegacyTransaction.class), eq(owner))).thenReturn(expectedTxId);

        LegacyTransaction transaction = new LegacyTransaction();
        transaction.addInstruction(
                SystemProgram.transfer(owner.getPublicKey(), recipient, amount)
        );

        String result = mockRpcApi.sendLegacyTransaction(transaction, owner);

        assertEquals(expectedTxId, result);
        verify(mockRpcApi).sendLegacyTransaction(any(LegacyTransaction.class), eq(owner));
    }

    /**
     * Tests the transferCheckedToSolAddress method for transferring Wrapped SOL (WSOL).
     * Verifies that the transaction ID returned matches the expected ID.
     *
     * @throws RpcException if there is an error during the RPC call.
     */
    @Test
    public void testTransferWSOL() throws RpcException {
        // For transferring Wrapped SOL (WSOL)
        PublicKey wsolMint = new PublicKey("So11111111111111111111111111111111111111112");
        long amount = 1_000_000_000L; // 1 WSOL (9 decimals)
        byte decimals = 9;

        PublicKey sourceWSOLATA = new PublicKey("J3dxNj7nDRRqRRXuEMynDG57DkZK4jYRuv3Garmb1i99");
        PublicKey destinationWSOLATA = new PublicKey("AyGCwnwxQMCqaU4ixReHt8h5W4dwmxU7eM3BEQBdWVca");
        String expectedTxId = "MockTransactionId";

        when(mockRpcApi.getTokenAccountsByOwner(eq(destination), eq(wsolMint))).thenReturn(destinationWSOLATA);
        when(mockRpcApi.sendLegacyTransaction(any(LegacyTransaction.class), eq(owner))).thenReturn(expectedTxId);

        String result = tokenManager.transferCheckedToSolAddress(owner, sourceWSOLATA, destination, wsolMint, amount, decimals);

        assertEquals(expectedTxId, result);
        verify(mockRpcApi).getTokenAccountsByOwner(eq(destination), eq(wsolMint));
        verify(mockRpcApi).sendLegacyTransaction(any(LegacyTransaction.class), eq(owner));
    }
}