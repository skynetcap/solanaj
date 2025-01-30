package org.p2p.solanaj.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import static org.junit.jupiter.api.Assertions.*;

import org.p2p.solanaj.programs.MemoProgram;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.*;
import org.p2p.solanaj.rpc.types.TokenResultObjects.*;
import org.p2p.solanaj.rpc.types.config.Commitment;
import org.p2p.solanaj.token.TokenManager;

import java.util.*;

public class MainnetTest extends AccountBasedTest {

    private final RpcClient client = new RpcClient(Cluster.MAINNET);
    public final TokenManager tokenManager = new TokenManager(client);

    private static final PublicKey USDC_TOKEN_MINT = new PublicKey("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v");
    private static final long LAMPORTS_PER_SOL = 1000000000L;


    @BeforeEach
    public void beforeMethod() throws InterruptedException {
        // Prevent RPCPool rate limit
        Thread.sleep(200L);
    }

    @Test
    public void getAccountInfoBase64() throws RpcException {
        // Get account Info
        final AccountInfo accountInfo = client.getApi().getAccountInfo(PublicKey.valueOf("So11111111111111111111111111111111111111112"));
        final double balance = (double) accountInfo.getValue().getLamports() / LAMPORTS_PER_SOL;

        // Account data list
        final List<String> accountData = accountInfo.getValue().getData();

        // Verify "base64" string in accountData
        assertTrue(accountData.stream().anyMatch(s -> s.equalsIgnoreCase("base64")));
        assertTrue(balance > 0);
    }

    @Test
    public void getAccountInfoBase58() throws RpcException {
        // Get account Info
        final AccountInfo accountInfo = client.getApi().getAccountInfo(PublicKey.valueOf("So11111111111111111111111111111111111111112"), Map.of("encoding", "base58"));
        final double balance = (double) accountInfo.getValue().getLamports() / LAMPORTS_PER_SOL;

        // Account data list
        final List<String> accountData = accountInfo.getValue().getData();

        // Verify "base64" string in accountData
        assertTrue(accountData.stream().anyMatch(s -> s.equalsIgnoreCase("base58")));
        assertTrue(balance > 0);
    }

