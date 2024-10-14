package org.p2p.solanaj.core;

import org.bitcoinj.core.Base58;
import org.p2p.solanaj.utils.ShortvecEncoding;
import org.p2p.solanaj.utils.TweetNaclFast;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a versioned Solana transaction, supporting version 0 transactions and Address Lookup Tables (ALTs).
 * This class allows for building, signing, and serializing versioned transactions.
 */
public class VersionedTransaction {

    private final VersionedMessage message;
    private final List<String> signatures;

    /**
     * Constructs a new VersionedTransaction with the given message.
     *
     * @param message The versioned message to include in this transaction
     */
    public VersionedTransaction(VersionedMessage message) {
        this.message = Objects.requireNonNull(message, "Message cannot be null");
        this.signatures = new ArrayList<>();
    }

    /**
     * Adds a signature to the transaction.
     *
     * @param signature The signature as a Base58-encoded string
     */
    public void addSignature(String signature) {
        signatures.add(signature);
    }

    /**
     * Signs the transaction using the provided Account(s).
     *
     * @param signers The list of Accounts used to sign the transaction
     */
    public void sign(List<Account> signers) {
        Objects.requireNonNull(signers, "Signers cannot be null");
        message.getHeader().setNumRequiredSignatures((byte) signers.size());

        signatures.clear(); // Clear existing signatures
        byte[] serializedMessage = message.serialize();
        for (Account signer : signers) {
            TweetNaclFast.Signature signatureProvider = new TweetNaclFast.Signature(new byte[0], signer.getSecretKey());
            byte[] signature = signatureProvider.detached(serializedMessage);
            String signatureBase58 = Base58.encode(signature);
            addSignature(signatureBase58);
        }

        // Update header with the number of required signatures
    }

    /**
     * Adds an Address Lookup Table to the transaction.
     *
     * @param lookupTable The AddressTableLookup to add
     */
    public void addAddressTableLookup(AddressTableLookup lookupTable) {
        Objects.requireNonNull(lookupTable, "LookupTable cannot be null");
        message.addAddressTableLookup(lookupTable);
    }

    /**
     * Serializes the entire transaction into a byte array, including signatures and message.
     *
     * @return The serialized transaction as a byte array
     */
    public byte[] serialize() {
        byte[] messageBytes = message.serializeV0Message();
        int totalLength = 1 + signatures.size() * 64 + messageBytes.length;
        
        ByteBuffer buffer = ByteBuffer.allocate(totalLength);
        buffer.put((byte) (0x80 | message.getVersion())); // Version prefix
        
        for (String signature : signatures) {
            byte[] signatureBytes = Base58.decode(signature);
            if (signatureBytes.length != 64) {
                throw new IllegalStateException("Invalid signature length: " + signatureBytes.length);
            }
            buffer.put(signatureBytes);
        }
        
        buffer.put(messageBytes);
        return buffer.array();
    }

    /**
     * Gets the underlying message of the transaction.
     *
     * @return The VersionedMessage object
     */
    public VersionedMessage getMessage() {
        return message;
    }

    /**
     * Gets the list of signatures for the transaction.
     *
     * @return List of signature strings
     */
    public List<String> getSignatures() {
        return signatures;
    }
}
