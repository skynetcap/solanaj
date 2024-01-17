package org.p2p.solanaj.programs;

import lombok.extern.slf4j.Slf4j;
import org.p2p.solanaj.core.AccountMeta;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AssociatedTokenProgram extends Program {

    public static final PublicKey PROGRAM_ID = new PublicKey("ATokenGPvbdGVxr1b2hvZbsiqW5xWH25efTNsLJA8knL");

    private static final int CREATE_IDEMPOTENT_METHOD_ID = 1;

    public static TransactionInstruction createIdempotent(PublicKey fundingAccount,
                                                          PublicKey walletAddress,
                                                          PublicKey mint) {
        final List<AccountMeta> keys = new ArrayList<>();

        // ATA pda
        PublicKey pda = null;
        try {
            pda = PublicKey.findProgramAddress(
                    List.of(
                            walletAddress.toByteArray(),
                            TokenProgram.PROGRAM_ID.toByteArray(),
                            mint.toByteArray()
                    ),
                    PROGRAM_ID
            ).getAddress();
            log.info("ATA: {}", pda.toBase58());
        } catch (Exception e) {
            log.error("Error finding ATA: {}", e.getMessage());
        }

        keys.add(new AccountMeta(fundingAccount, true, true));
        keys.add(new AccountMeta(pda, false, true));
        keys.add(new AccountMeta(fundingAccount, false, false));
        keys.add(new AccountMeta(mint, false, false));
        keys.add(new AccountMeta(SystemProgram.PROGRAM_ID, false, false));
        keys.add(new AccountMeta(TokenProgram.PROGRAM_ID, false, false));

        byte[] transactionData = encodeTransferTokenInstructionData();

        return createTransactionInstruction(
                PROGRAM_ID,
                keys,
                transactionData
        );
    }

    private static byte[] encodeTransferTokenInstructionData() {
        ByteBuffer result = ByteBuffer.allocate(1);
        result.order(ByteOrder.LITTLE_ENDIAN);
        result.put((byte) CREATE_IDEMPOTENT_METHOD_ID);

        return result.array();
    }

}
