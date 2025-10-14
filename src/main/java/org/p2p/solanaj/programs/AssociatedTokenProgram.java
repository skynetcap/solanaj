package org.p2p.solanaj.programs;

import org.p2p.solanaj.core.AccountMeta;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class AssociatedTokenProgram extends Program {

    public static final PublicKey PROGRAM_ID = new PublicKey("ATokenGPvbdGVxr1b2hvZbsiqW5xWH25efTNsLJA8knL");

    private static final int CREATE_METHOD_ID = 0;
    private static final int CREATE_IDEMPOTENT_METHOD_ID = 1;
    private static final int RECOVER_NESTED_METHOD_ID = 2;

    /**
     * Creates an associated token account for the given wallet address and token mint.
     * Returns an error if the account already exists.
     *
     * @param fundingAccount The account funding the creation of the associated token account
     * @param walletAddress The wallet address for the new associated token account
     * @param mint The token mint for the new associated token account
     * @return TransactionInstruction for creating the associated token account
     */
    public static TransactionInstruction create(PublicKey fundingAccount,
                                                PublicKey walletAddress,
                                                PublicKey mint) {
        return createInstruction(CREATE_METHOD_ID, fundingAccount, walletAddress, mint);
    }

    /**
     * Creates an associated token account for the given wallet address and token mint,
     * if it doesn't already exist. Returns an error if the account exists but with a different owner.
     *
     * @param fundingAccount The account funding the creation of the associated token account
     * @param walletAddress The wallet address for the new associated token account
     * @param mint The token mint for the new associated token account
     * @return TransactionInstruction for creating the associated token account idempotently
     */
    public static TransactionInstruction createIdempotent(PublicKey fundingAccount,
                                                          PublicKey walletAddress,
                                                          PublicKey mint) {
        return createInstruction(CREATE_IDEMPOTENT_METHOD_ID, fundingAccount, walletAddress, mint);
    }

    /**
     * Transfers from and closes a nested associated token account: an associated token account
     * owned by an associated token account.
     *
     * @param nestedAccount The nested associated token account to be closed
     * @param nestedMint The token mint for the nested associated token account
     * @param destinationAccount The wallet's associated token account to receive the tokens
     * @param ownerAccount The owner associated token account address
     * @param ownerMint The token mint for the owner associated token account
     * @param wallet The wallet address for the owner associated token account
     * @return TransactionInstruction for recovering a nested associated token account
     */
    public static TransactionInstruction recoverNested(PublicKey nestedAccount,
                                                       PublicKey nestedMint,
                                                       PublicKey destinationAccount,
                                                       PublicKey ownerAccount,
                                                       PublicKey ownerMint,
                                                       PublicKey wallet) {
        final List<AccountMeta> keys = new ArrayList<>();

        keys.add(new AccountMeta(nestedAccount, false, true));
        keys.add(new AccountMeta(nestedMint, false, false));
        keys.add(new AccountMeta(destinationAccount, false, true));
        keys.add(new AccountMeta(ownerAccount, false, false));
        keys.add(new AccountMeta(ownerMint, false, false));
        keys.add(new AccountMeta(wallet, true, true));
        keys.add(new AccountMeta(TokenProgram.PROGRAM_ID, false, false));

        byte[] transactionData = encodeInstructionData(RECOVER_NESTED_METHOD_ID);

        return createTransactionInstruction(PROGRAM_ID, keys, transactionData);
    }

    private static TransactionInstruction createInstruction(int methodId,
                                                            PublicKey fundingAccount,
                                                            PublicKey walletAddress,
                                                            PublicKey mint) {
        final List<AccountMeta> keys = new ArrayList<>();

        PublicKey pda = findAssociatedTokenAddress(walletAddress, mint);

        keys.add(new AccountMeta(fundingAccount, true, true));
        keys.add(new AccountMeta(pda, false, true));
        keys.add(new AccountMeta(walletAddress, false, false));
        keys.add(new AccountMeta(mint, false, false));
        keys.add(new AccountMeta(SystemProgram.PROGRAM_ID, false, false));
        keys.add(new AccountMeta(TokenProgram.PROGRAM_ID, false, false));

        byte[] transactionData = encodeInstructionData(methodId);

        return createTransactionInstruction(PROGRAM_ID, keys, transactionData);
    }

    private static PublicKey findAssociatedTokenAddress(PublicKey walletAddress, PublicKey mint) {
        try {
            PublicKey pda = PublicKey.findProgramAddress(
                    List.of(
                            walletAddress.toByteArray(),
                            TokenProgram.PROGRAM_ID.toByteArray(),
                            mint.toByteArray()
                    ),
                    PROGRAM_ID
            ).getAddress();
            return pda;
        } catch (Exception e) {
            throw new RuntimeException("Failed to find associated token address", e);
        }
    }

    private static byte[] encodeInstructionData(int methodId) {
        ByteBuffer result = ByteBuffer.allocate(1);
        result.order(ByteOrder.LITTLE_ENDIAN);
        result.put((byte) methodId);
        return result.array();
    }
}
