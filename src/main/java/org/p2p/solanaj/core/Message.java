package org.p2p.solanaj.core;

import org.bitcoinj.core.Base58;
import org.p2p.solanaj.utils.ShortvecEncoding;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

import lombok.Getter;

import org.p2p.solanaj.utils.GuardedArrayUtils;

public class Message {
    @Getter
    public static class MessageHeader {
        static final int HEADER_LENGTH = 3;

        private final byte numRequiredSignatures;
        private final byte numReadonlySignedAccounts;
        private final byte numReadonlyUnsignedAccounts;

        byte[] toByteArray() {
            return new byte[] { numRequiredSignatures, numReadonlySignedAccounts, numReadonlyUnsignedAccounts };
        }

        MessageHeader(byte[] byteArray) {
            numRequiredSignatures = byteArray[0];
            numReadonlySignedAccounts = byteArray[1];
            numReadonlyUnsignedAccounts = byteArray[2];
        }

        @Override
        public String toString() {
            return "MessageHeader{" +
                    "numRequiredSignatures=" + numRequiredSignatures +
                    ", numReadonlySignedAccounts=" + numReadonlySignedAccounts +
                    ", numReadonlyUnsignedAccounts=" + numReadonlyUnsignedAccounts +
                    '}';
        }
    }

    @Getter
    public static class CompiledInstruction {
        private byte programIdIndex;
        private byte[] keyIndicesCount;
        private byte[] keyIndices;
        private byte[] dataLength;
        private byte[] data;

        int getLength() {
            // 1 = programIdIndex length
            return 1 + keyIndicesCount.length + keyIndices.length + dataLength.length + data.length;
        }

        @Override
        public String toString() {
            return "CompiledInstruction{" +
                    "programIdIndex=" + programIdIndex +
                    ", keyIndicesCount=" + Arrays.toString(keyIndicesCount) +
                    ", keyIndices=" + Arrays.toString(keyIndices) +
                    ", dataLength=" + Arrays.toString(dataLength) +
                    ", data=" + Arrays.toString(data) +
                    '}';
        }
    }

    @Getter
    public static class MessageAddressTableLookup {
        private PublicKey accountKey;
        private byte[] writableIndexesCountLength;
        private byte[] writableIndexes;
        private byte[] readonlyIndexesCountLength;
        private byte[] readonlyIndexes;

        int getLength() {
            // 1 = programIdIndex length
            return PublicKey.PUBLIC_KEY_LENGTH + writableIndexesCountLength.length + writableIndexes.length +
                    readonlyIndexesCountLength.length + readonlyIndexes.length;
        }

        @Override
        public String toString() {
            return "MessageAddressTableLookup{" +
                    "accountKey=" + accountKey +
                    ", writableIndexesCountLength=" + Arrays.toString(writableIndexesCountLength) +
                    ", writableIndexes=" + Arrays.toString(writableIndexes) +
                    ", readonlyIndexesCountLength=" + Arrays.toString(readonlyIndexesCountLength) +
                    ", readonlyIndexes=" + Arrays.toString(readonlyIndexes) +
                    '}';
        }
    }

    private static final int RECENT_BLOCK_HASH_LENGTH = 32;

    private MessageHeader messageHeader;
    private String recentBlockhash;
    private final AccountKeysList accountKeys;
    private final List<CompiledInstruction> compiledInstructions;
    private final List<MessageAddressTableLookup> addressTableLookups;
    private Account feePayer;

    public Message() {
        this.accountKeys = new AccountKeysList();
        this.compiledInstructions = new ArrayList<>();
        this.addressTableLookups = new ArrayList<>();
    }

    public Message(MessageHeader messageHeader, String recentBlockhash, AccountKeysList accountKeys,
                   List<CompiledInstruction> compiledInstructions, List<MessageAddressTableLookup> addressTableLookups) {
        this.messageHeader = messageHeader;
        this.recentBlockhash = recentBlockhash;
        this.accountKeys = accountKeys;
        this.compiledInstructions = compiledInstructions;
        this.addressTableLookups = addressTableLookups;
    }

    public Message addInstruction(TransactionInstruction instruction) {
        accountKeys.addAll(instruction.getKeys());
        accountKeys.add(new AccountMeta(instruction.getProgramId(), false, false));

        List<AccountMeta> keysList = getAccountKeys();
        int keysSize = instruction.getKeys().size();

        CompiledInstruction compiledInstruction = new CompiledInstruction();
        compiledInstruction.programIdIndex = (byte) findAccountIndex(keysList, instruction.getProgramId());
        compiledInstruction.keyIndicesCount = ShortvecEncoding.encodeLength(keysSize);
        byte[] keyIndices = new byte[keysSize];
        for (int i = 0; i < instruction.getKeys().size(); i++) {
            keyIndices[i] = (byte) findAccountIndex(keysList, instruction.getKeys().get(i).getPublicKey());
        }
        compiledInstruction.keyIndices = keyIndices;
        compiledInstruction.dataLength = ShortvecEncoding.encodeLength(instruction.getData().length);
        compiledInstruction.data = instruction.getData();
        compiledInstructions.add(compiledInstruction);
        return this;
    }

