package org.p2p.solanaj.programs.anchor;

import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.AccountMeta;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.programs.Program;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implements the "initialize" call from Anchor's basic-0 tutorial.
 */
public class AnchorBasicTutorialProgram extends Program {

    // Testnet address of basic-0 = EkEwddr34fqnv2SJREPynyC335PE32PAfjY4LVW5bTJS (has a method called initialize)
    private static final PublicKey PROGRAM_ID = new PublicKey("EkEwddr34fqnv2SJREPynyC335PE32PAfjY4LVW5bTJS");
    private static final String FUNCTION_NAMESPACE = "global::initialize";

    /**
     * Calls basic_0::initialize
     *
     * @param caller account signing the transaction
     * @return tx id
     */
    public static TransactionInstruction initialize(Account caller) {
        final List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(caller.getPublicKey(),true, false));

        byte[] transactionData = encodeInitializeData();

        return createTransactionInstruction(
                PROGRAM_ID,
                keys,
                transactionData
        );
    }

    /**
     * Encodes the "global::initialize" sighash
     * @return byte array containing sighash for "global::initialize"
     */
    private static byte[] encodeInitializeData() {
        MessageDigest digest;
        byte[] encodedHash = null;
        int sigHashStart = 0;
        int sigHashEnd = 8;

        try {
            digest = MessageDigest.getInstance("SHA-256");
            encodedHash = Arrays.copyOfRange(
                    digest.digest(
                            FUNCTION_NAMESPACE.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    ),
                    sigHashStart,
                    sigHashEnd
            );
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return encodedHash;
    }


}
