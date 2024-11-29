package org.p2p.solanaj.rpc;

import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.rpc.types.*;
import org.p2p.solanaj.rpc.types.RpcResultTypes.ValueLong;
import org.p2p.solanaj.rpc.types.TokenResultObjects.TokenAccount;
import org.p2p.solanaj.rpc.types.TokenResultObjects.TokenAmountInfo;
import org.p2p.solanaj.rpc.types.config.*;
import org.p2p.solanaj.rpc.types.config.RpcSendTransactionConfig.Encoding;
import org.p2p.solanaj.ws.SubscriptionWebSocketClient;
import org.p2p.solanaj.ws.listeners.NotificationEventListener;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RpcApi {
    private RpcClient client;

    public RpcApi(RpcClient client) {
        this.client = client;
    }

    public LatestBlockhash getLatestBlockhash() throws RpcException {
        return getLatestBlockhash(null);
    }

    public LatestBlockhash getLatestBlockhash(Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();

        if (commitment != null) {
            params.add(Map.of("commitment", commitment.getValue()));
        }

        return client.call("getLatestBlockhash", params, LatestBlockhash.class);
    }

    @Deprecated
    public String getRecentBlockhash() throws RpcException {
        return getRecentBlockhash(null);
    }

    @Deprecated
    public String getRecentBlockhash(Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();

        if (commitment != null) {
            params.add(Map.of("commitment", commitment.getValue()));
        }

        return client.call("getRecentBlockhash", params, RecentBlockhash.class).getValue().getBlockhash();
    }

    public String sendTransaction(Transaction transaction, Account signer, String recentBlockHash) throws
            RpcException {
        return sendTransaction(transaction, Collections.singletonList(signer), recentBlockHash);
    }

    public String sendTransaction(Transaction transaction, Account signer) throws RpcException {
        return sendTransaction(transaction, Collections.singletonList(signer), null);
    }

    /**
     * Sends a transaction to the RPC server.
     *
     * @param transaction             The transaction to send.
     * @param signers                 The list of accounts signing the transaction.
     * @param recentBlockHash         The recent block hash. If null, it will be obtained from the RPC server.
     * @param rpcSendTransactionConfig The configuration object for sending transactions via RPC.
     * @return The transaction ID as a string.
     * @throws RpcException If an error occurs during the RPC call.
     */
    public String sendTransaction(Transaction transaction, List<Account> signers, String recentBlockHash,
                                  RpcSendTransactionConfig rpcSendTransactionConfig)
            throws RpcException {
        if (recentBlockHash == null) {
            recentBlockHash = getLatestBlockhash().getValue().getBlockhash();
        }
        transaction.setRecentBlockHash(recentBlockHash);
        transaction.sign(signers);
        byte[] serializedTransaction = transaction.serialize();

        String base64Trx = Base64.getEncoder().encodeToString(serializedTransaction);

        List<Object> params = new ArrayList<>();

        params.add(base64Trx);
        params.add(rpcSendTransactionConfig);

        return client.call("sendTransaction", params, String.class);
    }

    /**
     * Sends a transaction to the RPC server with external signer
     *
     * @param transaction - The transaction to send.
     * @param feePayerPublicKey - The public key of the signer's account.
     * @param externalSigner - Function for external sign.
     * @param recentBlockHash - The recent block hash. If null, it will be obtained from the RPC server.
     * @param rpcSendTransactionConfig - The configuration object for sending transactions via RPC.
     * @return The transaction ID as a string.
     * @throws RpcException If an error occurs during the RPC call.
     */
    public String sendTransaction(Transaction transaction, PublicKey feePayerPublicKey,
                                  Function<byte[], byte[]> externalSigner, String recentBlockHash,
                                  RpcSendTransactionConfig rpcSendTransactionConfig) throws RpcException {
        if (recentBlockHash == null) {
            recentBlockHash = getLatestBlockhash().getValue().getBlockhash();
        }
        transaction.setRecentBlockHash(recentBlockHash);
        transaction.signByExternalSigner(feePayerPublicKey, externalSigner);
        byte[] serializedTransaction = transaction.serialize();

        String base64Trx = Base64.getEncoder().encodeToString(serializedTransaction);

        List<Object> params = new ArrayList<>();

        params.add(base64Trx);
        params.add(rpcSendTransactionConfig);

        return client.call("sendTransaction", params, String.class);
    }

    /**
     * Sends a transaction to the network for processing.
     * A default RpcSendTransactionConfig is used.
     *
     * @param transaction    the transaction to send
     * @param signers        the list of accounts that will sign the transaction
     * @param recentBlockHash    the recent block hash to include in the transaction
     * @return the result of the transaction
     * @throws RpcException    if an error occurs during the RPC call
     */
    public String sendTransaction(Transaction transaction, List<Account> signers, String recentBlockHash)
            throws RpcException {
        return sendTransaction(transaction, signers, recentBlockHash, new RpcSendTransactionConfig());
    }

    public void sendAndConfirmTransaction(Transaction transaction, List<Account> signers,
            NotificationEventListener listener) throws RpcException {
        String signature = sendTransaction(transaction, signers, null);

        SubscriptionWebSocketClient subClient = SubscriptionWebSocketClient.getInstance(client.getEndpoint());
        subClient.signatureSubscribe(signature, listener);
    }

    public long getBalance(PublicKey account) throws RpcException {
        return getBalance(account, null);
    }

    public long getBalance(PublicKey account, Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();

        params.add(account.toString());
        if (commitment != null) {
            params.add(Map.of("commitment", commitment.getValue()));
        }

        return client.call("getBalance", params, ValueLong.class).getValue();
    }

    public ConfirmedTransaction getTransaction(String signature) throws RpcException {
        return getTransaction(signature, null);
    }

    public ConfirmedTransaction getTransaction(String signature, Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();
        params.add(signature);
        Map<String, Object> parameterMap = new HashMap<>();

        if (commitment != null) {
            parameterMap.put("commitment", commitment);
        }

        parameterMap.put("maxSupportedTransactionVersion", 0);
        params.add(parameterMap);
        return client.call("getTransaction", params, ConfirmedTransaction.class);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<SignatureInformation> getConfirmedSignaturesForAddress2(PublicKey account, int limit)
            throws RpcException {
        List<Object> params = new ArrayList<>();

        params.add(account.toString());
        params.add(new ConfirmedSignFAddr2(limit, Commitment.CONFIRMED));

        List<AbstractMap> rawResult = client.call("getConfirmedSignaturesForAddress2", params, List.class);

        List<SignatureInformation> result = new ArrayList<>();
        for (AbstractMap item : rawResult) {
            result.add(new SignatureInformation(item));
        }

        return result;
    }

    public List<SignatureInformation> getSignaturesForAddress(PublicKey account, int limit, Commitment commitment)
            throws RpcException {
        List<Object> params = new ArrayList<>();

        params.add(account.toString());
        params.add(new ConfirmedSignFAddr2(limit, commitment));

        List<AbstractMap> rawResult = client.call("getSignaturesForAddress", params, List.class);

        List<SignatureInformation> result = new ArrayList<>();
        for (AbstractMap item : rawResult) {
            result.add(new SignatureInformation(item));
        }

        return result;
    }

    public List<ProgramAccount> getProgramAccounts(PublicKey account, long offset, String bytes) throws RpcException {
        List<Object> filters = new ArrayList<>();
        filters.add(new Filter(new Memcmp(offset, bytes)));

        ProgramAccountConfig programAccountConfig = new ProgramAccountConfig(filters);
        return getProgramAccounts(account, programAccountConfig);
    }

    public List<ProgramAccount> getProgramAccountsBase64(PublicKey account, long offset, String bytes) throws RpcException {
        List<Object> filters = new ArrayList<>();
        Memcmp memcmp = new Memcmp(offset, bytes);

        filters.add(new Filter(memcmp));

        ProgramAccountConfig programAccountConfig = new ProgramAccountConfig(Encoding.base64);
        programAccountConfig.setFilters(filters);
        return getProgramAccounts(account, programAccountConfig);
    }

    public List<ProgramAccount> getProgramAccounts(PublicKey account) throws RpcException {
        return getProgramAccounts(account, new ProgramAccountConfig(Encoding.base64));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<ProgramAccount> getProgramAccounts(PublicKey account, ProgramAccountConfig programAccountConfig)
            throws RpcException {
        List<Object> params = new ArrayList<>();

        params.add(account.toString());

        if (programAccountConfig != null) {
            params.add(programAccountConfig);
        }

        List<AbstractMap> rawResult = client.call("getProgramAccounts", params, List.class);

        List<ProgramAccount> result = new ArrayList<>();
        for (AbstractMap item : rawResult) {
            result.add(new ProgramAccount(item));
        }

        return result;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<ProgramAccount> getProgramAccounts(PublicKey account, List<Memcmp> memcmpList, int dataSize)
            throws RpcException {
        List<Object> params = new ArrayList<>();

        params.add(account.toString());

        List<Object> filters = new ArrayList<>();
        memcmpList.forEach(memcmp -> {
            filters.add(new Filter(memcmp));
        });

        filters.add(new DataSize(dataSize));

        ProgramAccountConfig programAccountConfig = new ProgramAccountConfig(filters);
        programAccountConfig.setEncoding(Encoding.base64);
        params.add(programAccountConfig);

        List<AbstractMap> rawResult = client.call("getProgramAccounts", params, List.class);

        List<ProgramAccount> result = new ArrayList<>();
        for (AbstractMap item : rawResult) {
            result.add(new ProgramAccount(item));
        }

        return result;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<ProgramAccount> getProgramAccounts(PublicKey account, List<Memcmp> memcmpList) throws RpcException {
        List<Object> params = new ArrayList<>();

        params.add(account.toString());

        List<Object> filters = new ArrayList<>();
        memcmpList.forEach(memcmp -> {
            filters.add(new Filter(memcmp));
        });

        ProgramAccountConfig programAccountConfig = new ProgramAccountConfig(filters);
        programAccountConfig.setEncoding(Encoding.base64);
        params.add(programAccountConfig);

        List<AbstractMap> rawResult = client.call("getProgramAccounts", params, List.class);

        List<ProgramAccount> result = new ArrayList<>();
        for (AbstractMap item : rawResult) {
            result.add(new ProgramAccount(item));
        }

        return result;
    }

    public AccountInfo getAccountInfo(PublicKey account) throws RpcException {
        return getAccountInfo(account, new HashMap<>());
    }

    public AccountInfo getAccountInfo(PublicKey account, Map<String, Object> additionalParams) throws RpcException {
        List<Object> params = new ArrayList<>();

        Map<String, Object> parameterMap = new HashMap<>();

        parameterMap.put("encoding", additionalParams.getOrDefault("encoding", "base64"));

        if (additionalParams.containsKey("commitment")) {
            Commitment commitment = (Commitment) additionalParams.get("commitment");
            parameterMap.put("commitment", commitment.getValue());
        }
        if (additionalParams.containsKey("dataSlice")) {
            parameterMap.put("dataSlice", additionalParams.get("dataSlice"));
        }

        if (additionalParams.containsKey("minContextSlot")) {
            parameterMap.put("minContextSlot", additionalParams.get("minContextSlot"));
        }

        params.add(account.toString());
        params.add(parameterMap);

        return client.call("getAccountInfo", params, AccountInfo.class);
    }

    public SplTokenAccountInfo getSplTokenAccountInfo(PublicKey account) throws RpcException {
        List<Object> params = new ArrayList<>();
        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("encoding", "jsonParsed");

        params.add(account.toString());
        params.add(parameterMap);

        return client.call("getAccountInfo", params, SplTokenAccountInfo.class);
    }

    public long getMinimumBalanceForRentExemption(long dataLength) throws RpcException {
        return getMinimumBalanceForRentExemption(dataLength, null);
    }

    public long getMinimumBalanceForRentExemption(long dataLength, Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();

        params.add(dataLength);
        if (commitment != null) {
            params.add(Map.of("commitment", commitment.getValue()));
        }

        return client.call("getMinimumBalanceForRentExemption", params, Long.class);
    }

    public long getBlockTime(long block) throws RpcException {
        List<Object> params = new ArrayList<>();

        params.add(block);

        return client.call("getBlockTime", params, Long.class);
    }

    /**
     * Seemingly deprecated on the official Solana API.
     *
     * @return
     * @throws RpcException
     */
    public long getBlockHeight() throws RpcException {
        return getBlockHeight(null);
    }

    public long getBlockHeight(Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();

        if (commitment != null) {
            params.add(Map.of("commitment", commitment.getValue()));
        }
        return client.call("getBlockHeight", params, Long.class);
    }

    public BlockProduction getBlockProduction() throws RpcException {
        return getBlockProduction(new HashMap<>());
    }

    // TODO - implement the parameters - currently takes in none
    public BlockProduction getBlockProduction(Map<String, Object> optionalParams) throws RpcException {
        List<Object> params = new ArrayList<>();

        Map<String, Object> parameterMap = new HashMap<>();
        if (optionalParams.containsKey("commitment")) {
            Commitment commitment = (Commitment) optionalParams.get("commitment");
            parameterMap.put("commitment", commitment.getValue());
        }
        params.add(parameterMap);

        return client.call("getBlockProduction", params, BlockProduction.class);
    }

    public Long minimumLedgerSlot() throws RpcException {
        return client.call("minimumLedgerSlot", new ArrayList<>(), Long.class);
    }

    public SolanaVersion getVersion() throws RpcException {
        return client.call("getVersion", new ArrayList<>(), SolanaVersion.class);
    }

    public String requestAirdrop(PublicKey address, long lamports) throws RpcException {
        return requestAirdrop(address, lamports, null);
    }

    public String requestAirdrop(PublicKey address, long lamports, Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();

        params.add(address.toString());
        params.add(lamports);
        if (commitment != null) {
            params.add(Map.of("commitment", commitment.getValue()));
        }

        return client.call("requestAirdrop", params, String.class);
    }

    public BlockCommitment getBlockCommitment(long block) throws RpcException {
        List<Object> params = new ArrayList<>();

        params.add(block);

        return client.call("getBlockCommitment", params, BlockCommitment.class);
    }

    @Deprecated
    public FeeCalculatorInfo getFeeCalculatorForBlockhash(String blockhash) throws RpcException {
        return getFeeCalculatorForBlockhash(blockhash, null);
    }

    @Deprecated
    public FeeCalculatorInfo getFeeCalculatorForBlockhash(String blockhash, Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();

        params.add(blockhash);
        if (commitment != null) {
            params.add(Map.of("commitment", commitment.getValue()));
        }

        return client.call("getFeeCalculatorForBlockhash", params, FeeCalculatorInfo.class);
    }

    @Deprecated
    public FeeRateGovernorInfo getFeeRateGovernor() throws RpcException {
        return client.call("getFeeRateGovernor", new ArrayList<>(), FeeRateGovernorInfo.class);
    }

    /**
     * Gets the fee the network will charge for a particular message
     *
     * @param message Base-64 encoded Message
     * @return Fee for the message
     * @throws RpcException if the RPC call fails
     */
    public Long getFeeForMessage(String message) throws RpcException {
        return getFeeForMessage(message, null);
    }

    /**
     * Gets the fee the network will charge for a particular message
     *
     * @param message Base-64 encoded Message
     * @param commitment Optional commitment level
     * @return Fee for the message
     * @throws RpcException if the RPC call fails
     */
    public Long getFeeForMessage(String message, Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();
        params.add(message);

        Map<String, Object> configMap = new HashMap<>();
        if (commitment != null) {
            configMap.put("commitment", commitment.getValue());
        }
        params.add(configMap);

        Long feeValue = client.call("getFeeForMessage", params, ValueLong.class).getValue();

        if (feeValue == null) {
            return 0L;
        } else {
            return feeValue;
        }
    }

    /**
     * Gets a list of prioritization fees from recent blocks
     *
     * @return List of RecentPrioritizationFees
     * @throws RpcException if the RPC call fails
     */
    public List<RecentPrioritizationFees> getRecentPrioritizationFees() throws RpcException {
        return getRecentPrioritizationFees(null);
    }

    /**
     * Gets a list of prioritization fees from recent blocks
     *
     * @param addresses Optional list of PublicKey addresses to filter by
     * @return List of RecentPrioritizationFees
     * @throws RpcException if the RPC call fails
     */
    public List<RecentPrioritizationFees> getRecentPrioritizationFees(List<PublicKey> addresses) throws RpcException {
        List<Object> params = new ArrayList<>();

        if (addresses != null) {
            params.add(addresses.stream().map(PublicKey::toBase58).toList());
        }

        List<Map<String, Object>> rawResult = client.call("getRecentPrioritizationFees", params, List.class);
        
        List<RecentPrioritizationFees> result = new ArrayList<>();
        for (Map<String, Object> item : rawResult) {
            result.add(new RecentPrioritizationFees(item));
        }
        
        return result;
    }

    /**
     * Gets the current stake minimum delegation
     *
     * @return Stake minimum delegation in lamports
     * @throws RpcException if the RPC call fails
     */
    public Long getStakeMinimumDelegation() throws RpcException {
        return getStakeMinimumDelegation(null);
    }

    /**
     * Gets the current stake minimum delegation
     *
     * @param commitment Optional commitment level
     * @return Stake minimum delegation in lamports
     * @throws RpcException if the RPC call fails
     */
    public Long getStakeMinimumDelegation(Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();
        
        if (commitment != null) {
            Map<String, Object> configMap = new HashMap<>();
            configMap.put("commitment", commitment.getValue());
            params.add(configMap);
        }

        return client.call("getStakeMinimumDelegation", params, ValueLong.class).getValue();
    }

    @Deprecated
    public FeesInfo getFees() throws RpcException {
        return getFees(null);
    }

    @Deprecated
    public FeesInfo getFees(Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();

        if (commitment != null) {
            params.add(Map.of("commitment", commitment.getValue()));
        }

        return client.call("getFees", params, FeesInfo.class);
    }

    public long getTransactionCount() throws RpcException {
        return getTransactionCount(null);
    }

    public long getTransactionCount(Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();

        if (commitment != null) {
            params.add(Map.of("commitment", commitment.getValue()));
        }

        return client.call("getTransactionCount", params, Long.class);
    }

    public long getMaxRetransmitSlot() throws RpcException {
        return client.call("getMaxRetransmitSlot", new ArrayList<>(), Long.class);
    }

    public SimulatedTransaction simulateTransaction(String transaction, List<PublicKey> addresses) throws RpcException {
        SimulateTransactionConfig simulateTransactionConfig = new SimulateTransactionConfig(Encoding.base64);
        simulateTransactionConfig.setAccounts(
                Map.of(
                        "encoding",
                        Encoding.base64,
                        "addresses",
                        addresses.stream().map(PublicKey::toBase58).collect(Collectors.toList()))
        );
        simulateTransactionConfig.setReplaceRecentBlockhash(true);

        List<Object> params = new ArrayList<>();
        params.add(transaction);
        params.add(simulateTransactionConfig);

        SimulatedTransaction simulatedTransaction = client.call(
                "simulateTransaction",
                params,
                SimulatedTransaction.class
        );

        return simulatedTransaction;
    }


    public List<ClusterNode> getClusterNodes() throws RpcException {
        List<Object> params = new ArrayList<>();

        // TODO - fix uncasted type stuff
        List<AbstractMap> rawResult = client.call("getClusterNodes", params, List.class);

        List<ClusterNode> result = new ArrayList<>();
        for (AbstractMap item : rawResult) {
            result.add(new ClusterNode(item));
        }

        return result;
    }

    /**
     * Returns identity and transaction information about a confirmed block in the ledger
     * DEPRECATED: use getBlock instead
     */
    @Deprecated
    public ConfirmedBlock getConfirmedBlock(int slot) throws RpcException {
        List<Object> params = new ArrayList<>();

        params.add(slot);
        params.add(new BlockConfig());

        return client.call("getConfirmedBlock", params, ConfirmedBlock.class);
    }

    /**
     * Returns identity and transaction information about a confirmed block in the ledger
     */
    public Block getBlock(int slot) throws RpcException {
        return getBlock(slot, null);
    }

    public Block getBlock(int slot, Map<String, Object> optionalParams) throws RpcException {
        List<Object> params = new ArrayList<>();

        params.add(slot);

        if (optionalParams != null) {
            BlockConfig blockConfig = new BlockConfig();
            if (optionalParams.containsKey("commitment")) {
                Commitment commitment = (Commitment) optionalParams.get("commitment");
                blockConfig.setCommitment(commitment.getValue());
            }

            if (optionalParams.containsKey("maxSupportedTransactionVersion")) {
                blockConfig.setMaxSupportedTransactionVersion((Integer) optionalParams.get("maxSupportedTransactionVersion"));
            }

            params.add(blockConfig);
        }

        return client.call("getBlock", params, Block.class);
    }

    /**
     * Returns information about the highest snapshot slot
     *
     * @throws RpcException
     */
    public SnapshotSlot getHighestSnapshotSlot() throws RpcException {
        List<Object> params = new ArrayList<>();
        return client.call("getHighestSnapshotSlot", params, SnapshotSlot.class);
    }

    /**
     * Returns information about the current epoch
     * @return
     * @throws RpcException
     */
    public EpochInfo getEpochInfo() throws RpcException {
        return getEpochInfo(null);
    }

    public EpochInfo getEpochInfo(Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();

        if (commitment != null) {
            params.add(Map.of("commitment", commitment.getValue()));
        }

        return client.call("getEpochInfo", params, EpochInfo.class);
    }

    public EpochSchedule getEpochSchedule() throws RpcException {
        List<Object> params = new ArrayList<>();

        return client.call("getEpochSchedule", params, EpochSchedule.class);
    }

    public PublicKey getTokenAccountsByOwner(PublicKey owner, PublicKey tokenMint) throws RpcException {
        List<Object> params = new ArrayList<>();
        params.add(owner.toBase58());

        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("mint", tokenMint.toBase58());
        params.add(parameterMap);
        params.add(Map.of("encoding", "jsonParsed"));

        Map<String, Object> rawResult = client.call("getTokenAccountsByOwner", params, Map.class);

        PublicKey tokenAccountKey;

        try {
            String base58 = (String) ((Map) ((List) rawResult.get("value")).get(0)).get("pubkey");
            tokenAccountKey = new PublicKey(base58);

        } catch (Exception ex) {
            throw new RpcException("unable to get token account by owner");
        }

        return tokenAccountKey;
    }

    public InflationRate getInflationRate() throws RpcException {
        return client.call("getInflationRate", new ArrayList<>(), InflationRate.class);
    }

    public InflationGovernor getInflationGovernor() throws RpcException {
        return getInflationGovernor(null);
    }

    public InflationGovernor getInflationGovernor(Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();

        if (commitment != null) {
            params.add(Map.of("commitment", commitment.getValue()));
        }

        return client.call("getInflationGovernor", params, InflationGovernor.class);
    }

    public List<InflationReward> getInflationReward(List<PublicKey> addresses) throws RpcException {
        return getInflationReward(addresses, null, null);
    }

    public List<InflationReward> getInflationReward(List<PublicKey> addresses, Long epoch, Commitment commitment)
            throws RpcException {
        List<Object> params = new ArrayList<>();

        params.add(addresses.stream().map(PublicKey::toString).collect(Collectors.toList()));

        RpcEpochConfig rpcEpochConfig = new RpcEpochConfig();
        if (epoch != null) {
            rpcEpochConfig.setEpoch(epoch);
        }
        if (commitment != null) {
            rpcEpochConfig.setCommitment(commitment.getValue());
        }
        params.add(rpcEpochConfig);

        List<AbstractMap> rawResult = client.call("getInflationReward", params, List.class);

        List<InflationReward> result = new ArrayList<>();
        for (AbstractMap item : rawResult) {
            if (item != null) {
                result.add(new InflationReward(item));
            }
        }

        return result;
    }

    public long getSlot() throws RpcException {
        return getSlot(null);
    }

    public long getSlot(Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();

        if (commitment != null) {
            params.add(Map.of("commitment", commitment.getValue()));
        }

        return client.call("getSlot", params, Long.class);
    }

    public PublicKey getSlotLeader() throws RpcException {
        return getSlotLeader(null);
    }

    public PublicKey getSlotLeader(Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();

        if (commitment != null) {
            params.add(Map.of("commitment", commitment.getValue()));
        }

        return new PublicKey(client.call("getSlotLeader", params, String.class));
    }

    public List<PublicKey> getSlotLeaders(long startSlot, long limit) throws RpcException {
        List<Object> params = new ArrayList<>();

        params.add(startSlot);
        params.add(limit);

        List<String> rawResult = client.call("getSlotLeaders", params, List.class);

        List<PublicKey> result = new ArrayList<>();
        for (String item : rawResult) {
            result.add(new PublicKey(item));
        }

        return result;
    }

    @Deprecated
    public long getSnapshotSlot() throws RpcException {
        return client.call("getSnapshotSlot", new ArrayList<>(), Long.class);
    }

    public long getMaxShredInsertSlot() throws RpcException {
        return client.call("getMaxShredInsertSlot", new ArrayList<>(), Long.class);
    }

    public PublicKey getIdentity() throws RpcException {
        Map<String, Object> rawResult = client.call("getIdentity", new ArrayList<>(), Map.class);

        PublicKey identity;
        try {
            String base58 = (String) rawResult.get("identity");
            identity = new PublicKey(base58);

        } catch (Exception ex) {
            throw new RpcException("unable to get identity");
        }

        return identity;
    }

    public Supply getSupply() throws RpcException {
        return getSupply(null);
    }

    public Supply getSupply(Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();

        if (commitment != null) {
            params.add(Map.of("commitment", commitment.getValue()));
        }

        return client.call("getSupply", params, Supply.class);
    }

    public long getFirstAvailableBlock() throws RpcException {
        return client.call("getFirstAvailableBlock", new ArrayList<>(), Long.class);
    }

    public String getGenesisHash() throws RpcException {
        return client.call("getGenesisHash", new ArrayList<>(), String.class);
    }

    /**
     * Returns a list of confirmed blocks between two slots
     * DEPRECATED: use getBlocks instead
     */
    @Deprecated
    public List<Double> getConfirmedBlocks(Integer start, Integer end) throws RpcException {
        List<Object> params;
        params = (end == null ? Arrays.asList(start) : Arrays.asList(start, end));
        return this.client.call("getConfirmedBlocks", params, List.class);
    }
    /**
     * Returns a list of confirmed blocks between two slots
     * DEPRECATED: use getBlocks instead
     */
    @Deprecated
    public List<Double> getConfirmedBlocks(Integer start) throws RpcException {
        return this.getConfirmedBlocks(start, null);
    }

    public TokenResultObjects.TokenAmountInfo getTokenAccountBalance(PublicKey tokenAccount) throws RpcException {
        return getTokenAccountBalance(tokenAccount, null);
    }

    public TokenResultObjects.TokenAmountInfo getTokenAccountBalance(PublicKey tokenAccount, Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();
        params.add(tokenAccount.toString());

        if (commitment != null) {
            params.add(Map.of("commitment", commitment.getValue()));
        }

        Map<String, Object> rawResult = client.call("getTokenAccountBalance", params, Map.class);

        return new TokenAmountInfo((AbstractMap) rawResult.get("value"));
    }

    public TokenAmountInfo getTokenSupply(PublicKey tokenMint) throws RpcException {
        return getTokenSupply(tokenMint, null);
    }

    public TokenAmountInfo getTokenSupply(PublicKey tokenMint, Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();
        params.add(tokenMint.toString());

        if (null != commitment) {
            params.add(Map.of("commitment", commitment.getValue()));
        }

        Map<String, Object> rawResult =  client.call("getTokenSupply", params, Map.class);

        return new TokenAmountInfo((AbstractMap) rawResult.get("value"));
    }

    public List<TokenAccount> getTokenLargestAccounts(PublicKey tokenMint) throws RpcException {
        return getTokenLargestAccounts(tokenMint, null);
    }

    public List<TokenAccount> getTokenLargestAccounts(PublicKey tokenMint, Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();
        params.add(tokenMint.toString());

        if (null != commitment) {
            params.add(Map.of("commitment", commitment.getValue()));
        }

        Map<String, Object> rawResult = client.call("getTokenLargestAccounts", params, Map.class);

        List<TokenAccount> result = new ArrayList<>();
        for (AbstractMap item : (List<AbstractMap>) rawResult.get("value")) {
            result.add(new TokenAccount(item));
        }

        return result;
    }

    public TokenAccountInfo getTokenAccountsByOwner(PublicKey accountOwner, Map<String, Object> requiredParams,
            Map<String, Object> optionalParams) throws RpcException {
        return getTokenAccount(accountOwner, requiredParams, optionalParams, "getTokenAccountsByOwner");
    }

    public TokenAccountInfo getTokenAccountsByDelegate(PublicKey accountDelegate, Map<String, Object> requiredParams,
            Map<String, Object> optionalParams) throws RpcException {
        return getTokenAccount(accountDelegate, requiredParams, optionalParams, "getTokenAccountsByDelegate");
    }

    private TokenAccountInfo getTokenAccount(PublicKey account, Map<String, Object> requiredParams,
            Map<String, Object> optionalParams, String method) throws RpcException {
        List<Object> params = new ArrayList<>();
        params.add(account.toString());

        // Either mint or programId is required
        Map<String, Object> parameterMap = new HashMap<>();
        if (requiredParams.containsKey("mint")) {
            parameterMap.put("mint", requiredParams.get("mint").toString());
        } else if (requiredParams.containsKey("programId")) {
            parameterMap.put("programId", requiredParams.get("programId").toString());
        } else {
            throw new RpcException("mint or programId are mandatory parameters");
        }
        params.add(parameterMap);

        if (null != optionalParams) {
            parameterMap = new HashMap<>();
            parameterMap.put("encoding", optionalParams.getOrDefault("encoding", "jsonParsed"));
            if (optionalParams.containsKey("commitment")) {
                Commitment commitment = (Commitment) optionalParams.get("commitment");
                parameterMap.put("commitment", commitment.getValue());
            }
            if (optionalParams.containsKey("dataSlice")) {
                parameterMap.put("dataSlice", optionalParams.get("dataSlice"));
            }
            params.add(parameterMap);
        }

        return client.call(method, params, TokenAccountInfo.class);
    }

    public VoteAccounts getVoteAccounts() throws RpcException {
        return getVoteAccounts(null, null);
    }

    public VoteAccounts getVoteAccounts(PublicKey votePubkey, Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();

        VoteAccountConfig voteAccountConfig = new VoteAccountConfig();
        if (votePubkey != null) {
            voteAccountConfig.setVotePubkey(votePubkey.toBase58());
        }
        if (commitment != null) {
            voteAccountConfig.setCommitment(commitment.getValue());
        }
        params.add(voteAccountConfig);

        return client.call("getVoteAccounts", params, VoteAccounts.class);
    }

    @Deprecated
    public StakeActivation getStakeActivation(PublicKey publicKey) throws RpcException {
        return getStakeActivation(publicKey, null, null);
    }

    @Deprecated
    public StakeActivation getStakeActivation(PublicKey publicKey, Long epoch, Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();
        params.add(publicKey.toBase58());

        RpcEpochConfig rpcEpochConfig = new RpcEpochConfig();
        if (null != epoch) {
            rpcEpochConfig.setEpoch(epoch);
        }
        if (null != commitment) {
            rpcEpochConfig.setCommitment(commitment.getValue());
        }
        params.add(rpcEpochConfig);

        return client.call("getStakeActivation", params, StakeActivation.class);
    }

    public SignatureStatuses getSignatureStatuses(List<String> signatures, boolean searchTransactionHistory)
            throws RpcException {
        List<Object> params = new ArrayList<>();
        params.add(signatures);
        params.add(new SignatureStatusConfig(searchTransactionHistory));

        return client.call("getSignatureStatuses", params, SignatureStatuses.class);
    }

    public List<PerformanceSample> getRecentPerformanceSamples() throws RpcException {
        List<Object> params = new ArrayList<>();

        List<AbstractMap> rawResult = client.call("getRecentPerformanceSamples", params, List.class);

        List<PerformanceSample> result = new ArrayList<>();
        for (AbstractMap item : rawResult) {
            result.add(new PerformanceSample(item));
        }

        return result;
    }

    public List<PerformanceSample> getRecentPerformanceSamples(int limit) throws RpcException {
        List<Object> params = new ArrayList<>();
        params.add(limit);

        List<AbstractMap> rawResult = client.call("getRecentPerformanceSamples", params, List.class);

        List<PerformanceSample> result = new ArrayList<>();
        for (AbstractMap item : rawResult) {
            result.add(new PerformanceSample(item));
        }

        return result;
    }

    // Throws an exception if not healthy
    public boolean getHealth() throws RpcException {
        List<Object> params = new ArrayList<>();
        String result = client.call("getHealth", params, String.class);
        return result.equals("ok");
    }

    public List<LargeAccount> getLargestAccounts() throws RpcException {
        return getLargestAccounts(null, null);
    }

    public List<LargeAccount> getLargestAccounts(String filter, Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();

        LargestAccountConfig largestAccountConfig = new LargestAccountConfig();
        if (null != filter) {
            largestAccountConfig.setFilter(filter);
        }
        if (null != commitment) {
            largestAccountConfig.setCommitment(commitment.getValue());
        }
        params.add(largestAccountConfig);

        Map<String, Object> rawResult = client.call("getLargestAccounts", params, Map.class);

        List<LargeAccount> result = new ArrayList<>();
        for (AbstractMap item : (List<AbstractMap>) rawResult.get("value")) {
            result.add(new LargeAccount(item));
        }

        return result;
    }

    public List<LeaderSchedule> getLeaderSchedule() throws RpcException {
        return getLeaderSchedule(null, null, null);
    }

    public List<LeaderSchedule> getLeaderSchedule(Long epoch, String identity, Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();

        if (null != epoch) {
            params.add(epoch);
        }

        LeaderScheduleConfig leaderScheduleConfig = new LeaderScheduleConfig();
        if (null != identity) {
            leaderScheduleConfig.setIdentity(identity);
        }
        if (null != commitment) {
            leaderScheduleConfig.setCommitment(commitment.getValue());
        }
        params.add(leaderScheduleConfig);

        Map<String, Object> rawResult = client.call("getLeaderSchedule", params, Map.class);

        List<LeaderSchedule> result = new ArrayList<>();
        rawResult.forEach((key, value) -> {
            result.add(new LeaderSchedule(key, (List<Double>) value));
        });

        return result;
    }

    public List<AccountInfo.Value> getMultipleAccounts(List<PublicKey> publicKeys) throws RpcException {
        return getMultipleAccounts(publicKeys, new HashMap<>());
    }

    public List<AccountInfo.Value> getMultipleAccounts(List<PublicKey> publicKeys, Map<String, Object> additionalParams) throws RpcException {
        List<Object> params = new ArrayList<>();
        params.add(publicKeys.stream().map(PublicKey::toBase58).collect(Collectors.toList()));

        Map<String, Object> parameterMap = new HashMap<>();

        parameterMap.put("encoding", additionalParams.getOrDefault("encoding", "base64"));

        if (additionalParams.containsKey("commitment")) {
            Commitment commitment = (Commitment) additionalParams.get("commitment");
            parameterMap.put("commitment", commitment.getValue());
        }
        if (additionalParams.containsKey("dataSlice")) {
            parameterMap.put("dataSlice", additionalParams.get("dataSlice"));
        }

        params.add(parameterMap);

        Map<String, Object> rawResult = client.call("getMultipleAccounts", params, Map.class);
        List<AccountInfo.Value> result = new ArrayList<>();

        for (AbstractMap item : (List<AbstractMap>) rawResult.get("value")) {
            if (item != null) {
                result.add(new AccountInfo.Value(item));
            }
        }

        return result;
    }

    public Map<PublicKey, Optional<AccountInfo.Value>> getMultipleAccountsMap(List<PublicKey> publicKeys) throws RpcException {
        List<Object> params = new ArrayList<>();
        Map<PublicKey, Optional<AccountInfo.Value>> result = new HashMap<>();
        params.add(publicKeys.stream().map(PublicKey::toBase58).collect(Collectors.toList()));

        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("encoding", "base64");
        params.add(parameterMap);

        Map<String, Object> rawResult = client.call("getMultipleAccounts", params, Map.class);

        List<AbstractMap<String, Object>> resultList = (List<AbstractMap<String, Object>>) rawResult.get("value");
        for (int i = 0; i < resultList.size(); i++) {
            if (resultList.get(i) == null) {
                result.put(publicKeys.get(i), Optional.empty());
            } else {
                result.put(publicKeys.get(i), Optional.of(new AccountInfo.Value(resultList.get(i))));
            }
        }

        return result;
    }

    public boolean isBlockhashValid(String blockHash) throws RpcException {
        return isBlockhashValid(blockHash, null, null);
    }

    @SuppressWarnings("unchecked")
    public boolean isBlockhashValid(String blockHash, Commitment commitment, Long minContextSlot) throws RpcException {
        Map<String, Object> parameterMap = new HashMap<>();
        if (commitment != null) {
            parameterMap.put("commitment", commitment);
        }

        if (minContextSlot != null) {
            parameterMap.put("minContextSlot", minContextSlot);
        }
        List<Object> params = new ArrayList<>();
        params.add(blockHash);
        params.add(parameterMap);

        Map<Object, Object> call = client.call("isBlockhashValid", params, Map.class);
        Boolean result = (Boolean) call.get("value");

        return result;
    }

    /**
     * Returns a list of confirmed blocks between two slots
     *
     * @param startSlot Start slot (inclusive)
     * @param endSlot End slot (inclusive)
     * @return List of block numbers between start_slot and end_slot
     * @throws RpcException if the RPC call fails
     */
    public List<Long> getBlocks(long startSlot, long endSlot) throws RpcException {
        return getBlocks(startSlot, endSlot, null);
    }

    /**
     * Returns a list of confirmed blocks between two slots
     *
     * @param startSlot Start slot (inclusive)
     * @param endSlot End slot (inclusive)
     * @param commitment Bank state to query
     * @return List of block numbers between start_slot and end_slot
     * @throws RpcException if the RPC call fails
     */
    public List<Long> getBlocks(long startSlot, long endSlot, Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();
        params.add(startSlot);
        params.add(endSlot);

        if (commitment != null) {
            params.add(Map.of("commitment", commitment.getValue()));
        }

        List<Double> result = client.call("getBlocks", params, List.class);
        return result.stream().map(Double::longValue).collect(Collectors.toList());
    }

    /**
     * Returns a list of confirmed blocks starting at the given slot
     *
     * @param startSlot Start slot
     * @param limit Maximum number of blocks to return
     * @return List of block numbers from start_slot to limit
     * @throws RpcException if the RPC call fails
     */
    public List<Long> getBlocksWithLimit(long startSlot, long limit) throws RpcException {
        return getBlocksWithLimit(startSlot, limit, null);
    }

    /**
     * Returns a list of confirmed blocks starting at the given slot
     *
     * @param startSlot Start slot
     * @param limit Maximum number of blocks to return
     * @param commitment Bank state to query
     * @return List of block numbers from start_slot to limit
     * @throws RpcException if the RPC call fails
     */
    public List<Long> getBlocksWithLimit(long startSlot, long limit, Commitment commitment) throws RpcException {
        List<Object> params = new ArrayList<>();
        params.add(startSlot);
        params.add(limit);

        if (commitment != null) {
            params.add(Map.of("commitment", commitment.getValue()));
        }

        List<Double> result = client.call("getBlocksWithLimit", params, List.class);
        return result.stream().map(Double::longValue).collect(Collectors.toList());
    }

}
