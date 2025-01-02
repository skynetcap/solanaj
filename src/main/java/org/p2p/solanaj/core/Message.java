package org.p2p.solanaj.core;

import org.bitcoinj.core.Base58;
import org.p2p.solanaj.utils.ArrayUtils;
import org.p2p.solanaj.utils.ShortvecEncoding;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Message {
    private static class MessageHeader {
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
        /**
         * #################################################
         * ########## Here's the change. sort ##############
         * #################################################
         *
         */
        Collections.sort(keysList, new Comparator<AccountMeta>() {
            @Override
            public int compare(AccountMeta o1, AccountMeta o2) {
                if(o2.isSigner()){
                    return 1;
                }else if(o1.isSigner()){
                    return -1;
                }else{
                    return 0;
                }
            }
        });
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

    /**
     * deserialize Message
     * @param serializedMessageList message serialize byte array
     * @return Message
     * @author jc0803kevin
     */
    public static Message deserialize(List<Byte> serializedMessageList) {
        // Remove the byte as it is used to indicate legacy Transaction.
//        GuardedArrayUtils.guardedShift(serializedMessageList);

        // Remove three bytes for header
        byte[] messageHeaderBytes = ArrayUtils.guardedSplice(serializedMessageList, 0, MessageHeader.HEADER_LENGTH);
        MessageHeader messageHeader = new MessageHeader(messageHeaderBytes);

        // Total static account keys
        int accountKeysSize = ShortvecEncoding.decodeLength(serializedMessageList);
        List<AccountMeta> accountKeys = new ArrayList<>(accountKeysSize);
        for (int i = 0; i < accountKeysSize; i++) {
            byte[] accountMetaPublicKeyByteArray = ArrayUtils.guardedSplice(serializedMessageList, 0,
                    PublicKey.PUBLIC_KEY_LENGTH);
            PublicKey publicKey = new PublicKey(accountMetaPublicKeyByteArray);
            accountKeys.add(new AccountMeta(publicKey, false, false));
        }

        // setSigner VS setWritable
        for (AccountMeta accountKey : accountKeys) {
            PublicKey publicKey = accountKey.getPublicKey();
            boolean isSigner = isSigner(accountKeys, publicKey, messageHeader);
            boolean isWriter = isWriter(accountKeys, publicKey, messageHeader);
            accountKey.setSigner(isSigner);
            accountKey.setWritable(isWriter);
        }

        AccountKeysList accountKeysList = new AccountKeysList();
        accountKeysList.addAll(accountKeys);

        // recent_blockhash
        String recentBlockHash = Base58.encode(ArrayUtils.guardedSplice(serializedMessageList, 0,
                PublicKey.PUBLIC_KEY_LENGTH));

        // Deserialize instructions
        int instructionsLength = ShortvecEncoding.decodeLength(serializedMessageList);
        List<TransactionInstruction> instructions = new ArrayList<>();
        List<CompiledInstruction> compiledInstructions = new ArrayList<>(instructionsLength);
        for (int i = 0; i < instructionsLength; i++) {
            CompiledInstruction compiledInstruction = new CompiledInstruction();
            compiledInstruction.programIdIndex = ArrayUtils.guardedShift(serializedMessageList);
            int keysSize = ShortvecEncoding.decodeLength(serializedMessageList); // keysSize
            compiledInstruction.keyIndicesCount = ShortvecEncoding.encodeLength(keysSize);
            compiledInstruction.keyIndices = ArrayUtils.guardedSplice(serializedMessageList, 0, keysSize);
            var dataLength = ShortvecEncoding.decodeLength(serializedMessageList);
            compiledInstruction.dataLength = ShortvecEncoding.encodeLength(dataLength);
            compiledInstruction.data = ArrayUtils.guardedSplice(serializedMessageList, 0, dataLength);

            compiledInstructions.add(compiledInstruction);

            PublicKey programId = accountKeys.get(compiledInstruction.programIdIndex).getPublicKey();
            List<AccountMeta> keys = new ArrayList<>();
            for (int i1 = 0; i1 < compiledInstruction.keyIndices.length; i1++) {
                keys.add(accountKeys.get(compiledInstruction.keyIndices[i1]));
            }
            instructions.add(new TransactionInstruction(programId, keys, compiledInstruction.data));
        }

        return new Message(messageHeader, recentBlockHash, accountKeysList, instructions);
    }

    private static boolean isWriter(List<AccountMeta> accountKeys, PublicKey account, MessageHeader messageHeader){

        int index = indexOf(accountKeys, account);
        if(index == -1){
            return false;
        }
        boolean isSignerWriter= index < messageHeader.numRequiredSignatures - messageHeader.numReadonlySignedAccounts;
        boolean isNonSigner = index >= messageHeader.numRequiredSignatures;
        boolean isNonSignerReadonly = index >= (accountKeys.size() - messageHeader.numReadonlyUnsignedAccounts);
        boolean isNonSignerWriter = isNonSigner && !isNonSignerReadonly;
        return isSignerWriter || isNonSignerWriter;
    }

    private static boolean isSigner(List<AccountMeta> accountKeys, PublicKey account, MessageHeader messageHeader) {
        int index = indexOf(accountKeys, account);

        if (index == -1) {
            return false;
        }

        return index < messageHeader.numRequiredSignatures;
    }

    private static int indexOf(List<AccountMeta> accountKeys, PublicKey account){
        for (int i = 0; i < accountKeys.size(); i++) {
            if(account.toBase58().equals(accountKeys.get(i).getPublicKey().toBase58())){
                return i;
            }
        }

        return -1;
    }

}
