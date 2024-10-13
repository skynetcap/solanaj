package org.p2p.solanaj.core;

import org.bitcoinj.core.Base58;
import org.p2p.solanaj.utils.ShortvecEncoding;

import lombok.Setter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a versioned message for Solana transactions, supporting Address Lookup Tables.
 * This class allows adding instructions and address table lookups, and serializing the message.
 */
public class VersionedMessage {

    private byte version;
    private MessageHeader header;
    private List<PublicKey> accountKeys;
    private String recentBlockhash;
    private List<CompiledInstruction> instructions;
    private List<MessageAddressTableLookup> addressTableLookups;

    // Maps to track unique account keys and their indices
    private Map<PublicKey, Integer> accountKeyIndexMap;

    /**
     * Constructs a new VersionedMessage with default values.
     */
    public VersionedMessage() {
        this.version = 0; // Version 0
        this.header = new MessageHeader();
        this.accountKeys = new ArrayList<>();
        this.instructions = new ArrayList<>();
        this.addressTableLookups = new ArrayList<>();
        this.accountKeyIndexMap = new HashMap<>();
    }

    private int calculateSize() {
        int size = 1; // Version byte
        size += header.getSerializedSize();
        size += ShortvecEncoding.encodeLength(accountKeys.size()).length + (accountKeys.size() * 32);
        size += 32; // Recent blockhash
        size += ShortvecEncoding.encodeLength(instructions.size()).length;
        for (CompiledInstruction instruction : instructions) {
            size += instruction.getLength();
        }
        size += ShortvecEncoding.encodeLength(addressTableLookups.size()).length;
        for (MessageAddressTableLookup lookup : addressTableLookups) {
            size += lookup.getSerializedLength();
        }
        return size;
    }

    /**
     * Adds a TransactionInstruction to the message.
     *
     * @param instruction The TransactionInstruction to add
     */
    public void addInstruction(TransactionInstruction instruction) {
        Objects.requireNonNull(instruction, "Instruction cannot be null");
        CompiledInstruction compiled = compileInstruction(instruction);
        instructions.add(compiled);
    }


    /**
     * Adds an Address Lookup Table to the message.
     *
     * @param lookupTable The AddressTableLookup to add
     */
    public void addAddressTableLookup(AddressTableLookup lookupTable) {
        Objects.requireNonNull(lookupTable, "LookupTable cannot be null");
        MessageAddressTableLookup messageLookup = new MessageAddressTableLookup(
                lookupTable.getAccountKey(),
                lookupTable.getWritableIndexes(),
                lookupTable.getReadonlyIndexes()
        );
        addressTableLookups.add(messageLookup);
    }

    /**
     * Sets the recent blockhash for the transaction.
     *
     * @param recentBlockhash The recent blockhash to set
     */
    public void setRecentBlockhash(String recentBlockhash) {
        this.recentBlockhash = Objects.requireNonNull(recentBlockhash, "RecentBlockhash cannot be null");
    }


    /**
     * Serializes the versioned message into a byte array, including Address Lookup Tables.
     *
     * @return The serialized message as a byte array
     */
    public byte[] serialize() {
        ByteBuffer buffer = ByteBuffer.allocate(calculateSize());
        buffer.put(header.serialize());
        buffer.put(ShortvecEncoding.encodeLength(accountKeys.size()));
        for (PublicKey key : accountKeys) {
            buffer.put(key.toByteArray());
        }
        buffer.put(Base58.decode(recentBlockhash));
        buffer.put(ShortvecEncoding.encodeLength(instructions.size()));
        for (CompiledInstruction instruction : instructions) {
            buffer.put(instruction.serialize());
        }
        buffer.put(ShortvecEncoding.encodeLength(addressTableLookups.size()));
        for (MessageAddressTableLookup lookup : addressTableLookups) {
            buffer.put(lookup.serialize());
        }
        return buffer.array();
    }

    /**
     * Serializes the V0 message according to Solana's specification.
     *
     * @return Serialized V0 message as a byte array
     */
    private byte[] serializeV0Message() {
        byte[] headerBytes = header.serialize();
        byte[] accountKeysBytes = serializeAccountKeys();
        byte[] recentBlockhashBytes = recentBlockhash.getBytes();
        byte[] instructionsBytes = serializeInstructions();
        byte[] addressTableLookupsBytes = serializeAddressTableLookups();

        ByteBuffer buffer = ByteBuffer.allocate(
                headerBytes.length +
                        accountKeysBytes.length +
                        recentBlockhashBytes.length +
                        instructionsBytes.length +
                        addressTableLookupsBytes.length
        );

        buffer.put(headerBytes);
        buffer.put(accountKeysBytes);
        buffer.put(recentBlockhashBytes);
        buffer.put(instructionsBytes);
        buffer.put(addressTableLookupsBytes);

        return buffer.array();
    }


