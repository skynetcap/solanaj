package org.p2p.solanaj.core;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
    private final List<String> signatures;
    private byte[] serializedMessage;
    private byte version = (byte) 0xFF; // Default to legacy version
    private List<AddressTableLookup> addressTableLookups = new ArrayList<>();

    /**
     * Constructs a new Transaction instance.
     */
    public Transaction() {
        this.message = new Message();
        this.signatures = new ArrayList<>();
    }

    /**
     * Adds an instruction to the transaction.
     *
     * @param instruction The instruction to add
     * @return This Transaction instance for method chaining
     * @throws NullPointerException if the instruction is null
     */
    public Transaction addInstruction(TransactionInstruction instruction) {
        Objects.requireNonNull(instruction, "Instruction cannot be null");
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
        Objects.requireNonNull(recentBlockhash, "Recent blockhash cannot be null");
        message.setRecentBlockHash(recentBlockhash);
    }

    /**
     * Signs the transaction with a single signer.
     *
     * @param signer The account to sign the transaction
     * @throws NullPointerException if the signer is null
     */
    public void sign(Account signer) {
        sign(Arrays.asList(Objects.requireNonNull(signer, "Signer cannot be null")));
    }

    /**
     * Signs the transaction with multiple signers.
     *
     * @param signers The list of accounts to sign the transaction
     * @throws IllegalArgumentException if no signers are provided
     */
    public void sign(List<Account> signers) {
        if (signers == null || signers.isEmpty()) {
            throw new IllegalArgumentException("No signers provided");
        }

        Account feePayer = signers.get(0);
        message.setFeePayer(feePayer);

        serializedMessage = message.serialize();

        for (Account signer : signers) {
            try {
                TweetNaclFast.Signature signatureProvider = new TweetNaclFast.Signature(new byte[0], signer.getSecretKey());
                byte[] signature = signatureProvider.detached(serializedMessage);
                signatures.add(Base58.encode(signature));
            } catch (Exception e) {
                throw new RuntimeException("Error signing transaction", e);
            }
        }
    }

    /**
     * Serializes the transaction into a byte array.
     *
     * @return The serialized transaction as a byte array
     */
    public byte[] serialize() {
        byte[] serializedMessage = message.serialize();
        int signaturesSize = signatures.size();
        byte[] signaturesLength = ShortvecEncoding.encodeLength(signaturesSize);

        int totalSize = signaturesLength.length + signaturesSize * SIGNATURE_LENGTH + serializedMessage.length;
        if (version != (byte) 0xFF) {
            totalSize += 1; // Add 1 byte for version
            if (version == 0) {
                totalSize += ShortvecEncoding.encodeLength(addressTableLookups.size()).length;
                for (AddressTableLookup lookup : addressTableLookups) {
                    totalSize += lookup.getSerializedSize();
                }
            }
        }

        ByteBuffer out = ByteBuffer.allocate(totalSize);

        if (version != (byte) 0xFF) {
            out.put(version);
        }
        out.put(signaturesLength);

        for (String signature : signatures) {
            byte[] rawSignature = Base58.decode(signature);
            out.put(rawSignature);
        }

        out.put(serializedMessage);

        if (version == 0) {
            byte[] addressTableLookupsLength = ShortvecEncoding.encodeLength(addressTableLookups.size());
            out.put(addressTableLookupsLength);
            for (AddressTableLookup lookup : addressTableLookups) {
                out.put(lookup.serialize());
            }
        }

        return out.array();
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public byte getVersion() {
        return version;
    }

    public void addAddressTableLookup(PublicKey tablePubkey, List<Byte> writableIndexes, List<Byte> readonlyIndexes) {
        addressTableLookups.add(new AddressTableLookup(tablePubkey, writableIndexes, readonlyIndexes));
    }
}