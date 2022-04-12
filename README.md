# solanaj


Solana blockchain client, written in pure Java.
Solanaj is an API for integrating with Solana blockchain using the [Solana RPC API](https://docs.solana.com/apps/jsonrpc-api)

This fork includes functionality for multiple Solana programs, including the Serum DEX.

# solanaj-programs
For SolanaJ implementations of popular Solana programs such as Serum, please visit: https://github.com/skynetcap/solanaj-programs

## Requirements
- Java 11+

## Dependencies
- bitcoinj
- OkHttp
- Moshi

## Installation
1. Build SolanaJ:
   `mvn clean install`
2. To use in another project, add this dependency to Maven:
    ```xml
    <dependency>
            <groupId>org.p2p</groupId>
            <artifactId>solanaj</artifactId>
            <version>1.6-SNAPSHOT</version>
    </dependency>
    ```

## Example
##### Transfer lamports

```java
RpcClient client = new RpcClient(Cluster.TESTNET);

PublicKey fromPublicKey = new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo");
PublicKey toPublickKey = new PublicKey("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5");
int lamports = 3000;

Account signer = new Account(secret_key);

Transaction transaction = new Transaction();
transaction.addInstruction(SystemProgram.transfer(fromPublicKey, toPublickKey, lamports));

String signature = client.getApi().sendTransaction(transaction, signer);
```

##### Get balance

```java
RpcClient client = new RpcClient(Cluster.TESTNET);

long balance = client.getApi().getBalance(new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo"));
```

##### Get Serum market + orderbooks
```java
final PublicKey solUsdcPublicKey = new PublicKey("7xMDbYTCqQEcK2aM9LbetGtNFJpzKdfXzLL5juaLh4GJ");
final Market solUsdcMarket = new MarketBuilder()
        .setClient(new RpcClient())
        .setPublicKey(solUsdcPublicKey)
        .setRetrieveOrderBooks(true)
        .build();

final OrderBook bids = solUsdcMarket.getBidOrderBook();
```

##### Send a transaction with call to the "Memo" program
```java
// Create account from private key
final Account feePayer = new Account(Base58.decode(new String(data)));
final Transaction transaction = new Transaction();

// Add instruction to write memo
transaction.addInstruction(
        MemoProgram.writeUtf8(feePayer.getPublicKey(),"Hello from SolanaJ :)")
);

String response = result = client.getApi().sendTransaction(transaction, feePayer);
```

## Contribution

Welcome to contribute, feel free to change and open a PR.


## License

Solanaj is available under the MIT license. See the LICENSE file for more info.
