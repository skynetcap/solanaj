package org.p2p.solanaj.token;

import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.LegacyTransaction;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.programs.MemoProgram;
import org.p2p.solanaj.programs.TokenProgram;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;

/**
 * Manager class for calling {@link TokenProgram}-related APIs
 */
public class TokenManager {

    private final RpcClient client;

    public TokenManager(final RpcClient client) {
        this.client = client;
    }

    public String transfer(final Account owner, final PublicKey source, final PublicKey destination, final PublicKey tokenMint, long amount) {
        final LegacyTransaction legacyTransaction = new LegacyTransaction();

        // SPL token instruction
        legacyTransaction.addInstruction(
                TokenProgram.transfer(
                        source,
                        destination,
                        amount,
                        owner.getPublicKey()
                )
        );

        // Memo
        legacyTransaction.addInstruction(
                MemoProgram.writeUtf8(
                        owner.getPublicKey(),
                        "Hello from SolanaJ"
                )
        );

        // Call sendLegacyTransaction
        String result = null;
        try {
            result = client.getApi().sendLegacyTransaction(legacyTransaction, owner);
        } catch (RpcException e) {
            e.printStackTrace();
        }

        return result;
    }

    public String transferCheckedToSolAddress(final Account owner, final PublicKey source, final PublicKey destination, final PublicKey tokenMint, long amount, byte decimals) {
        // getTokenAccountsByOwner
        PublicKey tokenAccount = null;

        try {
            tokenAccount = client.getApi().getTokenAccountsByOwner(destination, tokenMint);
        } catch (RpcException e) {
            e.printStackTrace();
        }

        final LegacyTransaction legacyTransaction = new LegacyTransaction();
        // SPL token instruction
        legacyTransaction.addInstruction(
                TokenProgram.transferChecked(
                        source,
                        tokenAccount,
                        amount,
                        decimals,
                        owner.getPublicKey(),
                        tokenMint
                )
        );

        // Memo
        legacyTransaction.addInstruction(
                MemoProgram.writeUtf8(
                        owner.getPublicKey(),
                        "Hello from SolanaJ"
                )
        );

        // Call sendLegacyTransaction
        String result = null;
        try {
            result = client.getApi().sendLegacyTransaction(legacyTransaction, owner);
        } catch (RpcException e) {
            e.printStackTrace();
        }

        return result;
    }

    public String initializeAccount(Account newAccount, PublicKey usdcTokenMint, Account owner) {
        final LegacyTransaction legacyTransaction = new LegacyTransaction();

        // SPL token instruction
        legacyTransaction.addInstruction(
                TokenProgram.initializeAccount(
                        newAccount.getPublicKey(),
                        usdcTokenMint,
                        owner.getPublicKey()
                )
        );

        // Call sendLegacyTransaction
        String result = null;
        try {
            result = client.getApi().sendLegacyTransaction(legacyTransaction, owner);
        } catch (RpcException e) {
            e.printStackTrace();
        }

        return result;
    }
}
