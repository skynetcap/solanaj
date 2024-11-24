package org.p2p.solanaj.utils.bip32.wallet.key;

import lombok.Getter;
import lombok.Setter;
import org.p2p.solanaj.utils.bip32.crypto.Hash;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Marshalling code for HDKeys to base58 representations.
 * <p>
 * Will probably be migrated to builder pattern.
 */
@Setter
public class HdKey {
    @Getter
    private byte[] version;
    @Getter
    private int depth;
    private byte[] fingerprint;
    private byte[] childNumber;
    @Getter
    private byte[] chainCode;
    @Getter
    private byte[] keyData;

    HdKey(byte[] version, int depth, byte[] fingerprint, byte[] childNumber, byte[] chainCode, byte[] keyData) {
        this.version = version;
        this.depth = depth;
        this.fingerprint = fingerprint;
        this.childNumber = childNumber;
        this.chainCode = chainCode;
        this.keyData = keyData;
    }

    HdKey() {
    }

    /**
     * Get the full chain key.  This is not the public/private key for the address.
     * @return full HD Key
     */
    public byte[] getKey() {

        ByteArrayOutputStream key = new ByteArrayOutputStream();

        try {
            key.write(version);
            key.write(new byte[]{(byte) depth});
            key.write(fingerprint);
            key.write(childNumber);
            key.write(chainCode);
            key.write(keyData);
            byte[] checksum = Hash.sha256Twice(key.toByteArray());
            key.write(Arrays.copyOfRange(checksum, 0, 4));
        } catch (IOException e) {
            throw new RuntimeException("Unable to write key");
        }

        return key.toByteArray();
    }

}