    public void setRecentBlockHash(String recentBlockhash) {
        this.recentBlockhash = recentBlockhash;
    }

    public byte[] serialize() {

        if (recentBlockhash == null) {
            throw new IllegalArgumentException("recentBlockhash required");
        }

        if (compiledInstructions.isEmpty()) {
            throw new IllegalArgumentException("No instructions provided");
        }

        List<AccountMeta> keysList = getAccountKeys();
        int accountKeysSize = keysList.size();

        byte[] accountAddressesLength = ShortvecEncoding.encodeLength(accountKeysSize);

        byte[] instructionsCountLength = ShortvecEncoding.encodeLength(compiledInstructions.size());
        int compiledInstructionsLength = 0;
        for (CompiledInstruction compiledInstruction : this.compiledInstructions) {
            compiledInstructionsLength += compiledInstruction.getLength();
        }

        byte[] addressTableLookupsCountLength = ShortvecEncoding.encodeLength(addressTableLookups.size());
        int addressTableLookupsLength = 0;
        for (MessageAddressTableLookup addressTableLookup : this.addressTableLookups) {
            addressTableLookupsLength += addressTableLookup.getLength();
        }
        int bufferSize = MessageHeader.HEADER_LENGTH + RECENT_BLOCK_HASH_LENGTH + accountAddressesLength.length
                + (accountKeysSize * PublicKey.PUBLIC_KEY_LENGTH) + instructionsCountLength.length
                + compiledInstructionsLength + addressTableLookupsCountLength.length + addressTableLookupsLength;

        ByteBuffer out = ByteBuffer.allocate(bufferSize);

        ByteBuffer accountKeysBuff = ByteBuffer.allocate(accountKeysSize * PublicKey.PUBLIC_KEY_LENGTH);
        for (AccountMeta accountMeta : keysList) {
            accountKeysBuff.put(accountMeta.getPublicKey().toByteArray());
        }

        out.put(messageHeader.toByteArray());

        out.put(accountAddressesLength);
        out.put(accountKeysBuff.array());

        out.put(Base58.decode(recentBlockhash));
        out.put(instructionsCountLength);
        for (CompiledInstruction compiledInstruction : compiledInstructions) {
            out.put(compiledInstruction.programIdIndex);
            out.put(compiledInstruction.keyIndicesCount);
            out.put(compiledInstruction.keyIndices);
            out.put(compiledInstruction.dataLength);
            out.put(compiledInstruction.data);
        }

        out.put(addressTableLookupsCountLength);
        for (MessageAddressTableLookup addressTableLookup : addressTableLookups) {
            out.put(addressTableLookup.accountKey.toByteArray());
            out.put(addressTableLookup.writableIndexesCountLength);
            out.put(addressTableLookup.writableIndexes);
            out.put(addressTableLookup.readonlyIndexesCountLength);
            out.put(addressTableLookup.readonlyIndexes);
        }

        return out.array();
    }

