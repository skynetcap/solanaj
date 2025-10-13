package org.p2p.solanaj.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.logging.Logger;

import org.p2p.solanaj.utils.Base58;

public class AccountTest {

    @Test
    public void accountFromSecretKey() {
        byte[] secretKey = Base58
                .decode("4Z7cXSyeFR8wNGMVXUE1TwtKn5D5Vu7FzEv69dokLv7KrQk7h6pu4LF8ZRR9yQBhc7uSM6RTTZtU1fmaxiNrxXrs");
        assertEquals("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo", new Account(secretKey).getPublicKey().toString());

        assertEquals(64, new Account(secretKey).getSecretKey().length);
    }

    @Test
    public void generateNewAccount() {
        Account account = new Account();
        assertEquals(64, account.getSecretKey().length);
    }

    @Test
    public void fromBip44Mnemonic() {
        Account acc = Account.fromBip44Mnemonic(Arrays.asList("hint", "begin", "crowd", "dolphin", "drive", "render",
                "finger", "above", "sponsor", "prize", "runway", "invest", "dizzy", "pony", "bitter", "trial", "ignore",
                "crop", "please", "industry", "hockey", "wire", "use", "side"), "");

        assertEquals("G75kGJiizyFNdnvvHxkrBrcwLomGJT2CigdXnsYzrFHv", acc.getPublicKey().toString());
    }

    @Test
    public void fromBip44MnemonicChange() {
        Account acc = Account.fromBip44MnemonicWithChange(Arrays.asList("hint", "begin", "crowd", "dolphin", "drive", "render",
                "finger", "above", "sponsor", "prize", "runway", "invest", "dizzy", "pony", "bitter", "trial", "ignore",
                "crop", "please", "industry", "hockey", "wire", "use", "side"), "");

        assertEquals("AaXs7cLGcSVAsEt8QxstVrqhLhYN2iGhFNRemwYnHitV", acc.getPublicKey().toString());
    }

    @Test
    public void fromMnemonic() {
        Account acc = Account.fromMnemonic(Arrays.asList("spider", "federal", "bleak", "unable", "ask", "weasel",
                "diamond", "electric", "illness", "wheat", "uphold", "mind"), "");

        assertEquals("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo", acc.getPublicKey().toString());
    }

    @Test
    public void fromJson() {
        String json = "[94,151,102,217,69,77,121,169,76,7,9,241,196,119,233,67,25,222,209,40,113,70,33,81,154,33,136,30,208,45,227,28,23,245,32,61,13,33,156,192,84,169,95,202,37,105,150,21,157,105,107,130,13,134,235,7,16,130,50,239,93,206,244,0]";
        Account acc = Account.fromJson(json);

        assertEquals("2cXAj2TagK3t6rb2CGRwyhF6sTFJgLyzyDGSWBcGd8Go", acc.getPublicKey().toString());
    }

    @Test
    public void fromBip39MnemonicTest() {
        Account account = Account.fromBip39Mnemonic(
                Arrays.asList("iron", "make", "indoor", "where", "explain", "model", "maximum", "wonder",
                        "toward", "salad", "fan",  "try"),
                ""
        );

        Logger.getAnonymousLogger().info("Derived pubkey = " + account.getPublicKey().toBase58());
        assertEquals("BeepMww3KwiDeEhEeZmqk4TegvJYNuDERPWm142X6Mx3", account.getPublicKey().toBase58());
    }

    @Test
    public void testAccountEquality() {
        byte[] secretKey = Base58.decode("4Z7cXSyeFR8wNGMVXUE1TwtKn5D5Vu7FzEv69dokLv7KrQk7h6pu4LF8ZRR9yQBhc7uSM6RTTZtU1fmaxiNrxXrs");
        Account account1 = new Account(secretKey);
        Account account2 = new Account(secretKey);
        Account account3 = new Account();

        assertEquals(account1.getPublicKey(), account2.getPublicKey());
        assertNotEquals(account1.getPublicKey(), account3.getPublicKey());
    }

    @Test
    public void testInvalidSecretKeyLength() {
        byte[] invalidSecretKey = new byte[63]; // Invalid length
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> new Account(invalidSecretKey));
    }

    @Test
    public void testFromBip44MnemonicWithPassphrase() {
        Account acc = Account.fromBip44Mnemonic(
            Arrays.asList("hint", "begin", "crowd", "dolphin", "drive", "render",
                "finger", "above", "sponsor", "prize", "runway", "invest", "dizzy", "pony", "bitter", "trial", "ignore",
                "crop", "please", "industry", "hockey", "wire", "use", "side"),
            "passphrase123"
        );

        assertNotNull(acc);
        assertNotEquals("G75kGJiizyFNdnvvHxkrBrcwLomGJT2CigdXnsYzrFHv", acc.getPublicKey().toString());
    }

}
