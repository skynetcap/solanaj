package org.p2p.solanaj.programs;

import org.p2p.solanaj.core.AccountMeta;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for creating Token Program {@link TransactionInstruction}s
 */
public class TokenProgram extends Program {

    public static final PublicKey PROGRAM_ID = new PublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA");

    /**
     * The public key of the Solana rent sysvar.
     * This sysvar provides information about the rent exempt minimum balance.
     */
    public static final PublicKey SYSVAR_RENT_PUBKEY = new PublicKey("SysvarRent111111111111111111111111111111111");

    private static final int INITIALIZE_MINT_METHOD_ID = 0;
    private static final int INITIALIZE_ACCOUNT_METHOD_ID = 1;
    private static final int INITIALIZE_MULTISIG_METHOD_ID = 2;
    private static final int TRANSFER_METHOD_ID = 3;
    private static final int APPROVE_METHOD_ID = 4;
    private static final int REVOKE_METHOD_ID = 5;
    private static final int SET_AUTHORITY_METHOD_ID = 6;
    private static final int MINT_TO_METHOD_ID = 7;
    private static final int BURN_METHOD_ID = 8;
    private static final int CLOSE_ACCOUNT_METHOD_ID = 9;
    private static final int FREEZE_ACCOUNT_METHOD_ID = 10;
    private static final int THAW_ACCOUNT_METHOD_ID = 11;
    private static final int TRANSFER_CHECKED_METHOD_ID = 12;

    /**
     * Transfers an SPL token from the owner's source account to destination account.
     * Destination pubkey must be the Token Account (created by token mint), and not the main SOL address.
     * @param source SPL token wallet funding this transaction
     * @param destination Destined SPL token wallet
     * @param amount 64 bit amount of tokens to send
     * @param owner account/private key signing this transaction
     * @return transaction id for explorer
     */
    public static TransactionInstruction transfer(PublicKey source, PublicKey destination, long amount, PublicKey owner) {
        final List<AccountMeta> keys = new ArrayList<>();

        keys.add(new AccountMeta(source,false, true));
        keys.add(new AccountMeta(destination,false, true));
        keys.add(new AccountMeta(owner,true, false));

        byte[] transactionData = encodeTransferTokenInstructionData(
                amount
        );

        return createTransactionInstruction(
                PROGRAM_ID,
                keys,
                transactionData
        );
    }

    /**
     * Creates a transaction instruction for a checked token transfer.
     *
     * @param source The public key of the source account
     * @param destination The public key of the destination account
     * @param amount The amount of tokens to transfer
     * @param decimals The number of decimals in the token
     * @param owner The public key of the source account owner
     * @param tokenMint The public key of the token's mint
     * @return A TransactionInstruction for the transfer
     */
    public static TransactionInstruction transferChecked(PublicKey source, PublicKey destination, long amount, byte decimals, PublicKey owner, PublicKey tokenMint) {
        final List<AccountMeta> keys = new ArrayList<>();

        keys.add(new AccountMeta(source,false, true));
        // index 1 = token mint (https://docs.rs/spl-token/3.1.0/spl_token/instruction/enum.TokenInstruction.html#variant.TransferChecked)
        keys.add(new AccountMeta(tokenMint, false, false));
        keys.add(new AccountMeta(destination,false, true));
        keys.add(new AccountMeta(owner,true, false));

        byte[] transactionData = encodeTransferCheckedTokenInstructionData(
                amount,
                decimals
        );

        return createTransactionInstruction(
                PROGRAM_ID,
                keys,
                transactionData
        );
    }