    public static Message deserialize(List<Byte> serializedMessageList) {
        // Remove the byte as it is used to indicate legacy Transaction.
        GuardedArrayUtils.guardedShift(serializedMessageList);

        // Remove three bytes for header
        byte[] messageHeaderBytes = GuardedArrayUtils.guardedSplice(serializedMessageList, 0, MessageHeader.HEADER_LENGTH);
        MessageHeader messageHeader = new MessageHeader(messageHeaderBytes);

        // Total static account keys
        int accountKeysSize = ShortvecEncoding.decodeLength(serializedMessageList);
        List<AccountMeta> accountKeys = new ArrayList<>(accountKeysSize);
        for (int i = 0; i < accountKeysSize; i++) {
            byte[] accountMetaPublicKeyByteArray = GuardedArrayUtils.guardedSplice(serializedMessageList, 0,
                    PublicKey.PUBLIC_KEY_LENGTH);
            PublicKey publicKey = new PublicKey(accountMetaPublicKeyByteArray);
            accountKeys.add(new AccountMeta(publicKey, false, false));
        }
        AccountKeysList accountKeysList = new AccountKeysList();
        accountKeysList.addAll(accountKeys);

        // recent_blockhash
        String recentBlockHash = Base58.encode(GuardedArrayUtils.guardedSplice(serializedMessageList, 0,
                PublicKey.PUBLIC_KEY_LENGTH));

        // Deserialize instructions
        int instructionsLength = ShortvecEncoding.decodeLength(serializedMessageList);
        List<CompiledInstruction> compiledInstructions = new ArrayList<>(instructionsLength);
        for (int i = 0; i < instructionsLength; i++) {
            CompiledInstruction compiledInstruction = new CompiledInstruction();
            compiledInstruction.programIdIndex = GuardedArrayUtils.guardedShift(serializedMessageList);
            int keysSize = ShortvecEncoding.decodeLength(serializedMessageList); // keysSize
            compiledInstruction.keyIndicesCount = ShortvecEncoding.encodeLength(keysSize);
            compiledInstruction.keyIndices = GuardedArrayUtils.guardedSplice(serializedMessageList, 0, keysSize);
            var dataLength = ShortvecEncoding.decodeLength(serializedMessageList);
            compiledInstruction.dataLength = ShortvecEncoding.encodeLength(dataLength);
            compiledInstruction.data = GuardedArrayUtils.guardedSplice(serializedMessageList, 0, dataLength);

            compiledInstructions.add(compiledInstruction);

        }

        // Deserialize addressTableLookups
        int addressTableLookupsLength = ShortvecEncoding.decodeLength(serializedMessageList);
        List<MessageAddressTableLookup> addressTableLookups = new ArrayList<>(addressTableLookupsLength);
        for (int i = 0; i < addressTableLookupsLength; i++) {
            MessageAddressTableLookup addressTableLookup = new MessageAddressTableLookup();
            byte[] accountKeyByteArray = GuardedArrayUtils.guardedSplice(serializedMessageList, 0, PublicKey.PUBLIC_KEY_LENGTH);
            addressTableLookup.accountKey = new PublicKey(accountKeyByteArray);
            int writableIndexesLength = ShortvecEncoding.decodeLength(serializedMessageList); // keysSize
            addressTableLookup.writableIndexesCountLength = ShortvecEncoding.encodeLength(writableIndexesLength);
            addressTableLookup.writableIndexes = GuardedArrayUtils.guardedSplice(serializedMessageList, 0, writableIndexesLength);
            int readonlyIndexesLength = ShortvecEncoding.decodeLength(serializedMessageList);
            addressTableLookup.readonlyIndexesCountLength = ShortvecEncoding.encodeLength(readonlyIndexesLength);
            addressTableLookup.readonlyIndexes = GuardedArrayUtils.guardedSplice(serializedMessageList, 0, readonlyIndexesLength);

            addressTableLookups.add(addressTableLookup);
        }

        return new Message(messageHeader, recentBlockHash, accountKeysList, compiledInstructions, addressTableLookups);
    }

    protected void setFeePayer(Account feePayer) {
        this.feePayer = feePayer;
    }

    public List<AccountMeta> getAccountKeys() {
        List<AccountMeta> keysList = accountKeys.getList();

        // Check whether custom sorting is needed. The `getAccountKeys()` method returns a reversed list of accounts, with signable and mutable accounts at the end, but the fee is placed first. When a transaction involves multiple accounts that need signing, an incorrect order can cause bugs. Change to custom sorting based on the contract order.
        boolean needSort = keysList.stream().anyMatch(accountMeta -> accountMeta.getSort() < Integer.MAX_VALUE);
        if (needSort) {
            // Sort in ascending order based on the `sort` field.
            return keysList.stream()
                    .sorted(Comparator.comparingInt(AccountMeta::getSort))
                    .collect(Collectors.toList());
        }

        int feePayerIndex = findAccountIndex(keysList, feePayer.getPublicKey());
        List<AccountMeta> newList = new ArrayList<AccountMeta>();
        AccountMeta feePayerMeta = keysList.get(feePayerIndex);
        newList.add(new AccountMeta(feePayerMeta.getPublicKey(), true, true));
        keysList.remove(feePayerIndex);
        newList.addAll(keysList);

        return newList;
    }

    public int findAccountIndex(List<AccountMeta> accountMetaList, PublicKey key) {
        for (int i = 0; i < accountMetaList.size(); i++) {
            if (accountMetaList.get(i).getPublicKey().equals(key)) {
                return i;
            }
        }

        throw new RuntimeException("unable to find account index");
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageHeader=" + messageHeader +
                ", recentBlockhash='" + recentBlockhash + '\'' +
                ", accountKeys=" + accountKeys +
                ", compiledInstructions=" + compiledInstructions +
                ", addressTableLookups=" + addressTableLookups +
                ", feePayer=" + feePayer +
                '}';
    }
}
