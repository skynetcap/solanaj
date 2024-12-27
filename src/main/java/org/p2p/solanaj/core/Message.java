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
    public static class MessageHeader {
        static final int HEADER_LENGTH = 3;

        byte numRequiredSignatures = 0;
        byte numReadonlySignedAccounts = 0;
        byte numReadonlyUnsignedAccounts = 0;

        public MessageHeader(){}

        MessageHeader(byte[] byteArray) {
            numRequiredSignatures = byteArray[0];
            numReadonlySignedAccounts = byteArray[1];
            numReadonlyUnsignedAccounts = byteArray[2];
        }

        byte[] toByteArray() {
            return new byte[] { numRequiredSignatures, numReadonlySignedAccounts, numReadonlyUnsignedAccounts };
        }
    }

    private static class CompiledInstruction {
        byte programIdIndex;
        byte[] keyIndicesCount;
        byte[] keyIndices;
        byte[] dataLength;
        byte[] data;

        int getLength() {
            // 1 = programIdIndex length
            return 1 + keyIndicesCount.length + keyIndices.length + dataLength.length + data.length;
        }
    }

    private static final int RECENT_BLOCK_HASH_LENGTH = 32;

    private MessageHeader messageHeader;
    private String recentBlockhash;
    private final AccountKeysList accountKeys;
    private final List<TransactionInstruction> instructions;
    private PublicKey feePayer;

    public Message() {
        this.accountKeys = new AccountKeysList();
        this.instructions = new ArrayList<>();
    }

    public Message(MessageHeader messageHeader, String recentBlockhash, AccountKeysList accountKeys,
                   List<TransactionInstruction> compiledInstructions) {
        this.messageHeader = messageHeader;
        this.recentBlockhash = recentBlockhash;
        this.accountKeys = accountKeys;
        this.instructions = compiledInstructions;

        this.feePayer = accountKeys.getList().get(0).getPublicKey();
    }

    public Message addInstruction(TransactionInstruction instruction) {
        accountKeys.addAll(instruction.getKeys());
        accountKeys.add(new AccountMeta(instruction.getProgramId(), false, false));
        instructions.add(instruction);

//        List<AccountMeta> keysList = getAccountKeys();
//        int keysSize = instruction.getKeys().size();
//
//        CompiledInstruction compiledInstruction = new CompiledInstruction();
//        compiledInstruction.programIdIndex = (byte) findAccountIndex(keysList, instruction.getProgramId());
//        compiledInstruction.keyIndicesCount = ShortvecEncoding.encodeLength(keysSize);
//        byte[] keyIndices = new byte[keysSize];
//        for (int i = 0; i < instruction.getKeys().size(); i++) {
//            keyIndices[i] = (byte) findAccountIndex(keysList, instruction.getKeys().get(i).getPublicKey());
//        }
//        compiledInstruction.keyIndices = keyIndices;
//        compiledInstruction.dataLength = ShortvecEncoding.encodeLength(instruction.getData().length);
//        compiledInstruction.data = instruction.getData();
//        instructions.add(compiledInstruction);

        return this;
    }

    public void setRecentBlockHash(String recentBlockhash) {
        this.recentBlockhash = recentBlockhash;
    }

    public String getRecentBlockhash() {
        return recentBlockhash;
    }

    public MessageHeader getMessageHeader() {
        return messageHeader;
    }

    public List<TransactionInstruction> getInstructions() {
        return instructions;
    }

    public byte[] serialize() {

        if (recentBlockhash == null) {
            throw new IllegalArgumentException("recentBlockhash required");
        }

        if (instructions.isEmpty()) {
            throw new IllegalArgumentException("No instructions provided");
        }

        messageHeader = new MessageHeader();

        List<AccountMeta> keysList = getAccountKeys();
        int accountKeysSize = keysList.size();

        byte[] accountAddressesLength = ShortvecEncoding.encodeLength(accountKeysSize);

        int compiledInstructionsLength = 0;
        List<CompiledInstruction> compiledInstructions = new ArrayList<>();

        for (TransactionInstruction instruction : instructions) {
            int keysSize = instruction.getKeys().size();

            byte[] keyIndices = new byte[keysSize];
            for (int i = 0; i < keysSize; i++) {
                keyIndices[i] = (byte) findAccountIndex(keysList, instruction.getKeys().get(i).getPublicKey());
            }

            CompiledInstruction compiledInstruction = new CompiledInstruction();
            compiledInstruction.programIdIndex = (byte) findAccountIndex(keysList, instruction.getProgramId());
            compiledInstruction.keyIndicesCount = ShortvecEncoding.encodeLength(keysSize);
            compiledInstruction.keyIndices = keyIndices;
            compiledInstruction.dataLength = ShortvecEncoding.encodeLength(instruction.getData().length);
            compiledInstruction.data = instruction.getData();

            compiledInstructions.add(compiledInstruction);

            compiledInstructionsLength += compiledInstruction.getLength();
        }

        byte[] instructionsLength = ShortvecEncoding.encodeLength(compiledInstructions.size());

        int bufferSize = MessageHeader.HEADER_LENGTH + RECENT_BLOCK_HASH_LENGTH + accountAddressesLength.length
                + (accountKeysSize * PublicKey.PUBLIC_KEY_LENGTH) + instructionsLength.length
                + compiledInstructionsLength;

        ByteBuffer out = ByteBuffer.allocate(bufferSize);

        ByteBuffer accountKeysBuff = ByteBuffer.allocate(accountKeysSize * PublicKey.PUBLIC_KEY_LENGTH);
        for (AccountMeta accountMeta : keysList) {
            accountKeysBuff.put(accountMeta.getPublicKey().toByteArray());

            if (accountMeta.isSigner()) {
                messageHeader.numRequiredSignatures += 1;
                if (!accountMeta.isWritable()) {
                    messageHeader.numReadonlySignedAccounts += 1;
                }
            } else {
                if (!accountMeta.isWritable()) {
                    messageHeader.numReadonlyUnsignedAccounts += 1;
                }
            }
        }

        out.put(messageHeader.toByteArray());

        out.put(accountAddressesLength);
        out.put(accountKeysBuff.array());

        out.put(Base58.decode(recentBlockhash));

        out.put(instructionsLength);
        for (CompiledInstruction compiledInstruction : compiledInstructions) {
            out.put(compiledInstruction.programIdIndex);
            out.put(compiledInstruction.keyIndicesCount);
            out.put(compiledInstruction.keyIndices);
            out.put(compiledInstruction.dataLength);
            out.put(compiledInstruction.data);
        }

        return out.array();
    }

    protected void setFeePayer(PublicKey feePayer) {
        this.feePayer = feePayer;
    }

    public PublicKey getFeePayer() {
        return feePayer;
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

        int feePayerIndex = findAccountIndex(keysList, feePayer);
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

    public static Message deserialize(List<Byte> serializedMessageList) {
        // Remove the byte as it is used to indicate legacy Transaction.
//        GuardedArrayUtils.guardedShift(serializedMessageList);

        // Remove three bytes for header
        byte[] messageHeaderBytes = GuardedArrayUtils.guardedSplice(serializedMessageList, 0, MessageHeader.HEADER_LENGTH);
        byte numRequiredSignatures = messageHeaderBytes[0];
        byte numReadonlySignedAccounts = messageHeaderBytes[1];
        byte numReadonlyUnsignedAccounts = messageHeaderBytes[2];
        MessageHeader messageHeader = new MessageHeader(messageHeaderBytes);

        // Total static account keys
        int accountKeysSize = ShortvecEncoding.decodeLength(serializedMessageList);
//        byte[] accountAddressesLength = ShortvecEncoding.encodeLength(accountKeysSize);
//        GuardedArrayUtils.guardedSplice(serializedMessageList, 0, accountAddressesLength.length);



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
        List<TransactionInstruction> instructions = new ArrayList<>();
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

            PublicKey programId = accountKeys.get(compiledInstruction.programIdIndex).getPublicKey();
            List<AccountMeta> keys = new ArrayList<>();
            for (int i1 = 0; i1 < compiledInstruction.keyIndices.length; i1++) {
                keys.add(accountKeys.get(compiledInstruction.keyIndices[i1]));
            }
            instructions.add(new TransactionInstruction(programId, keys, compiledInstruction.data));
        }

        return new Message(messageHeader, recentBlockHash, accountKeysList, instructions);
//        return new Message();
    }

}
