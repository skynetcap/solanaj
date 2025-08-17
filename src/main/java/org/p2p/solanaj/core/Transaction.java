package org.p2p.solanaj.core;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Stream;

import org.bitcoinj.core.Base58;
import org.p2p.solanaj.utils.ShortvecEncoding;
import org.p2p.solanaj.utils.TweetNaclFast;

/**
 * Represents a Solana transaction.
 * This class allows for building, signing, and serializing transactions.
 */
public class Transaction {

    public static final int SIGNATURE_LENGTH = 64;

    private final Message message;
    private final List<Account> signers; // TODO: more like Map[PublicKey,Account]

    /**
     * Constructs a new Transaction instance.
     */
    public Transaction() {
        this.message = new Message();
        this.signers = new ArrayList<>();
    }

    /**
     * Adds an instruction to the transaction.
     *
     * @param instruction The instruction to add
     * @return This Transaction instance for method chaining
     * @throws NullPointerException if the instruction is null
     */
    public Transaction addInstruction(TransactionInstruction instruction) {
        Objects.requireNonNull(instruction, "Instruction cannot be null"); // Add input validation
        message.addInstruction(instruction);
        return this;
    }

    /**
     * Sets the recent blockhash for the transaction.
     *
     * @param recentBlockhash The recent blockhash to set
     * @throws NullPointerException if the recentBlockhash is null
     */
    public void setRecentBlockHash(String recentBlockhash) {
        Objects.requireNonNull(recentBlockhash, "Recent blockhash cannot be null"); // Add input validation
        message.setRecentBlockHash(recentBlockhash);
    }

    /** Use specific account as transaction fee payer (first signer) */
    public void setFeePayer(PublicKey feePayer) {
        Objects.requireNonNull(feePayer, "FeePayer cannot be null");
        message.setFeePayer(feePayer);
    }

    /**
     * Signs the transaction with a specific signer.
     *
     * Note - this method does not affect signatures order. If specific account is to be used
     * for first signature, `setFeePayer` method should be used additionally.
     *
     * @param signer The account to sign the transaction
     * @throws NullPointerException if the signer is null
     */
    public void sign(Account signer) {
        Objects.requireNonNull(signer, "Signer cannot be null");
        this.signers.add(signer);
    }

    /**
     * Signs the transaction with multiple signers.
     *
     * @param signers The list of accounts to sign the transaction
     * @throws IllegalArgumentException if no signers are provided
     */
    public void sign(List<Account> signers) {
        Objects.requireNonNull(signers, "Signer cannot be null");
        this.signers.addAll(signers);
    }

    /**
     * Signs and serializes the transaction into a byte array.
     *
     * @return The serialized transaction as a byte array
     */
    public byte[] serialize() {

        byte[] serializedMessage = message.serialize();

        // TODO: use signers lookup, fail on excessive signers
        Stream<Account> requiredSigners = message.getAccountKeys()
                .stream()
                .filter(AccountMeta::isSigner)
                .map(AccountMeta::getPublicKey)
                .map(publicKey ->
                        signers.stream()
                                .filter(account -> account.getPublicKey().equals(publicKey))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("Missing signer for account "+publicKey)));

        // TODO: validate signature length
        List<byte[]> signatures = requiredSigners.map(signer -> {
                    try {
                        TweetNaclFast.Signature signatureProvider = new TweetNaclFast.Signature(new byte[0], signer.getSecretKey());
                        return signatureProvider.detached(serializedMessage);
                    } catch (Exception e) {
                        throw new RuntimeException("Error signing transaction", e);
                    }}).toList();


        int signaturesSize = signatures.size();
        byte[] signaturesLength = ShortvecEncoding.encodeLength(signaturesSize);

        // Calculate total size before allocating ByteBuffer
        int totalSize = signaturesLength.length + signaturesSize * SIGNATURE_LENGTH + serializedMessage.length;
        ByteBuffer out = ByteBuffer.allocate(totalSize);

        out.put(signaturesLength);
        signatures.forEach(out::put);

        out.put(serializedMessage);

        return out.array();
    }
}
