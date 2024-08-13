package org.p2p.solanaj.core;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.Sha256Hash;
import org.p2p.solanaj.utils.ByteUtils;
import org.p2p.solanaj.utils.PublicKeySerializer;
import org.p2p.solanaj.utils.TweetNaclFast;

@JsonSerialize(using = PublicKeySerializer.class)
public class PublicKey {

    public static final int PUBLIC_KEY_LENGTH = 32;

    private final byte[] pubkey;

    public PublicKey(String pubkey) {
        if (pubkey.length() < PUBLIC_KEY_LENGTH) {
            throw new IllegalArgumentException("Invalid public key input");
        }

        this.pubkey = Base58.decode(pubkey);
    }

    public PublicKey(byte[] pubkey) {

        if (pubkey.length > PUBLIC_KEY_LENGTH) {
            throw new IllegalArgumentException("Invalid public key input");
        }

        this.pubkey = pubkey;
    }

    public static PublicKey readPubkey(byte[] bytes, int offset) {
        byte[] buf = ByteUtils.readBytes(bytes, offset, PUBLIC_KEY_LENGTH);
        return new PublicKey(buf);
    }

    public byte[] toByteArray() {
        return pubkey;
    }

    public String toBase58() {
        return Base58.encode(pubkey);
    }

    public boolean equals(PublicKey pubkey) {
        return Arrays.equals(this.pubkey, pubkey.toByteArray());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(pubkey);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;
        PublicKey person = (PublicKey) o;
        return equals(person);
    }

    public String toString() {
        return toBase58();
    }

    public static PublicKey createProgramAddress(List<byte[]> seeds, PublicKey programId) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        for (byte[] seed : seeds) {
            if (seed.length > 32) {
                throw new IllegalArgumentException("Max seed length exceeded");
            }

            buffer.writeBytes(seed);
        }

        buffer.writeBytes(programId.toByteArray());
        buffer.writeBytes("ProgramDerivedAddress".getBytes());

        byte[] hash = Sha256Hash.hash(buffer.toByteArray());

        if (TweetNaclFast.is_on_curve(hash) != 0) {
            throw new RuntimeException("Invalid seeds, address must fall off the curve");
        }

        return new PublicKey(hash);
    }

    @Getter
    public static class ProgramDerivedAddress {
        private final PublicKey address;
        private final int nonce;

        public ProgramDerivedAddress(PublicKey address, int nonce) {
            this.address = address;
            this.nonce = nonce;
        }

    }

    public static ProgramDerivedAddress findProgramAddress(List<byte[]> seeds, PublicKey programId) throws Exception {
        int nonce = 255;
        PublicKey address;

        List<byte[]> seedsWithNonce = new ArrayList<>(seeds);

        while (nonce != 0) {
            try {
                seedsWithNonce.add(new byte[] { (byte) nonce });
                address = createProgramAddress(seedsWithNonce, programId);
            } catch (Exception e) {
                seedsWithNonce.remove(seedsWithNonce.size() - 1);
                nonce--;
                continue;
            }

            return new ProgramDerivedAddress(address, nonce);
        }

        throw new Exception("Unable to find a viable program address nonce");
    }

    public static PublicKey valueOf(String publicKey) {
        return new PublicKey(publicKey);
    }

}
