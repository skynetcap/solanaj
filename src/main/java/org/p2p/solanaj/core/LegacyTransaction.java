package org.p2p.solanaj.core;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bitcoinj.core.Base58;
import org.p2p.solanaj.utils.Shortvec;
import org.p2p.solanaj.utils.TweetNaclFast;

public class LegacyTransaction {

    public static final int SIGNATURE_LENGTH = 64;

    private final LegacyMessage legacyMessage;
    private final List<String> signatures;
    private byte[] serializedLegacyMessage;

    public LegacyTransaction() {
        this.legacyMessage = new LegacyMessage();
        this.signatures = new ArrayList<>();
    }

    public LegacyTransaction addInstruction(TransactionInstruction instruction) {
        legacyMessage.addInstruction(instruction);

        return this;
    }

    public void setRecentBlockHash(String recentBlockhash) {
        legacyMessage.setRecentBlockHash(recentBlockhash);
    }

    public void sign(Account signer) {
        sign(Collections.singletonList(signer));
    }

    public void sign(List<Account> signers) {

        if (signers.isEmpty()) {
            throw new IllegalArgumentException("No signers");
        }

        Account feePayer = signers.get(0);
        legacyMessage.setFeePayer(feePayer);

        serializedLegacyMessage = legacyMessage.serialize();

        for (Account signer : signers) {
            TweetNaclFast.Signature signatureProvider = new TweetNaclFast.Signature(new byte[0], signer.getSecretKey());
            byte[] signature = signatureProvider.detached(serializedLegacyMessage);

            signatures.add(Base58.encode(signature));
        }
    }

    public byte[] serialize() {
        int signaturesSize = signatures.size();
        byte[] signaturesLength = Shortvec.encodeLength(signaturesSize);

        ByteBuffer out = ByteBuffer
                .allocate(signaturesLength.length + signaturesSize * SIGNATURE_LENGTH + serializedLegacyMessage.length);

        out.put(signaturesLength);

        for (String signature : signatures) {
            byte[] rawSignature = Base58.decode(signature);
            out.put(rawSignature);
        }

        out.put(serializedLegacyMessage);

        return out.array();
    }
}