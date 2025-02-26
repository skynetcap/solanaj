package org.p2p.solanaj.programs;

import org.p2p.solanaj.core.AccountMeta;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory class for creating Address Lookup Table program instructions.
 */
public class AddressLookupTableProgram extends Program {

    /** The program ID for the Address Lookup Table program */
    public static final PublicKey PROGRAM_ID = new PublicKey("AddressLookupTab1e1111111111111111111111111");

    private static final byte CREATE_LOOKUP_TABLE = 0;
    private static final byte FREEZE_LOOKUP_TABLE = 1;
    private static final byte EXTEND_LOOKUP_TABLE = 2;
    private static final byte DEACTIVATE_LOOKUP_TABLE = 3;
    private static final byte CLOSE_LOOKUP_TABLE = 4;

    /**
     * Creates an instruction to create a new address lookup table.
     *
     * @param authority The authority (signer) that can modify the table
     * @param payer The account paying for the table creation
     * @param recentSlot A recent slot to derive the table's address
     * @return A TransactionInstruction to create a new address lookup table
     */
    public static TransactionInstruction createLookupTable(PublicKey authority, PublicKey payer, long recentSlot) {
        PublicKey.ProgramDerivedAddress derivedAddress = PublicKey.findProgramAddress(
            List.of(authority.toByteArray(), ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(recentSlot).array()),
            PROGRAM_ID
        );

        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(derivedAddress.getAddress(), false, true));
        keys.add(new AccountMeta(authority, true, false));
        keys.add(new AccountMeta(payer, true, true));
        keys.add(new AccountMeta(SystemProgram.PROGRAM_ID, false, false));

        ByteBuffer data = ByteBuffer.allocate(4 + 8 + 1);
        data.order(ByteOrder.LITTLE_ENDIAN);
        data.putInt(CREATE_LOOKUP_TABLE);
        data.putLong(recentSlot);
        data.put((byte) derivedAddress.getNonce());

        return createTransactionInstruction(PROGRAM_ID, keys, data.array());
    }

    /**
     * Creates an instruction to freeze an address lookup table.
     *
     * @param lookupTable The address of the lookup table to freeze
     * @param authority The authority (signer) of the lookup table
     * @return A TransactionInstruction to freeze an address lookup table
     */
    public static TransactionInstruction freezeLookupTable(PublicKey lookupTable, PublicKey authority) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(lookupTable, false, true));
        keys.add(new AccountMeta(authority, true, false));

        ByteBuffer data = ByteBuffer.allocate(1);
        data.put(FREEZE_LOOKUP_TABLE);

        return createTransactionInstruction(PROGRAM_ID, keys, data.array());
    }

    /**
     * Creates an instruction to extend an address lookup table.
     *
     * @param lookupTable The address of the lookup table to extend
     * @param authority The authority (signer) of the lookup table
     * @param payer The account paying for the table extension
     * @param addresses The list of addresses to add to the table
     * @return A TransactionInstruction to extend an address lookup table
     */
    public static TransactionInstruction extendLookupTable(PublicKey lookupTable, PublicKey authority, PublicKey payer, List<PublicKey> addresses) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(lookupTable, false, true));
        keys.add(new AccountMeta(authority, true, false));
        keys.add(new AccountMeta(payer, true, true));
        keys.add(new AccountMeta(SystemProgram.PROGRAM_ID, false, false));

        ByteBuffer data = ByteBuffer.allocate(1 + 4 + addresses.size() * 32);
        data.order(ByteOrder.LITTLE_ENDIAN);
        data.put(EXTEND_LOOKUP_TABLE);
        data.putInt(addresses.size());
        for (PublicKey address : addresses) {
            data.put(address.toByteArray());
        }

        return createTransactionInstruction(PROGRAM_ID, keys, data.array());
    }

    /**
     * Creates an instruction to deactivate an address lookup table.
     *
     * @param lookupTable The address of the lookup table to deactivate
     * @param authority The authority (signer) of the lookup table
     * @return A TransactionInstruction to deactivate an address lookup table
     */
    public static TransactionInstruction deactivateLookupTable(PublicKey lookupTable, PublicKey authority) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(lookupTable, false, true));
        keys.add(new AccountMeta(authority, true, false));

        ByteBuffer data = ByteBuffer.allocate(1);
        data.put(DEACTIVATE_LOOKUP_TABLE);

        return createTransactionInstruction(PROGRAM_ID, keys, data.array());
    }

    /**
     * Creates an instruction to close an address lookup table.
     *
     * @param lookupTable The address of the lookup table to close
     * @param authority The authority (signer) of the lookup table
     * @param recipient The account to receive the closed table's lamports
     * @return A TransactionInstruction to close an address lookup table
     */
    public static TransactionInstruction closeLookupTable(PublicKey lookupTable, PublicKey authority, PublicKey recipient) {
        List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(lookupTable, false, true));
        keys.add(new AccountMeta(authority, true, false));
        keys.add(new AccountMeta(recipient, false, true));

        ByteBuffer data = ByteBuffer.allocate(1);
        data.put(CLOSE_LOOKUP_TABLE);

        return createTransactionInstruction(PROGRAM_ID, keys, data.array());
    }
}
