package org.p2p.solanaj.core;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.bitcoinj.core.Base58;
import org.p2p.solanaj.utils.ShortvecEncoding;
import org.p2p.solanaj.utils.TweetNaclFast;

public class LegacyTransaction {

    public static final int SIGNATURE_LENGTH = 64;

    private final LegacyMessage legacyMessage;
    private final List<String> signatures;
    private byte[] serializedLegacyMessage;

    /**
     * Constructs a new Legacy Transaction instance.
     */
    public LegacyTransaction() {
        this.legacyMessage = new LegacyMessage();
        this.signatures = new ArrayList<>(); // Use diamond operator
    }

    /**
     * Adds an instruction to the legacy transaction.
     *
     * @param instruction The instruction to add
     * @return This Transaction instance for method chaining
     * @throws NullPointerException if the instruction is null
     */
    public LegacyTransaction addInstruction(TransactionInstruction instruction) {
        Objects.requireNonNull(instruction, "Instruction cannot be null"); // Add input validation
        legacyMessage.addInstruction(instruction);
        return this;
    }

    /**
     * Sets the recent blockhash for the legacy transaction.
     *
     * @param recentBlockhash The recent blockhash to set
     * @throws NullPointerException if the recentBlockhash is null
     */
    public void setRecentBlockHash(String recentBlockhash) {
        Objects.requireNonNull(recentBlockhash, "Recent blockhash cannot be null"); // Add input validation
        legacyMessage.setRecentBlockHash(recentBlockhash);
    }

    /**
     * Signs the legacy transaction with a single signer.
     *
     * @param signer The account to sign the transaction
     * @throws NullPointerException if the signer is null
     */
    public void sign(Account signer) {
        sign(List.of(Objects.requireNonNull(signer, "Signer cannot be null"))); // Add input validation
    }

    /**
     * Signs the legacy transaction with multiple signers.
     *
     * @param signers The list of accounts to sign the transaction
     * @throws IllegalArgumentException if no signers are provided
     */
    public void sign(List<Account> signers) {
        if (signers == null || signers.isEmpty()) {
            throw new IllegalArgumentException("No signers provided");
        }

        Account feePayer = signers.get(0);
        legacyMessage.setFeePayer(feePayer);

        serializedLegacyMessage = legacyMessage.serialize();

        for (Account signer : signers) {
            try {
                TweetNaclFast.Signature signatureProvider = new TweetNaclFast.Signature(new byte[0], signer.getSecretKey());
                byte[] signature = signatureProvider.detached(serializedLegacyMessage);
                signatures.add(Base58.encode(signature));
            } catch (Exception e) {
                throw new RuntimeException("Error signing transaction", e); // Improve exception handling
            }
        }
    }

    /**
     * Serializes the legacy transaction into a byte array.
     *
     * @return The serialized transaction as a byte array
     */
    public byte[] serialize() {
        int signaturesSize = signatures.size();
        byte[] signaturesLength = ShortvecEncoding.encodeLength(signaturesSize);

        // Calculate total size before allocating ByteBuffer
        int totalSize = signaturesLength.length + signaturesSize * SIGNATURE_LENGTH + serializedLegacyMessage.length;
        ByteBuffer out = ByteBuffer.allocate(totalSize);

        out.put(signaturesLength);

        for (String signature : signatures) {
            byte[] rawSignature = Base58.decode(signature);
            out.put(rawSignature);
        }

        out.put(serializedLegacyMessage);

        return out.array();
    }
}