    /**
     * Serializes the account keys with their respective lengths.
     *
     * @return Serialized account keys as a byte array
     */
    private byte[] serializeAccountKeys() {
        byte[] accountKeysLength = ShortvecEncoding.encodeLength(accountKeys.size());
        ByteBuffer buffer = ByteBuffer.allocate(accountKeysLength.length + accountKeys.size() * 32);
        buffer.put(accountKeysLength);
        for (PublicKey key : accountKeys) {
            buffer.put(key.toByteArray());
        }
        return buffer.array();
    }

    /**
     * Serializes the instructions with their respective lengths.
     *
     * @return Serialized instructions as a byte array
     */
    private byte[] serializeInstructions() {
        byte[] instructionsLength = ShortvecEncoding.encodeLength(instructions.size());
        int totalInstructionBytes = instructions.stream().mapToInt(CompiledInstruction::getLength).sum();
        ByteBuffer buffer = ByteBuffer.allocate(instructionsLength.length + totalInstructionBytes);
        buffer.put(instructionsLength);
        for (CompiledInstruction instruction : instructions) {
            buffer.put(instruction.serialize());
        }
        return buffer.array();
    }

    /**
     * Serializes the address table lookups with their respective lengths.
     *
     * @return Serialized address table lookups as a byte array
     */
    private byte[] serializeAddressTableLookups() {
        byte[] lookupsLength = ShortvecEncoding.encodeLength(addressTableLookups.size());
        int totalLookupBytes = addressTableLookups.stream().mapToInt(MessageAddressTableLookup::getSerializedLength).sum();
        ByteBuffer buffer = ByteBuffer.allocate(lookupsLength.length + totalLookupBytes);
        buffer.put(lookupsLength);
        for (MessageAddressTableLookup lookup : addressTableLookups) {
            buffer.put(lookup.serialize());
        }
        return buffer.array();
    }

    /**
     * Compiles a TransactionInstruction into a CompiledInstruction by mapping
     * program ID and account keys to their respective indices.
     *
     * @param instruction The TransactionInstruction to compile
     * @return The compiled instruction
     */
    private CompiledInstruction compileInstruction(TransactionInstruction instruction) {
        // Ensure program ID is in accountKeys
        int programIdIndex = addAccountKey(instruction.getProgramId());

        // Map account keys to indices
        List<Integer> accountIndices = new ArrayList<>();
        for (AccountMeta meta : instruction.getKeys()) {
            accountIndices.add(addAccountKey(meta.getPublicKey()));
        }

        // Encode account indices as u8
        byte[] accountIndicesBytes = new byte[accountIndices.size()];
        for (int i = 0; i < accountIndices.size(); i++) {
            int index = accountIndices.get(i);
            if (index > 255) {
                throw new IllegalStateException("Account index exceeds u8 limit");
            }
            accountIndicesBytes[i] = (byte) index;
        }

        // Compile instruction data
        byte[] data = instruction.getData();

        return new CompiledInstruction(
                (byte) programIdIndex,
                ShortvecEncoding.encodeLength(accountIndices.size()),
                accountIndicesBytes,
                ShortvecEncoding.encodeLength(data.length),
                data
        );
    }


    /**
     * Adds an account key to the accountKeys list if not already present.
     *
     * @param key The PublicKey to add
     * @return The index of the key in the accountKeys list
     */
    private int addAccountKey(PublicKey key) {
        if (accountKeyIndexMap.containsKey(key)) {
            return accountKeyIndexMap.get(key);
        } else {
            accountKeys.add(key);
            int index = accountKeys.size() - 1;
            accountKeyIndexMap.put(key, index);
            return index;
        }
    }

    // Getters and setters...

    /**
     * Gets the version of the message.
     *
     * @return The version byte
     */
    public byte getVersion() {
        return version;
    }

    /**
     * Sets the version of the message.
     *
     * @param version The version byte to set
     */
    public void setVersion(byte version) {
        this.version = version;
    }


    /**
     * Gets the list of account keys.
     *
     * @return List of PublicKey objects
     */
    public List<PublicKey> getAccountKeys() {
        return accountKeys;
    }

    /**
     * Gets the message header.
     *
     * @return The MessageHeader object
     */
    public MessageHeader getHeader() {
        return header;
    }

    /**
     * Sets the message header.
     *
     * @param header The MessageHeader object to set
     */
    public void setHeader(MessageHeader header) {
        this.header = header;
    }

    /**
     * Gets the recent blockhash.
     *
     * @return The recent blockhash as a String
     */
    public String getRecentBlockhash() {
        return recentBlockhash;
    }

    /**
     * Gets the list of instructions.
     *
     * @return List of CompiledInstruction objects
     */
    public List<CompiledInstruction> getInstructions() {
        return instructions;
    }

    /**
     * Gets the list of Address Lookup Tables.
     *
     * @return List of MessageAddressTableLookup objects
     */
    public List<MessageAddressTableLookup> getAddressTableLookups() {
        return addressTableLookups;
    }

    /**
     * Inner class representing the message header.
     */
    @Setter
    public static class MessageHeader {
        private byte numRequiredSignatures;
        private byte numReadonlySignedAccounts;
        private byte numReadonlyUnsignedAccounts;

