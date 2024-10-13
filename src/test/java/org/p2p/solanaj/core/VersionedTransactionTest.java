package org.p2p.solanaj.core;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Base58;
import org.junit.jupiter.api.Test;
import org.p2p.solanaj.programs.SystemProgram;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Test class for VersionedTransaction.
 * Verifies the creation, signing, and serialization of versioned transactions,
 * including support for Address Lookup Tables.
 */
@Slf4j
public class VersionedTransactionTest {

    private final static Account signer = new Account();
    /**
     * Tests the creation of a VersionedTransaction with no Address Lookup Tables.
     */
    @Test
    public void testCreateVersionedTransaction() {
        VersionedMessage message = new VersionedMessage();
        message.setRecentBlockhash("Eit7RCyhUixAe2hGBS8oqnw59QK3kgMMjfLME5bm9wRn");

        TransactionInstruction instruction = new TransactionInstruction(
                SystemProgram.PROGRAM_ID,
                Arrays.asList(
                        new AccountMeta(new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo"), true, true),
                        new AccountMeta(new PublicKey("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5"), false, true)
                ),
                SystemProgram.transfer(
                        new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo"),
                        new PublicKey("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5"),
                        1000
                ).getData()
        );

        message.addInstruction(instruction);

        VersionedTransaction vt = new VersionedTransaction(message);
        vt.sign(Collections.singletonList(signer));

        byte[] serialized = vt.serialize();

        assertNotNull(serialized);
        assertTrue(serialized.length > 0);
    }

    /**
     * Tests the creation of a VersionedTransaction with an Address Lookup Table.
     */
    @Test
    public void testCreateVersionedTransactionWithLookupTable() {
        VersionedMessage message = new VersionedMessage();
        message.setRecentBlockhash("Eit7RCyhUixAe2hGBS8oqnw59QK3kgMMjfLME5bm9wRn");

        // Create a dummy Address Lookup Table
        AddressTableLookup lookupTable = new AddressTableLookup(
                new PublicKey("AddressLookupTab1e1111111111111111111111111"),
                Arrays.asList(0, 1), // Writable indexes
                Arrays.asList(2, 3)  // Readonly indexes
        );

        message.addAddressTableLookup(lookupTable);

        // Add instructions as needed...
        TransactionInstruction instruction = new TransactionInstruction(
                SystemProgram.PROGRAM_ID,
                Arrays.asList(
                        new AccountMeta(new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo"), true, true),
                        new AccountMeta(new PublicKey("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5"), false, true)
                ),
                SystemProgram.transfer(
                        new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo"),
                        new PublicKey("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5"),
                        2000
                ).getData()
        );

        message.addInstruction(instruction);

        VersionedTransaction vt = new VersionedTransaction(message);
        vt.sign(Collections.singletonList(signer));

        byte[] serialized = vt.serialize();

        assertNotNull(serialized);
        assertTrue(serialized.length > 0);
    }

    /**
     * Tests adding multiple Address Lookup Tables to a VersionedTransaction.
     */
    @Test
    public void testAddMultipleAddressLookupTables() {
        VersionedMessage message = new VersionedMessage();
        message.setRecentBlockhash("Eit7RCyhUixAe2hGBS8oqnw59QK3kgMMjfLME5bm9wRn");

        AddressTableLookup lookupTable1 = new AddressTableLookup(
                new PublicKey("AddressLookupTab1e1111111111111111111111111"),
                Arrays.asList(0, 1),
                Arrays.asList(2, 3)
        );

        AddressTableLookup lookupTable2 = new AddressTableLookup(
                new PublicKey("AddressLookupTab2e2222222222222222222222222"),
                Arrays.asList(4, 5),
                Arrays.asList(6, 7)
        );

        message.addAddressTableLookup(lookupTable1);
        message.addAddressTableLookup(lookupTable2);

        // Add instructions as needed...
        TransactionInstruction instruction = new TransactionInstruction(
                SystemProgram.PROGRAM_ID,
                Arrays.asList(
                        new AccountMeta(new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo"), true, true),
                        new AccountMeta(new PublicKey("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5"), false, true)
                ),
                SystemProgram.transfer(
                        new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo"),
                        new PublicKey("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5"),
                        3000
                ).getData()
        );

        message.addInstruction(instruction);

        VersionedTransaction vt = new VersionedTransaction(message);
        vt.sign(Collections.singletonList(signer));

        byte[] serialized = vt.serialize();

        assertNotNull(serialized);
        assertTrue(serialized.length > 0);
    }

    @Test
    public void testRpcClientVersioned() throws RpcException {
        RpcClient client = new RpcClient(Cluster.MAINNET);

        VersionedMessage message = new VersionedMessage();
        message.setRecentBlockhash("Eit7RCyhUixAe2hGBS8oqnw59QK3kgMMjfLME5bm9wRn");

        TransactionInstruction instruction = new TransactionInstruction(
                SystemProgram.PROGRAM_ID,
                Arrays.asList(
                        new AccountMeta(new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo"), true, true),
                        new AccountMeta(new PublicKey("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5"), false, true)
                ),
                SystemProgram.transfer(
                        new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo"),
                        new PublicKey("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5"),
                        1000
                ).getData()
        );

        message.addInstruction(instruction);

        AddressTableLookup addressTableLookup = new AddressTableLookup(
                new PublicKey("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5"),
                List.of(0),
                List.of(0)
        );

        VersionedTransaction vt = new VersionedTransaction(message);
        vt.addAddressTableLookup(addressTableLookup);
        vt.sign(Collections.singletonList(signer));

        String txId = client.getApi().sendVersionedTransaction(vt);

        log.info("Tx: {}", txId);
    }
}