# SolanaJ

<div align="center">

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-17%2B-blue)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Maven Central](https://img.shields.io/maven-central/v/com.mmorrell/solanaj.svg)](https://search.maven.org/artifact/com.mmorrell/solanaj)
[![Solana](https://img.shields.io/badge/Solana-Compatible-blueviolet)](https://solana.com/)
[![Java](https://img.shields.io/badge/Pure-Java-orange)](https://www.java.com/)
[![GitHub Stars](https://img.shields.io/github/stars/skynetcap/solanaj?style=social)](https://github.com/skynetcap/solanaj)

**A comprehensive, production-ready Java SDK for Solana blockchain development**

</div>

---

## 📋 Overview

**SolanaJ** is a robust, enterprise-grade Java client library designed for seamless integration with the Solana blockchain ecosystem. Built entirely in pure Java, this SDK provides developers with a complete suite of tools to interact with Solana's high-performance blockchain through its [JSON RPC API](https://docs.solana.com/apps/jsonrpc-api).

This carefully maintained fork extends the original SolanaJ implementation with comprehensive support for multiple Solana on-chain programs, including sophisticated integrations with the Serum decentralized exchange (DEX), SPL Token operations, and various system programs. Whether you're building decentralized finance (DeFi) applications, NFT platforms, or custom blockchain solutions, SolanaJ provides the foundational infrastructure required for professional Solana development in the Java ecosystem.

### ✨ Key Features

- 🚀 **High-Performance RPC Client** - Optimized for low-latency interactions with Solana nodes
- 🔐 **Comprehensive Cryptographic Support** - Full Ed25519 signature implementation with secure key management
- 💼 **Transaction Management** - Complete transaction building, signing, and submission workflows
- 📊 **Program Integration** - Native support for System Program, SPL Token, Memo, and Serum DEX
- 🔄 **Real-Time Updates** - WebSocket support for account and program subscription events
- ⚡ **Production Ready** - Battle-tested in production environments with extensive test coverage
- 🧪 **Developer Friendly** - Intuitive API design with comprehensive documentation and examples

---

## 📑 Table of Contents

- [Overview](#-overview)
- [Requirements](#%EF%B8%8F-requirements)
- [Installation](#-installation)
- [Core Dependencies](#-core-dependencies)
- [Quick Start](#-quick-start)
- [Comprehensive Examples](#-comprehensive-examples)
  - [Account Management & Balance Queries](#1-account-management--balance-queries)
  - [Native SOL Transfers](#2-native-sol-transfers)
  - [SPL Token Operations](#3-spl-token-operations)
  - [Transaction Building & Submission](#4-transaction-building--submission)
  - [Memo Program Integration](#5-memo-program-integration)
  - [Serum DEX Market Data](#6-serum-dex-market-data)
  - [Advanced RPC Operations](#7-advanced-rpc-operations)
  - [WebSocket Subscriptions](#8-websocket-subscriptions)
- [Extended Program Support](#-extended-program-support)
- [Architecture & Design](#-architecture--design)
- [Contributing](#-contributing)
- [Support & Community](#-support--community)
- [License](#-license)

---

## 🛠️ Requirements

SolanaJ is designed to work with modern Java development environments and requires the following:

- **Java Development Kit (JDK)**: Version 17 or higher
- **Build Tool**: Maven 3.6+ or Gradle 7.0+ (for dependency management)
- **Operating System**: Cross-platform compatible (Windows, macOS, Linux)

---

## 📦 Installation

### Maven Installation

To integrate SolanaJ into your Maven project, add the following dependency declaration to your project's `pom.xml` file:

```xml
<dependency>
    <groupId>com.mmorrell</groupId>
    <artifactId>solanaj</artifactId>
    <version>1.27.2</version>
</dependency>
```

### Gradle Installation

For Gradle-based projects, add the following to your `build.gradle` file:

```gradle
dependencies {
    implementation 'com.mmorrell:solanaj:1.26.0'
}
```

### Manual Installation

Alternatively, you can build the library from source:

```bash
git clone https://github.com/skynetcap/solanaj.git
cd solanaj
mvn clean install
```

---

## 📚 Core Dependencies

SolanaJ leverages industry-standard libraries to provide reliable and secure blockchain interactions:

| Dependency | Purpose | Version |
|------------|---------|---------|
| **BitcoinJ** | Cryptographic primitives and key management | Latest |
| **OkHttp** | High-performance HTTP client for RPC communication | 4.x |
| **Moshi** | Modern JSON serialization and deserialization | 1.x |
| **Lombok** | Boilerplate code reduction and cleaner syntax | Latest |

All dependencies are managed automatically through Maven and will be resolved during the installation process.

---

## 🚀 Quick Start

Get up and running with SolanaJ in under a minute:

```java
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.core.PublicKey;

public class SolanaQuickStart {
    public static void main(String[] args) {
        // Initialize RPC client connected to Solana mainnet
        RpcClient client = new RpcClient(Cluster.MAINNET);
        
        // Query account balance
        PublicKey wallet = new PublicKey("YourWalletAddressHere");
        long balance = client.getApi().getBalance(wallet);
        
        System.out.printf("Wallet balance: %.9f SOL%n", balance / 1_000_000_000.0);
    }
}
```

---

## 💡 Comprehensive Examples

### 1. Account Management & Balance Queries

Efficiently query account balances and retrieve detailed account information from the Solana blockchain:

```java
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.Account;

public class AccountManagement {
    public static void main(String[] args) {
        // Connect to Solana testnet for development purposes
        RpcClient client = new RpcClient(Cluster.TESTNET);
        
        // Query the lamport balance of a specific account
        PublicKey accountAddress = new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo");
        long balanceInLamports = client.getApi().getBalance(accountAddress);
        
        // Convert lamports to SOL (1 SOL = 1,000,000,000 lamports)
        double balanceInSol = balanceInLamports / 1_000_000_000.0;
        
        System.out.printf("Account: %s%n", accountAddress.toBase58());
        System.out.printf("Balance: %,d lamports (%.9f SOL)%n", balanceInLamports, balanceInSol);
        
        // Generate a new keypair for account creation
        Account newAccount = new Account();
        System.out.printf("New account generated: %s%n", newAccount.getPublicKey().toBase58());
        System.out.printf("Private key (keep secure!): %s%n", 
            Arrays.toString(newAccount.getSecretKey()));
    }
}
```

### 2. Native SOL Transfers

Execute secure SOL transfers between accounts with proper transaction construction and signing:

```java
import org.p2p.solanaj.core.*;
import org.p2p.solanaj.programs.SystemProgram;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.Cluster;

public class SolTransfer {
    public static void main(String[] args) throws RpcException {
        // Initialize client connection to Solana testnet
        RpcClient client = new RpcClient(Cluster.TESTNET);
        
        // Define sender and recipient public keys
        PublicKey senderPublicKey = new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo");
        PublicKey recipientPublicKey = new PublicKey("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5");
        
        // Specify transfer amount (in lamports: 1 SOL = 1,000,000,000 lamports)
        long transferAmountLamports = 5_000_000; // 0.005 SOL
        
        // Initialize the sender's account with their private key
        // SECURITY WARNING: Never hardcode private keys in production!
        byte[] privateKey = /* your 64-byte private key */;
        Account signerAccount = new Account(privateKey);
        
        // Construct a new transaction
        Transaction transferTransaction = new Transaction();
        
        // Add a transfer instruction using the System Program
        transferTransaction.addInstruction(
            SystemProgram.transfer(senderPublicKey, recipientPublicKey, transferAmountLamports)
        );
        
        // Submit the signed transaction to the network
        String transactionSignature = client.getApi().sendTransaction(transferTransaction, signerAccount);
        
        System.out.printf("Transaction submitted successfully!%n");
        System.out.printf("Signature: %s%n", transactionSignature);
        System.out.printf("View on Solana Explorer: https://explorer.solana.com/tx/%s?cluster=testnet%n", 
            transactionSignature);
    }
}
```

### 3. SPL Token Operations

Interact with SPL tokens for transfers, balance queries, and account management:

```java
import org.p2p.solanaj.core.*;
import org.p2p.solanaj.programs.TokenProgram;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.types.TokenAccountInfo;

public class TokenOperations {
    public static void main(String[] args) throws RpcException {
        RpcClient client = new RpcClient(Cluster.MAINNET);
        
        // USDC token mint address on Solana mainnet
        PublicKey usdcMintAddress = new PublicKey("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v");
        
        // Query all token accounts for a specific wallet
        PublicKey walletAddress = new PublicKey("YourWalletAddressHere");
        List<TokenAccountInfo> tokenAccounts = client.getApi()
            .getTokenAccountsByOwner(walletAddress, usdcMintAddress);
        
        System.out.printf("Found %d USDC token account(s)%n", tokenAccounts.size());
        
        for (TokenAccountInfo account : tokenAccounts) {
            System.out.printf("Token Account: %s%n", account.getPublicKey());
            System.out.printf("Balance: %s USDC%n", account.getAmount());
        }
        
        // Transfer SPL tokens between accounts
        PublicKey sourceTokenAccount = new PublicKey("SourceTokenAccountAddress");
        PublicKey destinationTokenAccount = new PublicKey("DestinationTokenAccountAddress");
        Account owner = new Account(/* private key */);
        long tokenAmount = 1000000; // Amount in token's smallest unit
        
        Transaction tokenTransfer = new Transaction();
        tokenTransfer.addInstruction(
            TokenProgram.transfer(
                sourceTokenAccount,
                destinationTokenAccount,
                owner.getPublicKey(),
                tokenAmount
            )
        );
        
        String signature = client.getApi().sendTransaction(tokenTransfer, owner);
        System.out.printf("Token transfer signature: %s%n", signature);
    }
}
```

### 4. Transaction Building & Submission

Construct complex multi-instruction transactions with advanced configuration options:

```java
import org.p2p.solanaj.core.*;
import org.p2p.solanaj.programs.*;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.Cluster;

public class AdvancedTransactions {
    public static void main(String[] args) throws RpcException {
        RpcClient client = new RpcClient(Cluster.MAINNET);
        Account feePayer = new Account(/* private key */);
        
        // Build a complex transaction with multiple instructions
        Transaction complexTransaction = new Transaction();
        
        // Set the fee payer explicitly
        complexTransaction.setFeePayer(feePayer.getPublicKey());
        
        // Add multiple instructions atomically
        complexTransaction.addInstruction(
            SystemProgram.transfer(
                feePayer.getPublicKey(),
                new PublicKey("RecipientAddress1"),
                1_000_000
            )
        );
        
        complexTransaction.addInstruction(
            SystemProgram.transfer(
                feePayer.getPublicKey(),
                new PublicKey("RecipientAddress2"),
                2_000_000
            )
        );
        
        complexTransaction.addInstruction(
            MemoProgram.writeUtf8(feePayer.getPublicKey(), "Multi-instruction transaction")
        );
        
        // Retrieve a recent blockhash for transaction freshness
        String recentBlockhash = client.getApi().getRecentBlockhash();
        complexTransaction.setRecentBlockHash(recentBlockhash);
        
        // Sign the transaction with all required signers
        complexTransaction.sign(feePayer);
        
        // Submit with custom RPC configuration
        String signature = client.getApi().sendTransaction(complexTransaction, feePayer);
        
        System.out.printf("Complex transaction submitted: %s%n", signature);
        
        // Wait for confirmation
        boolean confirmed = client.getApi().confirmTransaction(signature);
        System.out.printf("Transaction confirmed: %s%n", confirmed);
    }
}
```

### 5. Memo Program Integration

Attach human-readable messages to your transactions using the Memo program:

```java
import org.p2p.solanaj.core.*;
import org.p2p.solanaj.programs.MemoProgram;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.utils.Base58Utils;

public class MemoExample {
    public static void main(String[] args) throws RpcException {
        RpcClient client = new RpcClient(Cluster.TESTNET);
        
        // Decode Base58-encoded private key
        byte[] privateKeyBytes = Base58Utils.decode("YourBase58EncodedPrivateKey");
        Account account = new Account(privateKeyBytes);
        
        // Create transaction with memo instruction
        Transaction memoTransaction = new Transaction();
        
        // Add UTF-8 encoded memo to the transaction
        String memoContent = "Payment for services rendered - Invoice #12345";
        memoTransaction.addInstruction(
            MemoProgram.writeUtf8(account.getPublicKey(), memoContent)
        );
        
        // Send the transaction
        String signature = client.getApi().sendTransaction(memoTransaction, account);
        
        System.out.printf("Memo transaction submitted successfully%n");
        System.out.printf("Transaction ID: %s%n", signature);
        System.out.printf("Memo content: \"%s\"%n", memoContent);
        System.out.printf("View transaction: https://explorer.solana.com/tx/%s?cluster=testnet%n", 
            signature);
    }
}
```

### 6. Serum DEX Market Data

Access real-time market data and orderbook information from Serum decentralized exchange:

```java
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.serum.model.*;

public class SerumMarketData {
    public static void main(String[] args) {
        // Initialize RPC client for mainnet
        RpcClient client = new RpcClient(Cluster.MAINNET);
        
        // SOL/USDC market public key on Serum
        PublicKey solUsdcMarketKey = new PublicKey("9wFFyRfZBsuAha4YcuxcXLKwMxJR43S7fPfQLusDBzvT");
        
        // Build market instance with full orderbook data
        Market solUsdcMarket = new MarketBuilder()
            .setClient(client)
            .setPublicKey(solUsdcMarketKey)
            .setRetrieveOrderBooks(true)
            .setRetrieveEventQueue(true)
            .build();
        
        // Access bid orderbook (buy orders)
        OrderBook bids = solUsdcMarket.getBidOrderBook();
        System.out.printf("=== BID ORDERBOOK (Top 5) ===%n");
        bids.getOrders().stream()
            .limit(5)
            .forEach(order -> {
                System.out.printf("Price: $%.4f | Size: %.4f SOL | Total: $%.2f%n",
                    order.getPrice(),
                    order.getSize(),
                    order.getPrice() * order.getSize()
                );
            });
        
        // Access ask orderbook (sell orders)
        OrderBook asks = solUsdcMarket.getAskOrderBook();
        System.out.printf("%n=== ASK ORDERBOOK (Top 5) ===%n");
        asks.getOrders().stream()
            .limit(5)
            .forEach(order -> {
                System.out.printf("Price: $%.4f | Size: %.4f SOL | Total: $%.2f%n",
                    order.getPrice(),
                    order.getSize(),
                    order.getPrice() * order.getSize()
                );
            });
        
        // Calculate spread
        double bestBid = bids.getBestBid().getPrice();
        double bestAsk = asks.getBestAsk().getPrice();
        double spread = bestAsk - bestBid;
        double spreadPercent = (spread / bestBid) * 100;
        
        System.out.printf("%n=== MARKET STATISTICS ===%n");
        System.out.printf("Best Bid: $%.4f%n", bestBid);
        System.out.printf("Best Ask: $%.4f%n", bestAsk);
        System.out.printf("Spread: $%.4f (%.3f%%)%n", spread, spreadPercent);
    }
}
```

### 7. Advanced RPC Operations

Leverage advanced RPC functionality for sophisticated blockchain queries:

```java
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.types.*;

public class AdvancedRpcQueries {
    public static void main(String[] args) throws RpcException {
        RpcClient client = new RpcClient(Cluster.MAINNET);
        
        // Get current slot and epoch information
        long currentSlot = client.getApi().getSlot();
        EpochInfo epochInfo = client.getApi().getEpochInfo();
        
        System.out.printf("Current Slot: %,d%n", currentSlot);
        System.out.printf("Epoch: %d | Slot Index: %,d / %,d%n",
            epochInfo.getEpoch(),
            epochInfo.getSlotIndex(),
            epochInfo.getSlotsInEpoch()
        );
        
        // Query transaction history for an account
        PublicKey account = new PublicKey("YourAccountAddress");
        List<TransactionSignature> signatures = client.getApi()
            .getSignaturesForAddress(account, 10); // Last 10 transactions
        
        System.out.printf("%nRecent transaction signatures:%n");
        signatures.forEach(sig -> {
            System.out.printf("- %s (Slot: %,d)%n", sig.getSignature(), sig.getSlot());
        });
        
        // Get detailed account information
        AccountInfo accountInfo = client.getApi().getAccountInfo(account);
        System.out.printf("%nAccount Details:%n");
        System.out.printf("Owner Program: %s%n", accountInfo.getOwner());
        System.out.printf("Lamports: %,d%n", accountInfo.getLamports());
        System.out.printf("Executable: %s%n", accountInfo.isExecutable());
        System.out.printf("Rent Epoch: %d%n", accountInfo.getRentEpoch());
        
        // Query program accounts
        PublicKey tokenProgramId = new PublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA");
        List<ProgramAccount> programAccounts = client.getApi()
            .getProgramAccounts(tokenProgramId);
        
        System.out.printf("%nTotal token accounts: %,d%n", programAccounts.size());
    }
}
```

### 8. WebSocket Subscriptions

Subscribe to real-time blockchain events and account changes:

```java
import org.p2p.solanaj.ws.SubscriptionWebSocketClient;
import org.p2p.solanaj.ws.listeners.*;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.Cluster;

public class WebSocketSubscriptions {
    public static void main(String[] args) {
        // Create WebSocket client for real-time subscriptions
        SubscriptionWebSocketClient wsClient = new SubscriptionWebSocketClient(Cluster.MAINNET);
        
        // Subscribe to account changes
        PublicKey accountToMonitor = new PublicKey("YourAccountAddress");
        
        wsClient.accountSubscribe(accountToMonitor, new NotificationEventListener() {
            @Override
            public void onNotificationEvent(Object data) {
                System.out.printf("Account update received: %s%n", data.toString());
                // Process account state changes in real-time
            }
            
            @Override
            public void onError(Throwable error) {
                System.err.printf("WebSocket error: %s%n", error.getMessage());
            }
        });
        
        // Subscribe to program account updates
        PublicKey programId = new PublicKey("ProgramIdToMonitor");
        
        wsClient.programSubscribe(programId, new NotificationEventListener() {
            @Override
            public void onNotificationEvent(Object data) {
                System.out.printf("Program account update: %s%n", data.toString());
            }
            
            @Override
            public void onError(Throwable error) {
                System.err.printf("Program subscription error: %s%n", error.getMessage());
            }
        });
        
        // Subscribe to signature confirmations
        String transactionSignature = "YourTransactionSignature";
        
        wsClient.signatureSubscribe(transactionSignature, new NotificationEventListener() {
            @Override
            public void onNotificationEvent(Object data) {
                System.out.printf("Transaction confirmed: %s%n", transactionSignature);
                // Handle confirmation
            }
            
            @Override
            public void onError(Throwable error) {
                System.err.printf("Signature subscription error: %s%n", error.getMessage());
            }
        });
        
        System.out.println("WebSocket subscriptions active. Listening for updates...");
        
        // Keep the application running to receive updates
        try {
            Thread.sleep(300000); // Run for 5 minutes
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            wsClient.close();
        }
    }
}
```

---

## 🔌 Extended Program Support

SolanaJ provides extensive support for Solana's ecosystem programs. For advanced program integrations including Serum DEX trading, liquidity pool operations, and custom program interactions, visit the companion repository:

**[SolanaJ-Programs](https://github.com/skynetcap/solanaj-programs)** - Advanced program implementations for professional Solana development

This extended library includes:
- **Serum DEX**: Place orders, cancel orders, settle funds, and manage open orders accounts
- **SPL Token Extensions**: Advanced token operations including multisig, freeze authority, and mint management
- **Anchor Program Support**: Interact with Anchor-based programs
- **Custom Program Templates**: Boilerplate for building your own program integrations

---

## 🏛️ Architecture & Design

SolanaJ follows modern software engineering principles and best practices:

- **Clean Architecture**: Separation of concerns with distinct layers for RPC, core logic, and program interactions
- **Type Safety**: Comprehensive use of Java's type system to prevent runtime errors
- **Immutability**: Defensive copying and immutable data structures where appropriate
- **Error Handling**: Graceful exception handling with detailed error messages
- **Testability**: Extensive unit and integration test coverage
- **Documentation**: Comprehensive JavaDoc documentation for all public APIs

---

## 🤝 Contributing

SolanaJ thrives on community contributions and welcomes developers of all skill levels to participate in its ongoing development. We appreciate bug reports, feature requests, documentation improvements, and code contributions.

### Contribution Guidelines

1. **Fork the Repository**: Create your own fork of the SolanaJ repository
2. **Create a Feature Branch**: Use descriptive branch names following the pattern `feature/your-feature-name` or `bugfix/issue-description`
3. **Implement Your Changes**: Write clean, well-documented code following the existing code style
4. **Add Tests**: Include comprehensive unit tests for new functionality
5. **Update Documentation**: Ensure JavaDoc comments are complete and update relevant README sections
6. **Commit Your Changes**: Use clear, descriptive commit messages following conventional commit standards
7. **Push to Your Fork**: Upload your changes to your forked repository
8. **Submit a Pull Request**: Create a detailed PR describing your changes, motivation, and any breaking changes

### Code Quality Standards

- Follow Java naming conventions and code style guidelines
- Maintain backward compatibility unless explicitly discussed
- Write comprehensive unit tests achieving >80% code coverage
- Include JavaDoc documentation for all public methods and classes
- Ensure all existing tests pass before submitting PR
- Run static analysis tools and address any warnings

### Areas for Contribution

We particularly welcome contributions in these areas:
- Additional Solana program integrations
- Performance optimizations
- Enhanced error handling and logging
- Expanded test coverage
- Documentation improvements and examples
- Bug fixes and security enhancements

---

## 💬 Support & Community

### Getting Help

- **GitHub Issues**: Report bugs or request features at [SolanaJ Issues](https://github.com/skynetcap/solanaj/issues)
- **Solana Discord**: Join the Solana developer community at [discord.gg/solana](https://discord.gg/solana)
- **Stack Overflow**: Tag questions with `solanaj` and `solana`

### Useful Resources

- [Solana Documentation](https://docs.solana.com/) - Official Solana developer documentation
- [Solana Cookbook](https://solanacookbook.com/) - Practical development recipes
- [Solana Stack Exchange](https://solana.stackexchange.com/) - Community Q&A platform

---

## 📄 License

SolanaJ is open-source software released under the **MIT License**. This permissive license allows you to freely use, modify, and distribute this software in both commercial and non-commercial projects.

See the [LICENSE](LICENSE) file for complete license terms and conditions.

---

<div align="center">

[Report Bug](https://github.com/skynetcap/solanaj/issues) · [Request Feature](https://github.com/skynetcap/solanaj/issues) · [Documentation](https://docs.solana.com/)

</div>