        public byte[] serialize() {
            ByteBuffer buffer = ByteBuffer.allocate(3);
            buffer.put(numRequiredSignatures);
            buffer.put(numReadonlySignedAccounts);
            buffer.put(numReadonlyUnsignedAccounts);
            return buffer.array();
        }

        public int getSerializedSize() {
            return 3;
        }

        // Getters and setters...
    }

    /**
     * Inner class representing a compiled instruction.
     */
    public static class CompiledInstruction {
        private byte programIdIndex;
        private byte[] accountsCount;
        private byte[] accounts;
        private byte[] dataLength;
        private byte[] data;

        /**
         * Constructs a CompiledInstruction from components.
         *
         * @param programIdIndex The index of the program ID in accountKeys
         * @param accountsCount  The encoded length of accounts
         * @param accounts       The account indices as bytes
         * @param dataLength     The encoded length of data
         * @param data           The instruction data
         */
        public CompiledInstruction(byte programIdIndex, byte[] accountsCount, byte[] accounts, byte[] dataLength, byte[] data) {
            this.programIdIndex = programIdIndex;
            this.accountsCount = accountsCount;
            this.accounts = accounts;
            this.dataLength = dataLength;
            this.data = data;
        }

        /**
         * Serializes the compiled instruction into a byte array.
         *
         * @return Byte array representing the compiled instruction
         */
        public byte[] serialize() {
            ByteBuffer buffer = ByteBuffer.allocate(
                    1 + // programIdIndex
                            accountsCount.length +
                            accounts.length +
                            dataLength.length +
                            data.length
            );
            buffer.put(programIdIndex);
            buffer.put(accountsCount);
            buffer.put(accounts);
            buffer.put(dataLength);
            buffer.put(data);
            return buffer.array();
        }


        /**
         * Gets the length of the compiled instruction.
         *
         * @return Length in bytes
         */
        public int getLength() {
            return 1 + accountsCount.length + accounts.length + dataLength.length + data.length;
        }
    }


    /**
     * Inner class representing a MessageAddressTableLookup.
     */
    public static class MessageAddressTableLookup {
        private final PublicKey accountKey;
        private final List<Byte> writableIndexes;
        private final List<Byte> readonlyIndexes;

        /**
         * Constructs a MessageAddressTableLookup with the given parameters.
         *
         * @param accountKey      The public key of the address lookup table
         * @param writableIndexes The list of writable address indexes
         * @param readonlyIndexes The list of readonly address indexes
         */
        public MessageAddressTableLookup(PublicKey accountKey, List<Integer> writableIndexes, List<Integer> readonlyIndexes) {
            this.accountKey = Objects.requireNonNull(accountKey, "AccountKey cannot be null");
            this.writableIndexes = new ArrayList<>();
            for (Integer index : writableIndexes) {
                if (index < 0 || index > 255) {
                    throw new IllegalArgumentException("Writable index must be between 0 and 255");
                }
                this.writableIndexes.add(index.byteValue());
            }
            this.readonlyIndexes = new ArrayList<>();
            for (Integer index : readonlyIndexes) {
                if (index < 0 || index > 255) {
                    throw new IllegalArgumentException("Readonly index must be between 0 and 255");
                }
                this.readonlyIndexes.add(index.byteValue());
            }
        }

        /**
         * Serializes the MessageAddressTableLookup into a byte array.
         *
         * @return Byte array representing the serialized lookup table
         */
        public byte[] serialize() {
            byte[] accountKeyBytes = accountKey.toByteArray();
            byte[] writableLength = ShortvecEncoding.encodeLength(writableIndexes.size());
            byte[] readonlyLength = ShortvecEncoding.encodeLength(readonlyIndexes.size());

            ByteBuffer buffer = ByteBuffer.allocate(
                    accountKeyBytes.length +
                            writableLength.length +
                            writableIndexes.size() +
                            readonlyLength.length +
                            readonlyIndexes.size()
            );
            buffer.put(accountKeyBytes);
            buffer.put(writableLength);
            for (Byte index : writableIndexes) {
                buffer.put(index);
            }
            buffer.put(readonlyLength);
            for (Byte index : readonlyIndexes) {
                buffer.put(index);
            }
            return buffer.array();
        }

        /**
         * Gets the serialized length of the MessageAddressTableLookup.
         *
         * @return Length in bytes
         */
        public int getSerializedLength() {
            return 32 + // accountKey
                    ShortvecEncoding.encodeLength(writableIndexes.size()).length +
                    writableIndexes.size() +
                    ShortvecEncoding.encodeLength(readonlyIndexes.size()).length +
                    readonlyIndexes.size();
        }

        // Getters

        /**
         * Gets the account key of the lookup table.
         *
         * @return The PublicKey of the lookup table
         */
        public PublicKey getAccountKey() {
            return accountKey;
        }

        /**
         * Gets the writable indexes.
         *
         * @return List of writable indexes as bytes
         */
        public List<Byte> getWritableIndexes() {
            return writableIndexes;
        }


        /**
         * Gets the readonly indexes.
         *
         * @return List of readonly indexes as bytes
         */
        public List<Byte> getReadonlyIndexes() {
            return readonlyIndexes;
        }
    }
}