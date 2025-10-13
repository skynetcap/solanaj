package org.p2p.solanaj.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.p2p.solanaj.utils.Base58Utils;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.p2p.solanaj.utils.ByteUtils;
import org.p2p.solanaj.utils.PublicKeySerializer;
import org.p2p.solanaj.utils.TweetNaclFast;

@JsonSerialize(using = PublicKeySerializer.class)
public class PublicKey {

    public static final int PUBLIC_KEY_LENGTH = 32;

    private final byte[] pubkey;

    public PublicKey(String pubkey) {
        if (pubkey.length() < PUBLIC_KEY_LENGTH) {
            throw new IllegalArgumentException("Invalid public key input: length must be at least " + PUBLIC_KEY_LENGTH);
        }
        this.pubkey = Base58Utils.decode(pubkey);
    }

    public PublicKey(byte[] pubkey) {
        if (pubkey.length != PUBLIC_KEY_LENGTH) {
            throw new IllegalArgumentException("Invalid public key input: length must be exactly " + PUBLIC_KEY_LENGTH);
        }
        this.pubkey = Arrays.copyOf(pubkey, PUBLIC_KEY_LENGTH);
    }

    public static PublicKey readPubkey(byte[] bytes, int offset) {
        byte[] buf = ByteUtils.readBytes(bytes, offset, PUBLIC_KEY_LENGTH);
        return new PublicKey(buf);
    }

    public byte[] toByteArray() {
        return pubkey;
    }

    public String toBase58() {
        return Base58Utils.encode(pubkey);
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
        if (this == o) return true;
        if (!(o instanceof PublicKey)) return false;
        PublicKey other = (PublicKey) o;
        return Arrays.equals(this.pubkey, other.pubkey);
    }

    public String toString() {
        return toBase58();
    }

    public static PublicKey createProgramAddress(List<byte[]> seeds, PublicKey programId) {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            for (byte[] seed : seeds) {
                if (seed.length > 32) {
                    throw new IllegalArgumentException("Max seed length exceeded: " + seed.length);
                }
                buffer.write(seed);
            }
            buffer.write(programId.toByteArray());
            buffer.write("ProgramDerivedAddress".getBytes());

            byte[] hash = sha256Hash(buffer.toByteArray());

            if (TweetNaclFast.is_on_curve(hash) != 0) {
                throw new IllegalStateException("Invalid seeds, address must fall off the curve");
            }

            return new PublicKey(hash);
        } catch (IOException e) {
            throw new RuntimeException("Error creating program address", e);
        }
    }

    public static PublicKey createWithSeed(PublicKey fromPublicKey, String seed, PublicKey programId) {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            if (seed.length() > 32) {
                throw new IllegalArgumentException("Max seed length exceeded: " + seed.length());
            }

            buffer.write(fromPublicKey.toByteArray());
            buffer.write(seed.getBytes());
            buffer.write(programId.toByteArray());

            byte[] hash = sha256Hash(buffer.toByteArray());
            return new PublicKey(hash);
        } catch (IOException e) {
            throw new RuntimeException("Error creating program address", e);
        }
    }

    public static class ProgramDerivedAddress {
        private PublicKey address;
        private int nonce;

        public ProgramDerivedAddress(PublicKey address, int nonce) {
            this.address = address;
            this.nonce = nonce;
        }

        public PublicKey getAddress() {
            return address;
        }

        public int getNonce() {
            return nonce;
        }

    }

    public static ProgramDerivedAddress findProgramAddress(List<byte[]> seeds, PublicKey programId) {
        for (int nonce = 255; nonce >= 0; nonce--) {
            try {
                List<byte[]> seedsWithNonce = new ArrayList<>(seeds);
                seedsWithNonce.add(new byte[] { (byte) nonce });
                PublicKey address = createProgramAddress(seedsWithNonce, programId);
                return new ProgramDerivedAddress(address, nonce);
            } catch (IllegalStateException e) {
                // Address was on the curve, try next nonce
            }
        }
        throw new IllegalStateException("Unable to find a viable program address nonce");
    }



    public static PublicKey valueOf(String publicKey) {
        return new PublicKey(publicKey);
    }

    /**
     * Computes SHA-256 hash of the given input.
     *
     * @param input the input bytes to hash
     * @return the SHA-256 hash
     */
    private static byte[] sha256Hash(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

}
