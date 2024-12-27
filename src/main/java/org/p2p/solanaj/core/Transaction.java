package org.p2p.solanaj.core;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.bitcoinj.core.Base58;
import org.p2p.solanaj.utils.ByteUtils;
import org.p2p.solanaj.utils.GuardedArrayUtils;
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

    /**
     * Constructs a new Transaction instance.
     */
    public Transaction() {
        this.message = new Message();
        this.signatures = new ArrayList<>(); // Use diamond operator
    }

    public Transaction(Message message, List<String> signatures) {
        this.message = message;
        this.signatures = signatures;
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

    /**
     * Signs the transaction with a single signer.
     *
     * @param signer The account to sign the transaction
     * @throws NullPointerException if the signer is null
     */
    public void sign(Account signer) {
        sign(List.of(Objects.requireNonNull(signer, "Signer cannot be null"))); // Add input validation
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
        message.setFeePayer(feePayer.getPublicKey());

        serializedMessage = message.serialize();

        for (Account signer : signers) {
            try {
                TweetNaclFast.Signature signatureProvider = new TweetNaclFast.Signature(new byte[0], signer.getSecretKey());
                byte[] signature = signatureProvider.detached(serializedMessage);
                signatures.add(Base58.encode(signature));
            } catch (Exception e) {
                throw new RuntimeException("Error signing transaction", e); // Improve exception handling
            }
        }
    }

    public String getTxHash(){
        if (signatures == null || signatures.isEmpty()){
            return null;
        }
        return signatures.get(0);
    }

    /**
     * Serializes the transaction into a byte array.
     *
     * @return The serialized transaction as a byte array
     */
    public byte[] serialize() {
        int signaturesSize = signatures.size();
        byte[] signaturesLength = ShortvecEncoding.encodeLength(signaturesSize);

        // Calculate total size before allocating ByteBuffer
        int totalSize = signaturesLength.length + signaturesSize * SIGNATURE_LENGTH + serializedMessage.length;
        ByteBuffer out = ByteBuffer.allocate(totalSize);

        out.put(signaturesLength);

        for (String signature : signatures) {
            byte[] rawSignature = Base58.decode(signature);
            out.put(rawSignature);
        }

        out.put(serializedMessage);

        return out.array();
    }


//    public static Transaction deserialize(byte[] serializeTx){
//        int signaturesSize = ShortvecEncoding.decodeLength(serializeTx);
//        List<String> signatures = new ArrayList<>(signaturesSize);
//
//        List<Byte> serializedTransactionList = ByteUtils.toByteList(serializeTx);
//
//        for (int i = 0; i < signaturesSize; i++) {
//
//            byte[] signatureBytes = GuardedArrayUtils.guardedSplice(serializedTransactionList, 0, SIGNATURE_LENGTH);
//            signatures.add(Base58.encode(signatureBytes));
//        }
//
////        Message message = Message.deserialize(serializedTransactionList);
//        return new Transaction(null, signatures);
//    }


//    public static Transaction deserialize(byte[] serializedTransaction) {
//        List<Byte> serializedTransactionList = ByteUtils.toByteList(serializedTransaction);
//
//        int signaturesSize = ShortvecEncoding.decodeLength(serializedTransaction);
//        List<String> signatures = new ArrayList<>(signaturesSize);
//
//        byte[] byteArray = serializedTransaction;
//
//        for (int i = 0; i < signaturesSize; i++) {
//
//            byte[] signatureBytes = GuardedArrayUtils.guardedSplice(serializedTransactionList, 0, SIGNATURE_LENGTH);
//            signatures.add(Base58.encode(signatureBytes));
//
////            System.out.println("byteArray--1->" + Arrays.toString(byteArray));
////            byte[] signature = slice(byteArray, 0, Transaction.SIGNATURE_LENGTH);
////            System.out.println("signature-- ->" + Arrays.toString(signature));
////            signatures.add(Base58.encode(signature));
////
////            System.out.println("byteArray--2->" + Arrays.toString(byteArray));
////            byteArray = Arrays.copyOfRange(byteArray, Transaction.SIGNATURE_LENGTH, byteArray.length);
////
////            System.out.println("byteArray--3->" + Arrays.toString(byteArray));
////            signatures.add(Base58.encode(signature));
//        }
//
//        // Message message = Message.deserialize(serializedTransactionList);
//        // todo kevin
//        return new Transaction(null, signatures);
//    }

    public static Transaction deserialize(byte[] serializedTransaction) {
        List<Byte> serializedTransactionList = ByteUtils.toByteList(serializedTransaction);

        int signaturesSize = ShortvecEncoding.decodeLength(serializedTransactionList);
        List<String> signatures = new ArrayList<>(signaturesSize);

        for (int i = 0; i < signaturesSize; i++) {

            byte[] signatureBytes = GuardedArrayUtils.guardedSplice(serializedTransactionList, 0, SIGNATURE_LENGTH);
            signatures.add(Base58.encode(signatureBytes));
        }

         Message message = Message.deserialize(serializedTransactionList);
        return new Transaction(message, signatures);
    }

    public static byte[] slice(byte[] data, int start, int length) {
        byte[] slice = new byte[length];
        System.arraycopy(data, start, slice, 0, length);
        return slice;
    }
}