    public static TransactionInstruction initializeAccount(final PublicKey account, final PublicKey mint, final PublicKey owner) {
        final List<AccountMeta> keys = new ArrayList<>();

        keys.add(new AccountMeta(account,false, true));
        keys.add(new AccountMeta(mint, false, false));
        keys.add(new AccountMeta(owner,false, true));
        keys.add(new AccountMeta(SYSVAR_RENT_PUBKEY,false, false));

        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) INITIALIZE_ACCOUNT_METHOD_ID);

        return createTransactionInstruction(
                PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    public static TransactionInstruction closeAccount(final PublicKey accountPubkey, final PublicKey destinationPubkey, final PublicKey ownerPubkey) {
        final List<AccountMeta> keys = new ArrayList<>();

        keys.add(new AccountMeta(accountPubkey, false, true));
        keys.add(new AccountMeta(destinationPubkey, false, true));
        keys.add(new AccountMeta(ownerPubkey, true, false));

        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) CLOSE_ACCOUNT_METHOD_ID);

        return createTransactionInstruction(
                PROGRAM_ID,
                keys,
                buffer.array()
        );
    }

    /**
     * Encodes the transfer token instruction data.
     *
     * @param amount The amount of tokens to transfer
     * @return A byte array containing the encoded instruction data
     */
    private static byte[] encodeTransferTokenInstructionData(long amount) {
        ByteBuffer result = ByteBuffer.allocate(9);
        result.order(ByteOrder.LITTLE_ENDIAN);

        result.put((byte) TRANSFER_METHOD_ID);
        result.putLong(amount);

        return result.array();
    }

    /**
     * Encodes the transfer checked token instruction data.
     *
     * @param amount The amount of tokens to transfer
     * @param decimals The number of decimals in the token
     * @return A byte array containing the encoded instruction data
     */
    private static byte[] encodeTransferCheckedTokenInstructionData(long amount, byte decimals) {
        ByteBuffer result = ByteBuffer.allocate(10);
        result.order(ByteOrder.LITTLE_ENDIAN);

        result.put((byte) TRANSFER_CHECKED_METHOD_ID);
        result.putLong(amount);
        result.put(decimals);

        return result.array();
    }

    /**
     * Creates an instruction to initialize a new mint.
     *
     * @param mintPubkey The public key of the mint to initialize
     * @param decimals Number of base 10 digits to the right of the decimal place
     * @param mintAuthority The authority/multisignature to mint tokens
     * @param freezeAuthority The freeze authority/multisignature of the mint (optional)
     * @return TransactionInstruction to initialize the mint
     */
    public static TransactionInstruction initializeMint(
            PublicKey mintPubkey,
            int decimals,
            PublicKey mintAuthority,
            PublicKey freezeAuthority
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(mintPubkey, false, true));
        keys.add(new AccountMeta(SYSVAR_RENT_PUBKEY, false, false));

        ByteBuffer buffer = ByteBuffer.allocate(1 + 1 + 32 + 1 + (freezeAuthority != null ? 32 : 0));
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) INITIALIZE_MINT_METHOD_ID);
        buffer.put((byte) decimals);
        buffer.put(mintAuthority.toByteArray());
        buffer.put((byte) (freezeAuthority != null ? 1 : 0));
        if (freezeAuthority != null) {
            buffer.put(freezeAuthority.toByteArray());
        }

        return createTransactionInstruction(PROGRAM_ID, keys, buffer.array());
    }

    /**
     * Creates an instruction to initialize a multisig account.
     *
     * @param multisigPubkey The public key of the multisig account to initialize
     * @param signerPubkeys The public keys of the signers
     * @param m The number of required signatures
     * @return TransactionInstruction to initialize the multisig account
     */
    public static TransactionInstruction initializeMultisig(
            PublicKey multisigPubkey,
            List<PublicKey> signerPubkeys,
            int m
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(multisigPubkey, false, true));
        keys.add(new AccountMeta(SYSVAR_RENT_PUBKEY, false, false));
        for (PublicKey signerPubkey : signerPubkeys) {
            keys.add(new AccountMeta(signerPubkey, false, false));
        }

        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) INITIALIZE_MULTISIG_METHOD_ID);
        buffer.put((byte) m);

        return createTransactionInstruction(PROGRAM_ID, keys, buffer.array());
    }

    /**
     * Creates an instruction to approve a delegate.
     *
     * @param sourcePubkey The public key of the source account
     * @param delegatePubkey The public key of the delegate
     * @param ownerPubkey The public key of the source account owner
     * @param amount The amount of tokens to approve
     * @return TransactionInstruction to approve the delegate
     */
    public static TransactionInstruction approve(
            PublicKey sourcePubkey,
            PublicKey delegatePubkey,
            PublicKey ownerPubkey,
            long amount
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(sourcePubkey, false, true));
        keys.add(new AccountMeta(delegatePubkey, false, false));
        keys.add(new AccountMeta(ownerPubkey, true, false));

        ByteBuffer buffer = ByteBuffer.allocate(9);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) APPROVE_METHOD_ID);
        buffer.putLong(amount);

        return createTransactionInstruction(PROGRAM_ID, keys, buffer.array());
    }

    /**
     * Creates an instruction to revoke a delegate's authority.
     *
     * @param accountPubkey The public key of the token account
     * @param ownerPubkey The public key of the token account owner
     * @return TransactionInstruction to revoke the delegate's authority
     */
    public static TransactionInstruction revoke(
            PublicKey accountPubkey,
            PublicKey ownerPubkey
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(accountPubkey, false, true));
        keys.add(new AccountMeta(ownerPubkey, true, false));

        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put((byte) REVOKE_METHOD_ID);

        return createTransactionInstruction(PROGRAM_ID, keys, buffer.array());
    }

    /**
     * Creates an instruction to set a new authority of a mint or account.
     *
     * @param accountPubkey The public key of the mint or account
     * @param currentAuthorityPubkey The current authority of the mint or account
     * @param newAuthorityPubkey The new authority to set (optional)
     * @param authorityType The type of authority to set
     * @return TransactionInstruction to set the authority
     */
    public static TransactionInstruction setAuthority(
            PublicKey accountPubkey,
            PublicKey currentAuthorityPubkey,
            PublicKey newAuthorityPubkey,
            AuthorityType authorityType
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(accountPubkey, false, true));
        keys.add(new AccountMeta(currentAuthorityPubkey, true, false));

        ByteBuffer buffer = ByteBuffer.allocate(1 + 1 + 1 + (newAuthorityPubkey != null ? 32 : 0));
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) SET_AUTHORITY_METHOD_ID);
        buffer.put((byte) authorityType.getValue());
        buffer.put((byte) (newAuthorityPubkey != null ? 1 : 0));
        if (newAuthorityPubkey != null) {
            buffer.put(newAuthorityPubkey.toByteArray());
        }

        return createTransactionInstruction(PROGRAM_ID, keys, buffer.array());
    }

    /**
     * Creates an instruction to mint new tokens.
     *
     * @param mintPubkey The public key of the mint
     * @param destinationPubkey The public key of the account to mint tokens to
     * @param authorityPubkey The public key of the minting authority
     * @param amount The amount of new tokens to mint
     * @return TransactionInstruction to mint tokens
     */
    public static TransactionInstruction mintTo(
            PublicKey mintPubkey,
            PublicKey destinationPubkey,
            PublicKey authorityPubkey,
            long amount
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(mintPubkey, false, true));
        keys.add(new AccountMeta(destinationPubkey, false, true));
        keys.add(new AccountMeta(authorityPubkey, true, false));

        ByteBuffer buffer = ByteBuffer.allocate(9);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) MINT_TO_METHOD_ID);
        buffer.putLong(amount);

        return createTransactionInstruction(PROGRAM_ID, keys, buffer.array());
    }

    /**
     * Creates an instruction to burn tokens.
     *
     * @param accountPubkey The public key of the account to burn tokens from
     * @param mintPubkey The public key of the mint
     * @param ownerPubkey The public key of the token account owner
     * @param amount The amount of tokens to burn
     * @return TransactionInstruction to burn tokens
     */
    public static TransactionInstruction burn(
            PublicKey accountPubkey,
            PublicKey mintPubkey,
            PublicKey ownerPubkey,
            long amount
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(accountPubkey, false, true));
        keys.add(new AccountMeta(mintPubkey, false, true));
        keys.add(new AccountMeta(ownerPubkey, true, false));

        ByteBuffer buffer = ByteBuffer.allocate(9);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) BURN_METHOD_ID);
        buffer.putLong(amount);

        return createTransactionInstruction(PROGRAM_ID, keys, buffer.array());
    }

    /**
     * Creates an instruction to freeze an account.
     *
     * @param accountPubkey The public key of the account to freeze
     * @param mintPubkey The public key of the mint
     * @param authorityPubkey The public key of the freeze authority
     * @return TransactionInstruction to freeze the account
     */
    public static TransactionInstruction freezeAccount(
            PublicKey accountPubkey,
            PublicKey mintPubkey,
            PublicKey authorityPubkey
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(accountPubkey, false, true));
        keys.add(new AccountMeta(mintPubkey, false, false));
        keys.add(new AccountMeta(authorityPubkey, true, false));

        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put((byte) FREEZE_ACCOUNT_METHOD_ID);

        return createTransactionInstruction(PROGRAM_ID, keys, buffer.array());
    }

    /**
     * Creates an instruction to thaw a frozen account.
     *
     * @param accountPubkey The public key of the account to thaw
     * @param mintPubkey The public key of the mint
     * @param authorityPubkey The public key of the freeze authority
     * @return TransactionInstruction to thaw the account
     */
    public static TransactionInstruction thawAccount(
            PublicKey accountPubkey,
            PublicKey mintPubkey,
            PublicKey authorityPubkey
    ) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(accountPubkey, false, true));
        keys.add(new AccountMeta(mintPubkey, false, false));
        keys.add(new AccountMeta(authorityPubkey, true, false));

        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put((byte) THAW_ACCOUNT_METHOD_ID);

        return createTransactionInstruction(PROGRAM_ID, keys, buffer.array());
    }

    /**
     * Enum representing the types of authorities that can be set for a mint or account.
     */
    public enum AuthorityType {
        MINT_TOKENS(0),
        FREEZE_ACCOUNT(1),
        ACCOUNT_OWNER(2),
        CLOSE_ACCOUNT(3);

        private final int value;

        AuthorityType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