    @Test
    public void getAccountInfoRootCommitment() {
        try {
            // Get account Info
            final AccountInfo accountInfo = client.getApi().getAccountInfo(PublicKey.valueOf(
                    "So11111111111111111111111111111111111111112"), Map.of("commitment", Commitment.ROOT));
            final double balance = (double) accountInfo.getValue().getLamports() / LAMPORTS_PER_SOL;
            LOGGER.info("balance = " + balance);
            // Verify any balance
            assertTrue(balance > 0);
        } catch (RpcException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getAccountInfoJsonParsed() {
        try {
            final SplTokenAccountInfo accountInfo = client.getApi().getSplTokenAccountInfo(
                    PublicKey.valueOf("8tnpAECxAT9nHBqR1Ba494Ar5dQMPGhL31MmPJz1zZvY")
            );

            assertTrue(
                    accountInfo.getValue().getData().getProgram().equalsIgnoreCase("spl-token")
            );

        } catch (RpcException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPdaStakeAccountDerive() throws RpcException {
        PublicKey miner = new PublicKey("mineXqpDeBeMR8bPQCyy9UneJZbjFywraS3koWZ8SSH");
        PublicKey programId = new PublicKey("J6XAzG8S5KmoBM8GcCFfF8NmtzD7U3QPnbhNiYwsu9we");
        PublicKey boostProgramId = new PublicKey("boostmPwypNUQu8qZ8RoWt5DXyYSVYxnBXqbbrGjecc");
        PublicKey staker = new PublicKey("skynetDj29GH6o6bAqoixCpDuYtWqi1rm8ZNx1hB3vq");
        PublicKey oreSolMeteoraLpTokenMint = new PublicKey("DrSS5RM7zUd9qjUEdDaf31vnDUSbCrMto6mjqTrHFifN");

        // Managed Proof Address
        var managedProofAddress = PublicKey.findProgramAddress(
            List.of(
                "managed-proof-account".getBytes(),
                miner.toByteArray()
            ),
            programId
        );

        var delegatedBoostAddress = PublicKey.findProgramAddress(
            List.of(
                "v2-delegated-boost".getBytes(),
                staker.toByteArray(),
                oreSolMeteoraLpTokenMint.toByteArray(),
                managedProofAddress.getAddress().toByteArray()
            ),
            programId
        );

        var boostPda = PublicKey.findProgramAddress(
            List.of(
                "boost".getBytes(),
                oreSolMeteoraLpTokenMint.toByteArray()
            ),
            boostProgramId
        );

        var stakePda = PublicKey.findProgramAddress(
            List.of(
                "stake".getBytes(),
                managedProofAddress.getAddress().toByteArray(),
                boostPda.getAddress().toByteArray()
            ),
            boostProgramId
        );

        LOGGER.info("managedProofAddress: " + managedProofAddress.getAddress());
        LOGGER.info("delegatedBoostAddress: " + delegatedBoostAddress.getAddress());
        LOGGER.info("boostPda: " + boostPda.getAddress());
        LOGGER.info("stakePda: " + stakePda.getAddress());

        // Deserialize delegatedBoostAddress
        byte[] delegatedBoostAddressData = client.getApi().getAccountInfo(delegatedBoostAddress.getAddress()).getDecodedData();
        LOGGER.info("delegatedBoostAddressData: " + Arrays.toString(delegatedBoostAddressData));
    }

    @Test
    public void getToken2022AccountInfoLegacyDto() throws RpcException {
        SplTokenAccountInfo token2022AccountInfo = client.getApi().getSplTokenAccountInfo(PublicKey.valueOf("HeLp6NuQkmYB4pYWo2zYs22mESHXPQYzXbB8n4V98jwC"));
        LOGGER.info(token2022AccountInfo.toString());

        int decimals = token2022AccountInfo.getValue().getData().getParsed().getInfo().getDecimals();
        LOGGER.info("decimals: " + decimals);

        assertEquals(9, decimals);

        Optional<Extension> tokenExtension = token2022AccountInfo.getValue().getData().getParsed().getInfo().getExtensions().stream()
            .filter(e -> e.getExtensionType().equals("tokenMetadata"))
            .findAny();

        assertTrue(tokenExtension.isPresent());

        Extension extension = tokenExtension.get();

        LOGGER.info("name: " + extension.getState().getName());
        assertEquals("ai16z", extension.getState().getName());
    }

    @Test
    public void getToken2022AccountInfo() throws RpcException {
        SplTokenAccountInfo token2022AccountInfo = client.getApi().getSplTokenAccountInfo(PublicKey.valueOf("HeLp6NuQkmYB4pYWo2zYs22mESHXPQYzXbB8n4V98jwC"));
        LOGGER.info(token2022AccountInfo.toString());

        int decimals = token2022AccountInfo.getValue().getData().getParsed().getInfo().getDecimals();
        LOGGER.info("decimals: " + decimals);
        assertEquals(9, decimals);

        Optional<Extension> tokenExtension = token2022AccountInfo.getValue().getData().getParsed().getInfo().getExtensions().stream()
            .filter(e -> e.getExtensionType().equals("tokenMetadata"))
            .findAny();
        assertTrue(tokenExtension.isPresent());

        Extension extension = tokenExtension.get();
        LOGGER.info("name: " + extension.getState().getName());
        assertEquals("ai16z", extension.getState().getName());

        Extension tokenMetadataExtension = token2022AccountInfo.getExtension("tokenMetadata").get();
        LOGGER.info("tokenMetadataExtension: " + tokenMetadataExtension.toString());

        Optional<ExtensionState> extensionState = token2022AccountInfo.getToken2022Metadata();
        assertTrue(extensionState.isPresent());

        ExtensionState extensionStateFinal = extensionState.get();
        LOGGER.info("name: " + extensionStateFinal.getName());
        assertEquals("ai16z", extensionStateFinal.getName());
    }

    @Test
    public void getToken2022Metadata() throws RpcException {
        SplTokenAccountInfo token2022AccountInfo = client.getApi().getSplTokenAccountInfo(PublicKey.valueOf("HeLp6NuQkmYB4pYWo2zYs22mESHXPQYzXbB8n4V98jwC"));
        LOGGER.info(token2022AccountInfo.toString());

        Optional<String> name = token2022AccountInfo.getTokenName();
        assertTrue(name.isPresent());
        assertEquals("ai16z", name.get());

        Optional<String> symbol = token2022AccountInfo.getTokenSymbol();
        assertTrue(symbol.isPresent());
        assertEquals("ai16z", symbol.get());    

        Optional<String> uri = token2022AccountInfo.getTokenUri();
        assertTrue(uri.isPresent());
        assertEquals("https://ipfs.io/ipfs/bafkreigaf4mmibkmjmz4mn4opsvzbcp74k2edldui2hxtecoflaltog7x4", uri.get());
    }

    /**
     * Calls sendLegacyTransaction with a call to the Memo program included.
     */
    @Test
    @Disabled
    public void transactionMemoTest() {
        final int lamports = 1111;
        final PublicKey destination = solDestination;

        // Create account from private key
        final Account feePayer = testAccount;

        final LegacyTransaction legacyTransaction = new LegacyTransaction();

        // First intruction it adds here is a small amount of SOL (like 0.000001) just to have some content in the tx
        // Probably not really needed
//        transaction.addInstruction(
//                SystemProgram.transfer(
//                        feePayer.getPublicKey(),
//                        destination,
//                        lamports
//                )
//        );

        // Add instruction to write memo
        legacyTransaction.addInstruction(
                MemoProgram.writeUtf8(feePayer.getPublicKey(), "Twitter: skynetcap")
        );

        // Call sendLegacyTransaction
        String result = null;
        try {
            result = client.getApi().sendLegacyTransaction(legacyTransaction, feePayer);
            LOGGER.info("Result = " + result);
        } catch (RpcException e) {
            e.printStackTrace();
        }

        assertNotNull(result);
    }

    @Test
    public void getBlockCommitmentTest() {
        // Block 5 used for testing - matches docs
        long block = 5;

        try {
            final BlockCommitment blockCommitment = client.getApi().getBlockCommitment(block);

            LOGGER.info(String.format("block = %d, totalStake = %d", block, blockCommitment.getTotalStake()));

            assertNotNull(blockCommitment);
            assertTrue(blockCommitment.getTotalStake() > 0);
        } catch (RpcException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getBlockHeightTest() {
        try {
            long blockHeight = client.getApi().getBlockHeight();
            LOGGER.info(String.format("Block height = %d", blockHeight));
            assertTrue(blockHeight > 0);
        } catch (RpcException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getBlockProductionTest() throws RpcException {
        BlockProduction blockProduction = client.getApi().getBlockProduction();
        LOGGER.info(String.format("Block height = %s", blockProduction.getValue()));
        assertNotNull(blockProduction);
    }

    @Test
    public void minimumLedgerSlotTest() throws RpcException {
        long minimumLedgerSlot = client.getApi().minimumLedgerSlot();
        LOGGER.info(String.format("minimumLedgerSlot = %d", minimumLedgerSlot));
        assertTrue(minimumLedgerSlot > 0);
    }

    @Test
    public void getVersionTest() throws RpcException {
        SolanaVersion version = client.getApi().getVersion();
        LOGGER.info(
                String.format(
                        "solana-core: %s, feature-set: %s",
                        version.getSolanaCore(),
                        version.getFeatureSet()
                )
        );
        assertNotNull(version);
        assertNotNull(version.getSolanaCore());
    }


    @Test
    public void getClusterNodesTest() {
        try {
            final List<ClusterNode> clusterNodes = client.getApi().getClusterNodes();

            // Make sure we got some nodes
            assertNotNull(clusterNodes);
            assertTrue(clusterNodes.size() > 0);

            // Output the nodes
            LOGGER.info("Cluster Nodes:");
            clusterNodes.forEach(clusterNode -> {
                LOGGER.info(clusterNode.toString());
            });
        } catch (RpcException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getEpochInfoTest() throws RpcException {
        final EpochInfo epochInfo = client.getApi().getEpochInfo();
        assertNotNull(epochInfo);

        LOGGER.info(epochInfo.toString());

        // Validate the returned data
        assertTrue(epochInfo.getAbsoluteSlot() > 0);
        assertTrue(epochInfo.getEpoch() > 0);
        assertTrue(epochInfo.getSlotsInEpoch() > 0);
        assertTrue(epochInfo.getBlockHeight() > 0);
        assertTrue(epochInfo.getSlotIndex() > 0);
    }

    @Test
    public void getHighestSnapshotSlotTest() throws RpcException {
        final SnapshotSlot highestSnapshotSlot = client.getApi().getHighestSnapshotSlot();
        assertNotNull(highestSnapshotSlot);

        LOGGER.info(String.format("Highest full snapshot slot: %d", highestSnapshotSlot.getFullSnapshotSlot()));
        assertTrue(highestSnapshotSlot.getFullSnapshotSlot() > 0);
    }

    @Test
    public void getEpochScheduleTest() throws RpcException {
        final EpochSchedule epochSchedule = client.getApi().getEpochSchedule();
        assertNotNull(epochSchedule);

        LOGGER.info(epochSchedule.toString());

        // Validate the returned data
        assertTrue(epochSchedule.getSlotsPerEpoch() > 0);
        assertTrue(epochSchedule.getLeaderScheduleSlotOffset() > 0);
    }

    @Test
    public void getInflationRateTest() throws RpcException {
        InflationRate inflationRate = client.getApi().getInflationRate();
        LOGGER.info(inflationRate.toString());

        //validate the returned data
        assertNotNull(inflationRate);
        assertTrue(inflationRate.getEpoch() > 0);
        assertTrue(inflationRate.getFoundation() >= 0);
        assertTrue(inflationRate.getValidator() >= 0);
        assertTrue(inflationRate.getTotal() >= 0);
        assertEquals(inflationRate.getTotal(), inflationRate.getFoundation() + inflationRate.getValidator(), 0.0);
    }

    @Test
    public void getInflationGovernorTest() throws RpcException {
        InflationGovernor inflationGovernor = client.getApi().getInflationGovernor();
        LOGGER.info(inflationGovernor.toString());

        //validate the returned data
        assertNotNull(inflationGovernor);
        assertTrue(inflationGovernor.getInitial() > 0);
        assertTrue(inflationGovernor.getTerminal() > 0);
        assertTrue(inflationGovernor.getTaper() > 0);
        assertTrue(inflationGovernor.getFoundation() >= 0);
        assertTrue(inflationGovernor.getFoundationTerm() >= 0);
    }

    @Test
    @Disabled
    public void getInflationRewardTest() throws RpcException {
        List<InflationReward> inflationRewards = client.getApi().getInflationReward(
                Arrays.asList(
                        PublicKey.valueOf("H8VT3V6EDiYiQqmeDgqZJf4Tt76Qe6WZjPhighAGPL5T"),
                        PublicKey.valueOf("BsXUTPFf5b82ptLGfDVXhAPmGk1ZwTirWA2aQrBq4zBW")
                ),
                300L,
                null);

        LOGGER.info(inflationRewards.toString());

        //validate the returned data
        assertNotNull(inflationRewards);
        assertEquals(1, inflationRewards.size());
        for (InflationReward inflationReward : inflationRewards) {
            assertEquals(300L, inflationReward.getEpoch(), 0);
            assertTrue(inflationReward.getAmount() > 0);
            assertTrue(inflationReward.getEffectiveSlot() > 0);
            assertTrue(inflationReward.getPostBalance() > 0);
        }
    }

    @Test
    public void getSlotTest() throws RpcException {
        long slot = client.getApi().getSlot();
        LOGGER.info(String.format("Current slot = %d", slot));
        assertTrue(slot > 0);
    }

    @Test
    public void getSlotLeaderTest() throws RpcException {
        PublicKey slotLeader = client.getApi().getSlotLeader();
        LOGGER.info(String.format("Current slot leader = %s", slotLeader));
        assertNotNull(slotLeader);
    }

    @Test
    public void getSlotLeadersTest() throws RpcException {
        long limit = 5;
        long currentSlot = client.getApi().getSlot();
        List<PublicKey> slotLeaders = client.getApi().getSlotLeaders(currentSlot, limit);
        slotLeaders.forEach(slotLeader ->
                LOGGER.info(slotLeader.toString())
        );

        assertNotNull(slotLeaders);
        assertEquals(limit, slotLeaders.size());
    }

    @Test
    @Disabled
    public void getSnapshotSlotTest() throws RpcException {
        long snapshotSlot = client.getApi().getSnapshotSlot();
        LOGGER.info(String.format("Snapshot slot = %d", snapshotSlot));
        assertTrue(snapshotSlot > 0);
    }

    @Test
    public void getMaxShredInsertSlotTest() throws RpcException {
        long maxShredInsertSlot = client.getApi().getMaxShredInsertSlot();
        LOGGER.info(String.format("Max slot after shred insert = %d", maxShredInsertSlot));
        assertTrue(maxShredInsertSlot > 0);
    }

    @Test
    public void getIdentityTest() throws RpcException {
        PublicKey identity = client.getApi().getIdentity();
        LOGGER.info(String.format("Identity of the current node = %s", identity));
        assertNotNull(identity);
    }

    @Test
    @Disabled
    public void getSupplyTest() throws RpcException {
        Supply supply = client.getApi().getSupply();
        LOGGER.info(supply.toString());

        //validate the returned data
        assertNotNull(supply);
        assertTrue(supply.getValue().getTotal() > 0);
        assertTrue(supply.getValue().getCirculating() > 0);
        assertTrue(supply.getValue().getNonCirculating() > 0);
        assertEquals(supply.getValue().getTotal(), supply.getValue().getCirculating() + supply.getValue().getNonCirculating());
        assertTrue(supply.getValue().getNonCirculatingAccounts().size() > 0);
    }

    @Test
    public void getFirstAvailableBlockTest() throws RpcException {
        long firstAvailableBlock = client.getApi().getFirstAvailableBlock();
        LOGGER.info(String.format("First available block in the ledger = %d", firstAvailableBlock));
        assertTrue(firstAvailableBlock >= 0);
    }

    @Test
    public void getGenesisHashTest() throws RpcException {
        String genesisHash = client.getApi().getGenesisHash();
        LOGGER.info(String.format("Genesis hash = %s", genesisHash));
        assertNotNull(genesisHash);
    }

    @Test
    public void getTokenAccountBalanceTest() throws RpcException {
        TokenAmountInfo tokenAccountBalance = client.getApi().getTokenAccountBalance(PublicKey.valueOf(
                "8tnpAECxAT9nHBqR1Ba494Ar5dQMPGhL31MmPJz1zZvY"));
        LOGGER.info(tokenAccountBalance.toString());

        //validate the returned data
        assertNotNull(tokenAccountBalance);
        assertEquals(6, tokenAccountBalance.getDecimals());
        assertTrue(tokenAccountBalance.getUiAmount() > 0);
    }

    @Test
    public void getTokenSupplyTest() throws RpcException {
        TokenAmountInfo tokenSupply = client.getApi().getTokenSupply(PublicKey.valueOf(
                "4k3Dyjzvzp8eMZWUXbBCjEvwSkkk59S5iCNLY3QrkX6R"));
        LOGGER.info(tokenSupply.toString());

        //validate the returned data
        assertNotNull(tokenSupply);
        assertEquals(6, tokenSupply.getDecimals());
        assertTrue(tokenSupply.getUiAmount() > 0);
    }

    @Test
    @Disabled
    public void getTokenLargestAccountsTest() throws RpcException {
        List<TokenAccount> tokenAccounts = client.getApi().getTokenLargestAccounts(PublicKey.valueOf(
                "4k3Dyjzvzp8eMZWUXbBCjEvwSkkk59S5iCNLY3QrkX6R"));
        LOGGER.info(tokenAccounts.toString());

        //validate the returned data
        assertNotNull(tokenAccounts);
        assertEquals(20, tokenAccounts.size());
        tokenAccounts.forEach(tokenAccount -> {
            assertEquals(6, tokenAccount.getDecimals());
        });
    }

    @Test
    public void getTokenAccountsByOwnerTest() throws RpcException {
        Map<String, Object> requiredParams = Map.of("mint", USDC_TOKEN_MINT);
        TokenAccountInfo tokenAccount = client.getApi().getTokenAccountsByOwner(PublicKey.valueOf(
                "AoUnMozL1ZF4TYyVJkoxQWfjgKKtu8QUK9L4wFdEJick"), requiredParams, new HashMap<>());
        LOGGER.info(tokenAccount.toString());

        //validate the returned data
        assertNotNull(tokenAccount);
        assertEquals("27T5c11dNMXjcRuko9CeUy3Wq41nFdH3tz9Qt4REzZMM", tokenAccount.getValue().get(0).getPubkey());
    }

    @Test
    @Disabled
    public void getTokenAccountsByDelegateTest() throws RpcException {
        Map<String, Object> requiredParams = Map.of("mint", USDC_TOKEN_MINT);
        TokenAccountInfo tokenAccount = client.getApi().getTokenAccountsByDelegate(PublicKey.valueOf(
                "AoUnMozL1ZF4TYyVJkoxQWfjgKKtu8QUK9L4wFdEJick"), requiredParams, new HashMap<>());
        LOGGER.info(tokenAccount.toString());

        //validate the returned data
        assertNotNull(tokenAccount);
        assertTrue(tokenAccount.getValue().isEmpty());
    }

    @Test
    public void getTransactionCountTest() throws RpcException {
        long transactionCount = client.getApi().getTransactionCount();
        assertTrue(transactionCount > 0);
    }

    @Test
    @Disabled
    public void getFeeCalculatorForBlockhashTest() throws RpcException, InterruptedException {
        String recentBlockHash = client.getApi().getRecentBlockhash();
        Thread.sleep(20000L);
        FeeCalculatorInfo feeCalculatorInfo = client.getApi().getFeeCalculatorForBlockhash(recentBlockHash);
        LOGGER.info(feeCalculatorInfo.getValue().getFeeCalculator().toString());

        assertNotNull(feeCalculatorInfo);
        assertTrue(feeCalculatorInfo.getValue().getFeeCalculator().getLamportsPerSignature() > 0);
    }

    @Test
    @Disabled
    public void getFeesRateGovernorTest() throws RpcException {
        FeeRateGovernorInfo feeRateGovernorInfo = client.getApi().getFeeRateGovernor();
        LOGGER.info(feeRateGovernorInfo.getValue().getFeeRateGovernor().toString());

        assertNotNull(feeRateGovernorInfo);
        assertTrue(feeRateGovernorInfo.getValue().getFeeRateGovernor().getBurnPercent() > 0);
        assertTrue(feeRateGovernorInfo.getValue().getFeeRateGovernor().getMaxLamportsPerSignature() > 0);
        assertTrue(feeRateGovernorInfo.getValue().getFeeRateGovernor().getMinLamportsPerSignature() > 0);
        assertTrue(feeRateGovernorInfo.getValue().getFeeRateGovernor().getTargetLamportsPerSignature() >= 0);
        assertTrue(feeRateGovernorInfo.getValue().getFeeRateGovernor().getTargetSignaturesPerSlot() >= 0);
    }

    @Test
    @Disabled
    public void getFeesInfoTest() throws RpcException {
        FeesInfo feesInfo = client.getApi().getFees();
        LOGGER.info(feesInfo.toString());

        assertNotNull(feesInfo);
        assertNotEquals("", feesInfo.getValue().getBlockhash());
        assertTrue(feesInfo.getValue().getFeeCalculator().getLamportsPerSignature() > 0);
        assertTrue(feesInfo.getValue().getLastValidSlot() > 0);
    }

    @Test
    public void getFeeForMessageTest() throws RpcException {
        Long result = client.getApi().getFeeForMessage("AQABAgIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEBAQAA");
        LOGGER.info("feeForMessageTest = " + result);
    }

    @Test
    @Disabled
    public void getMaxRetransmitSlotTest() throws RpcException {
        long maxRetransmitSlot = client.getApi().getMaxRetransmitSlot();
        LOGGER.info("maxRetransmitSlot = " + maxRetransmitSlot);
        assertTrue(maxRetransmitSlot > 0);
    }

    @Test
    public void getBalanceTest() throws RpcException {
        long balance = client.getApi().getBalance(PublicKey.valueOf("CuieVDEDtLo7FypA9SbLM9saXFdb1dsshEkyErMqkRQq"));
        LOGGER.info(String.format("Balance = %d", balance));
        assertTrue(balance >= 0);
    }

    @Test
    public void getMinimumBalanceForRentExemptionTest() throws RpcException {
        long minimumBalance = client.getApi().getMinimumBalanceForRentExemption(5000);
        LOGGER.info(String.format("Minimum balance for rent exemption = %d", minimumBalance));
        assertTrue(minimumBalance > 0);
    }

    @Test
    @Disabled
    public void getRecentBlockhashTest() throws RpcException {
        String recentBlockhash = client.getApi().getRecentBlockhash();
        LOGGER.info(String.format("Recent blockhash = %s", recentBlockhash));
        assertNotNull(recentBlockhash);
    }

    @Disabled
    @Test
    public void getStakeActivationTest() throws RpcException {
        StakeActivation stakeActivation = client.getApi().getStakeActivation(
                PublicKey.valueOf("CYRJWqiSjLitBAcRxPvWpgX3s5TvmN2SuRY3eEYypFvT")
        );

        LOGGER.info(stakeActivation.toString());

        //validate the returned data
        assertNotNull(stakeActivation);
        assertEquals("active", stakeActivation.getState());
        assertTrue(stakeActivation.getActive() > 0);
        assertEquals(0, stakeActivation.getInactive());
    }

    @Disabled
    @Test
    public void simulateTransactionTest() throws RpcException {
        String transaction = "ASdDdWBaKXVRA+6flVFiZokic9gK0+r1JWgwGg/GJAkLSreYrGF4rbTCXNJvyut6K6hupJtm72GztLbWNmRF1Q4BAAEDBhrZ0FOHFUhTft4+JhhJo9+3/QL6vHWyI8jkatuFPQzrerzQ2HXrwm2hsYGjM5s+8qMWlbt6vbxngnO8rc3lqgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAy+KIwZmU8DLmYglP3bPzrlpDaKkGu6VIJJwTOYQmRfUBAgIAAQwCAAAAuAsAAAAAAAA=";
        List<PublicKey> addresses = List.of(PublicKey.valueOf("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo"));
        SimulatedTransaction simulatedTransaction = client.getApi().simulateTransaction(transaction, addresses);
        assertTrue(simulatedTransaction.getValue().getLogs().size() > 0);
    }

    @Test
    @Disabled
    public void sendTokenTest() {
        final PublicKey source = usdcSource; // Private key's USDC token account
        final PublicKey destination = usdcDestination; // Destination's USDC account
        final int tokenAmount = 10; // 0.000100 USDC

        // Create account from private key
        final Account owner = testAccount;

        // "10" = 0.0000001 (or similar)
        final String txId = tokenManager.transfer(
                owner,
                source,
                destination,
                USDC_TOKEN_MINT,
                tokenAmount
        );

        assertNotNull(txId);
    }

    @Test
    @Disabled
    public void transferCheckedTest() {
        final PublicKey source = usdcSource; // Private key's USDC token account
        final PublicKey destination = solDestination;

        /*
            amount = "0.0001" usdc
            amount = 100
            decimals = 6
         */

        final long tokenAmount = 100;
        final byte decimals = 6;

        // Create account from private key
        final Account owner = testAccount;

        final String txId = tokenManager.transferCheckedToSolAddress(
                owner,
                source,
                destination,
                USDC_TOKEN_MINT,
                tokenAmount,
                decimals
        );

        // TODO - actually verify something
        assertNotNull(txId);
    }

    @Test
    @Disabled
    public void initializeAccountTest() {
        final Account owner = testAccount;
        final Account newAccount = new Account();
        final String txId = tokenManager.initializeAccount(
                newAccount,
                USDC_TOKEN_MINT,
                owner
        );

        // TODO - actually verify something
        assertNotNull(txId);
        System.out.println(testAccount.getPublicKey().toBase58());
    }

    @Test
    @Disabled
    public void getConfirmedBlockTest() throws RpcException {
        ConfirmedBlock block = this.client.getApi().getConfirmedBlock(124398367);
        assertEquals(124398367, block.getParentSlot());
    }

    @Test
    @Disabled
    public void getSignaturesForAddressTest() throws RpcException {
        List<SignatureInformation> confirmedSignatures = client.getApi().getSignaturesForAddress(
                solDestination,
                15,
                Commitment.CONFIRMED
        );

        confirmedSignatures.forEach(signatureInformation -> System.out.println(signatureInformation.getSlot() + ": " + signatureInformation.getSignature()));
        assertTrue(confirmedSignatures.size() > 0);
    }

    @Test
    @Disabled
    public void getBlockTest() throws RpcException {
        Block block = this.client.getApi().getBlock(124398367);
        assertEquals(112516757, block.getBlockHeight());

        Block blockWithVersion = this.client.getApi().getBlock(124398367, Map.of(
                "maxSupportedTransactionVersion", 0
        ));
        assertEquals(112516757, blockWithVersion.getBlockHeight());
    }

    // Ignored since some validators can only get recent blocks
    @Test
    @Disabled
    public void getConfirmedBlocksTest() throws RpcException {
        List<Double> blocks = this.client.getApi().getConfirmedBlocks(5);
        List<Double> singleBlock = this.client.getApi().getConfirmedBlocks(5, 5);
        assertEquals(Double.valueOf(5), Double.valueOf(blocks.get(0)));
        assertEquals(Double.valueOf(5), Double.valueOf(singleBlock.get(0)));
    }

    @Test
    public void getVoteAccountsTest() throws RpcException {
        VoteAccounts voteAccounts = client.getApi().getVoteAccounts();
        assertNotNull(voteAccounts.getCurrent().get(0).getVotePubkey());
    }

    @Test
    @Disabled
    public void getSignatureStatusesTest() throws RpcException {
        SignatureStatuses signatureStatuses = client.getApi().getSignatureStatuses(
                List.of(
                        "3nVfYabxKv9ohGb4nXF3EyJQnbVcGVQAm2QKzdPrsemrP4D8UEZEzK8bCWgyTFif6mjo99akvHcCbxiEKzN5L9ZG",
                        "5GvXGwBGocNuVAuTuDyXfxJYMx7SC1cs7owC5r48RNg9UAhTBUQ6irU932fpVAwrVy8WFCUct2RSvtKHUJBRfC5j"
                ),
                true
        );

        assertTrue(signatureStatuses.getValue().get(0).getConfirmationStatus().length() > 0);
    }

    @Test
    public void getRecentPerformanceSamplesLimitTest() throws RpcException {
        List<PerformanceSample> performanceSamples = client.getApi().getRecentPerformanceSamples(3);

        assertEquals(3, performanceSamples.size());
        assertTrue(performanceSamples.get(0).getSlot() > 0);
    }

    @Test
    @Disabled
    public void getHealthTest() throws RpcException {
        boolean isHealthy = client.getApi().getHealth();

        assertTrue(isHealthy);
    }

    @Test
    @Disabled
    public void getLargestAccountsTest() throws RpcException {
        List<LargeAccount> largeAccounts = client.getApi().getLargestAccounts();

        assertTrue(largeAccounts.size() > 0);
        assertTrue(largeAccounts.get(0).getLamports() > 0);
    }

    @Test
    public void getLeaderScheduleTest() throws RpcException {
        List<LeaderSchedule> leaderSchedules = client.getApi().getLeaderSchedule();

        assertTrue(leaderSchedules.size() > 0);
    }

    @Test
    @Disabled
    public void getLeaderScheduleTest_identity() throws RpcException {
        List<LeaderSchedule> leaderSchedules = client.getApi().getLeaderSchedule(null,
                "12oRmi8YDbqpkn326MdjwFeZ1bh3t7zVw8Nra2QK2SnR", null);

        assertSame(1, leaderSchedules.size());
        assertEquals("12oRmi8YDbqpkn326MdjwFeZ1bh3t7zVw8Nra2QK2SnR", leaderSchedules.get(0).getIdentity());
    }

    @Test
    public void getMultipleAccountsTest() throws RpcException {
        List<AccountInfo.Value> accounts = client.getApi().getMultipleAccounts(
                List.of(
                        PublicKey.valueOf("skynetDj29GH6o6bAqoixCpDuYtWqi1rm8ZNx1hB3vq"),
                        PublicKey.valueOf("namesLPneVptA9Z5rqUDD9tMTWEJwofgaYwp8cawRkX")
                )
        );

        assertNotNull(accounts);
        assertEquals(2, accounts.size());
    }

    @Test
    public void getMultipleAccountsMapTest() throws RpcException {
        Map<PublicKey, Optional<AccountInfo.Value>> accounts = client.getApi().getMultipleAccountsMap(
                List.of(
                        PublicKey.valueOf("skynetDj29GH6o6bAqoixCpDuYtWqi1rm8ZNx1hB3vq"),
                        PublicKey.valueOf("EQFKqRty2TfpdTiB7Fyw8wquFxJSRrCD5gP5SPRWzKFZ"), // doesn't exist
                        PublicKey.valueOf("namesLPneVptA9Z5rqUDD9tMTWEJwofgaYwp8cawRkX")
                )
        );

        assertNotNull(accounts);
        assertTrue(accounts.get(PublicKey.valueOf("EQFKqRty2TfpdTiB7Fyw8wquFxJSRrCD5gP5SPRWzKFZ")).isEmpty());
        assertTrue(accounts.get(PublicKey.valueOf("skynetDj29GH6o6bAqoixCpDuYtWqi1rm8ZNx1hB3vq")).isPresent());
        assertEquals(3, accounts.size());
    }

    @Test
    public void testGetDecodedData() {
        try {
            byte[] data = client.getApi()
                    .getAccountInfo(new PublicKey("8BnEgHoWFysVcuFFX7QztDmzuH8r5ZFvyP3sYwn1XTh6"))
                    .getDecodedData();

            LOGGER.info(Arrays.toString(data));

            assertEquals(388, data.length);
        } catch (RpcException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void getTransactionTest() throws RpcException {
        String transactionSignature =
                "25XzdvPoirNY8kFALxVZXWbdU6LmMitNceHciYRSNV4S5zjUPjeJaWHCP9dingewmrsrcoKtAP57JXyVXtSsV6Bv";

        ConfirmedTransaction transactionInfo = client.getApi().getTransaction(transactionSignature);

        String fromKey = transactionInfo.getTransaction().getMessage().getAccountKeys().get(0);
        String toKey = transactionInfo.getTransaction().getMessage().getAccountKeys().get(1);

        assertEquals("HHntUXQbUBdx8HZQQaT7W1ZSgKRitMtForz4YJXc6qF6", fromKey);
        assertEquals("6QcgNYEqHeUohoJWR5ppuRg9Ugh6scMzJY4j4tFnrZMu", toKey);

        ConfirmedTransaction transactionInfoCommitted = client.getApi().getTransaction(transactionSignature, Commitment.CONFIRMED);

        String fromKeyCommitted = transactionInfoCommitted.getTransaction().getMessage().getAccountKeys().get(0);
        String toKeyCommitted = transactionInfoCommitted.getTransaction().getMessage().getAccountKeys().get(1);

        assertEquals("HHntUXQbUBdx8HZQQaT7W1ZSgKRitMtForz4YJXc6qF6", fromKeyCommitted);
        assertEquals("6QcgNYEqHeUohoJWR5ppuRg9Ugh6scMzJY4j4tFnrZMu", toKeyCommitted);
    }

    @Test
    @Disabled
    public void isBlockhashValidTest() throws RpcException, InterruptedException {
        String recentBlockHash = client.getApi().getRecentBlockhash();
        Thread.sleep(500L);
        assertTrue(client.getApi().isBlockhashValid(recentBlockHash));
    }

    @Test
    public void rentEpochTest() {
        PublicKey testMarket = new PublicKey("52fF6wBZdSL8niV2tGuPu5gap15qLu6vMbmDoPVEDRqL");

        try {
            AccountInfo accountInfo = client.getApi().getAccountInfo(testMarket);
            LOGGER.info(String.format("Account: %s", accountInfo.toString()));
        } catch (RpcException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testGetStakeMinimumDelegationWithoutCommitment() throws RpcException {
        Long minDelegation = client.getApi().getStakeMinimumDelegation();
        assertNotNull(minDelegation);
        assertTrue(minDelegation > 0);
        LOGGER.info("Minimum stake delegation (without commitment): " + minDelegation);
    }

    /**
     * Test getStakeMinimumDelegation with commitment
     */
    @Test
    public void testGetStakeMinimumDelegationWithCommitment() throws RpcException {
        Long minDelegation = client.getApi().getStakeMinimumDelegation(Commitment.FINALIZED);
        assertNotNull(minDelegation);
        assertTrue(minDelegation > 0);
        LOGGER.info("Minimum stake delegation (with FINALIZED commitment): " + minDelegation);
    }

    @Test
    public void testGetBlocks() throws RpcException {
        long startSlot = client.getApi().getSlot() - 10; // 10 slots before the current slot
        long endSlot = client.getApi().getSlot();

        List<Long> blocks = client.getApi().getBlocks(startSlot, endSlot);

        assertNotNull(blocks);
        assertFalse(blocks.isEmpty());
        assertTrue(blocks.get(0) >= startSlot);
        assertTrue(blocks.get(blocks.size() - 1) <= endSlot);
    }

    @Test
    public void testGetBlocksWithCommitment() throws RpcException {
        long startSlot = client.getApi().getSlot() - 10; // 10 slots before the current slot
        long endSlot = client.getApi().getSlot();

        List<Long> blocks = client.getApi().getBlocks(startSlot, endSlot, Commitment.CONFIRMED);

        assertNotNull(blocks);
        assertFalse(blocks.isEmpty());
        assertTrue(blocks.get(0) >= startSlot);
        assertTrue(blocks.get(blocks.size() - 1) <= endSlot);
    }

    @Test
    public void testGetBlocksWithLimit() throws RpcException {
        long startSlot = client.getApi().getSlot() - 10; // 10 slots before the current slot
        long limit = 5;

        List<Long> blocks = client.getApi().getBlocksWithLimit(startSlot, limit);

        assertNotNull(blocks);
        assertFalse(blocks.isEmpty());
        assertTrue(blocks.get(0) >= startSlot);
        assertTrue(blocks.size() <= limit);
    }

    @Test
    public void testGetBlocksWithLimitAndCommitment() throws RpcException {
        long startSlot = client.getApi().getSlot() - 10; // 10 slots before the current slot
        long limit = 5;

        List<Long> blocks = client.getApi().getBlocksWithLimit(startSlot, limit, Commitment.CONFIRMED);

        assertNotNull(blocks);
        assertFalse(blocks.isEmpty());
        assertTrue(blocks.get(0) >= startSlot);
        assertTrue(blocks.size() <= limit);
    }
}