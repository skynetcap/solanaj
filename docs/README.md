# SolanaJ

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-17%2B-blue)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Maven Central](https://img.shields.io/maven-central/v/com.mmorrell/solanaj.svg)](https://search.maven.org/artifact/com.mmorrell/solanaj)
[![Solana](https://img.shields.io/badge/Solana-Compatible-blueviolet)](https://solana.com/)
[![Java](https://img.shields.io/badge/Pure-Java-orange)](https://www.java.com/)
[![Documentation](https://img.shields.io/badge/API-Documentation-lightgrey)](https://docs.solana.com/apps/jsonrpc-api)
[![Discord](https://img.shields.io/discord/889577356681945098?color=blueviolet)](https://discord.gg/solana)
[![GitHub Stars](https://img.shields.io/github/stars/skynetcap/solanaj?style=social)](https://github.com/skynetcap/solanaj)

Solana blockchain client, written in pure Java. SolanaJ is an API for integrating with Solana blockchain using the [Solana RPC API](https://docs.solana.com/apps/jsonrpc-api).

This fork includes functionality for multiple Solana programs, including the Serum DEX.

## Table of Contents

- [SolanaJ-Programs](#solanaj-programs)
- [Requirements](#%EF%B8%8F-requirements)
- [Dependencies](#-dependencies)
- [Installation](#-installation)
- [Build](#%EF%B8%8F-build)
- [Examples](#-examples)
    - [Transfer Lamports](#transfer-lamports)
    - [Get Balance](#get-balance)
    - [Get Serum Market + Orderbooks](#get-serum-market--orderbooks)
    - [Send a Transaction with Memo Program](#send-a-transaction-with-memo-program)
- [Contributing](#-contributing)
- [License](#-license)

## SolanaJ-Programs

For SolanaJ implementations of popular Solana programs such as Serum, please visit: [https://github.com/skynetcap/solanaj-programs](https://github.com/skynetcap/solanaj-programs)

## 🛠️ Requirements

- Java 17+

## 📚 Dependencies

- bitcoinj
- OkHttp
- Moshi

## 📦 Installation

Add the following Maven dependency to your project's `pom.xml`:

```xml
<dependency>
    <groupId>com.mmorrell</groupId>
    <artifactId>solanaj</artifactId>
    <version>1.19.2</version>
</dependency>
```

## 🏗️ Build

1. In `pom.xml`, update the `maven-gpg-plugin` configuration with your homedir and keyname:

```xml
<configuration>
    <homedir>/home/your_username/.gnupg/</homedir>
    <keyname>YOUR_GPG_KEY_ID</keyname>
</configuration>
```

2. Check if you have a GPG key:

```sh
gpg --list-secret-keys
```

3. If no key is returned, create one:

```sh
gpg --full-generate-key
```

4. Run the Maven install command:

```sh
mvn install
```

The build should complete successfully.

## 🚀 Examples

### Transfer Lamports

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

### Get Balance

```java
RpcClient client = new RpcClient(Cluster.TESTNET);

long balance = client.getApi().getBalance(new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo"));
```

### Get Serum Market + Orderbooks

```java
final PublicKey solUsdcPublicKey = new PublicKey("7xMDbYTCqQEcK2aM9LbetGtNFJpzKdfXzLL5juaLh4GJ");
final Market solUsdcMarket = new MarketBuilder()
        .setClient(new RpcClient())
        .setPublicKey(solUsdcPublicKey)
        .setRetrieveOrderBooks(true)
        .build();

final OrderBook bids = solUsdcMarket.getBidOrderBook();
```

### Send a Transaction with Memo Program

```java
// Create account from private key
final Account feePayer = new Account(Base58.decode(new String(data)));
final Transaction transaction = new Transaction();

// Add instruction to write memo
transaction.addInstruction(
        MemoProgram.writeUtf8(feePayer.getPublicKey(),"Hello from SolanaJ :)")
);

String response = client.getApi().sendTransaction(transaction, feePayer);
```

## 🤝 Contributing

We welcome contributions to SolanaJ! Here's how you can help:

1. Fork the repository
2. Create a new branch (`git checkout -b feature/your-feature-name`)
3. Make your changes
4. Commit your changes (`git commit -am 'Add some feature'`)
5. Push to the branch (`git push origin feature/your-feature-name`)
6. Create a new Pull Request

Please make sure to update tests as appropriate and adhere to the existing coding style.

## 📄 License

SolanaJ is open-source software licensed under the [MIT License](LICENSE). See the LICENSE file for more details.