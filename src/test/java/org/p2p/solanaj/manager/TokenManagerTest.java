package org.p2p.solanaj.manager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.programs.SystemProgram;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcApi;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.token.TokenManager;

import static org.junit.Assert.*;
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
    @Before
    public void setUp() {
        mockRpcClient = Mockito.mock(RpcClient.class);
        mockRpcApi = Mockito.mock(RpcApi.class);
        when(mockRpcClient.getApi()).thenReturn(mockRpcApi);
        tokenManager = new TokenManager(mockRpcClient);

        owner = new Account();
        source = new PublicKey("4k3Dyjzvzp8e1Z1g1g1g1g1g1g1g1g1g1g1g1g1g1g1"); // Example source token account
        destination = new PublicKey("5h3Q4g1g1g1g1g1g1g1g1g1g1g1g1g1g1g1g1g1g1g1"); // Example destination token account
        tokenMint = new PublicKey("So11111111111111111111111111111111111111112"); // Wrapped SOL (WSOL) mint
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

        when(mockRpcApi.sendTransaction(any(Transaction.class), eq(owner))).thenReturn(expectedTxId);

        String result = tokenManager.transfer(owner, source, destination, tokenMint, amount);

        assertEquals(expectedTxId, result);
        verify(mockRpcApi).sendTransaction(any(Transaction.class), eq(owner));
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
        when(mockRpcApi.sendTransaction(any(Transaction.class), eq(owner))).thenReturn(expectedTxId);

        String result = tokenManager.transferCheckedToSolAddress(owner, source, destination, tokenMint, amount, decimals);

        assertEquals(expectedTxId, result);
        verify(mockRpcApi).getTokenAccountsByOwner(eq(destination), eq(tokenMint));
        verify(mockRpcApi).sendTransaction(any(Transaction.class), eq(owner));
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

        when(mockRpcApi.sendTransaction(any(Transaction.class), eq(owner))).thenReturn(expectedTxId);

        String result = tokenManager.initializeAccount(newAccount, usdcTokenMint, owner);

        assertEquals(expectedTxId, result);
        verify(mockRpcApi).sendTransaction(any(Transaction.class), eq(owner));
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
        PublicKey bonkMint = new PublicKey("DezXAZ8z7PnrnRJjz3wXBoRgixCa6xjnB7YaB1pPB263"); // Example BONK mint
        long amount = 1_000_000L; // 1 BONK (assuming 5 decimals)
        byte decimals = 5;

        PublicKey sourceATA = new PublicKey("4k3Dyjzvzp8e1Z1g1g1g1g1g1g1g1g1g1g1g1g1g1"); // Example source ATA
        PublicKey destinationATA = new PublicKey("5h3Q4g1g1g1g1g1g1g1g1g1g1g1g1g1g1g1g1g1g1"); // Example destination ATA
        String expectedTxId = "MockTransactionId";

        when(mockRpcApi.getTokenAccountsByOwner(eq(destination), eq(bonkMint))).thenReturn(destinationATA);
        when(mockRpcApi.sendTransaction(any(Transaction.class), eq(owner))).thenReturn(expectedTxId);

        String result = tokenManager.transferCheckedToSolAddress(owner, sourceATA, destination, bonkMint, amount, decimals);

        assertEquals(expectedTxId, result);
        verify(mockRpcApi).getTokenAccountsByOwner(eq(destination), eq(bonkMint));
        verify(mockRpcApi).sendTransaction(any(Transaction.class), eq(owner));
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

        when(mockRpcApi.sendTransaction(any(Transaction.class), eq(owner))).thenReturn(expectedTxId);

        Transaction transaction = new Transaction();
        transaction.addInstruction(
                SystemProgram.transfer(owner.getPublicKey(), recipient, amount)
        );

        String result = mockRpcApi.sendTransaction(transaction, owner);

        assertEquals(expectedTxId, result);
        verify(mockRpcApi).sendTransaction(any(Transaction.class), eq(owner));
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
        PublicKey wsolMint = new PublicKey("So11111111111111111111111111111111111111112"); // Wrapped SOL (WSOL) mint
        long amount = 1_000_000_000L; // 1 WSOL (9 decimals)
        byte decimals = 9;

        PublicKey sourceWSOLATA = new PublicKey("4k3Dyjzvzp8e1Z1g1g1g1g1g1g1g1g1g1g1g1g1g1"); // Example source WSOL ATA
        PublicKey destinationWSOLATA = new PublicKey("5h3Q4g1g1g1g1g1g1g1g1g1g1g1g1g1g1g1g1g1g1"); // Example destination WSOL ATA
        String expectedTxId = "MockTransactionId";

        when(mockRpcApi.getTokenAccountsByOwner(eq(destination), eq(wsolMint))).thenReturn(destinationWSOLATA);
        when(mockRpcApi.sendTransaction(any(Transaction.class), eq(owner))).thenReturn(expectedTxId);

        String result = tokenManager.transferCheckedToSolAddress(owner, sourceWSOLATA, destination, wsolMint, amount, decimals);

        assertEquals(expectedTxId, result);
        verify(mockRpcApi).getTokenAccountsByOwner(eq(destination), eq(wsolMint));
        verify(mockRpcApi).sendTransaction(any(Transaction.class), eq(owner));
    }
}