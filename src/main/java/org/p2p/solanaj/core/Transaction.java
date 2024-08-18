package org.p2p.solanaj.core;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bitcoinj.core.Base58;
import org.p2p.solanaj.utils.ByteUtils;
import org.p2p.solanaj.utils.GuardedArrayUtils;
import org.p2p.solanaj.utils.Shortvec;
import org.p2p.solanaj.utils.TweetNaclFast;

public class Transaction {

    public static final int SIGNATURE_LENGTH = 64;

    private final Message message;
    private final List<String> signatures;
    private byte[] serializedMessage;

    public Transaction() {
        this.message = new Message();
        this.signatures = new ArrayList<>();
    }

    public Transaction(Message message, List<String> signatures) {
        this.message = message;
        this.signatures = signatures;
    }

    public Transaction addInstruction(TransactionInstruction instruction) {
        message.addInstruction(instruction);

        return this;
    }

    public void setRecentBlockHash(String recentBlockhash) {
        message.setRecentBlockHash(recentBlockhash);
    }

    public void sign(Account signer) {
        sign(Collections.singletonList(signer));
    }

    public void sign(List<Account> signers) {
        if (signers.isEmpty()) {
            throw new IllegalArgumentException("No signers");
        }

        Account feePayer = signers.get(0);
        message.setFeePayer(feePayer);

        List<AccountMeta> signerPubKeys = List.copyOf(message.getAccountKeys());
        signerPubKeys = signerPubKeys.subList(0, signers.size());

        serializedMessage = message.serialize();

        for (Account signer : signers) {
            int signerIndex = message.findAccountIndex(signerPubKeys, signer.getPublicKey());
            if (signerIndex < 0) {
                throw new IllegalArgumentException("Cannot sign with non signer key: " +
                        signer.getPublicKey().toBase58());
            }
            TweetNaclFast.Signature signatureProvider = new TweetNaclFast.Signature(new byte[0], signer.getSecretKey());
            byte[] signature = signatureProvider.detached(serializedMessage);

            this.signatures.set(signerIndex, Base58.encode(signature));
        }
    }

    public byte[] serialize() {
        int signaturesSize = signatures.size();
        byte[] signaturesLength = Shortvec.encodeLength(signaturesSize);

        ByteBuffer out = ByteBuffer
                .allocate(signaturesLength.length + signaturesSize * SIGNATURE_LENGTH + serializedMessage.length);

        out.put(signaturesLength);

        for (String signature : signatures) {
            byte[] rawSignature = Base58.decode(signature);
            out.put(rawSignature);
        }

        out.put(serializedMessage);

        return out.array();
    }

    public static Transaction deserialize(byte[] serializedTransaction) {
        List<Byte> serializedTransactionList = ByteUtils.toByteList(serializedTransaction);

        int signaturesSize = Shortvec.decodeLength(serializedTransactionList);
        List<String> signatures = new ArrayList<>(signaturesSize);

        for (int i = 0; i < signaturesSize; i++) {

            byte[] signatureBytes = GuardedArrayUtils.guardedSplice(serializedTransactionList, 0, SIGNATURE_LENGTH);
            signatures.add(Base58.encode(signatureBytes));
        }

        Message message = Message.deserialize(serializedTransactionList);
        return new Transaction(message, signatures);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "message=" + message +
                ", signatures=" + signatures +
                ", serializedMessage=" + Arrays.toString(serializedMessage) +
                '}';
    }
}
