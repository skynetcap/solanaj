package org.p2p.solanaj.programs;

import org.junit.Ignore;
import org.junit.Test;
import org.p2p.solanaj.core.Account;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class BPFLoaderTest {

    private final RpcClient client = new RpcClient("https://ssc-dao.genesysgo.net/");

    @Test
    @Ignore
    public void initializeBufferTest() throws RpcException {
        Account account = null;
        try {
            account = Account.fromJson(Files.readString(Paths.get("src/test/resources/mainnet.json")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(account.getPublicKey().toBase58());

        Transaction transaction = new Transaction();

        // initialize buffer
        Account bufferAccount = new Account();

        transaction.addInstruction(
                SystemProgram.createAccount(
                        account.getPublicKey(),
                        bufferAccount.getPublicKey(),
                        3290880,
                        165L,
                        PublicKey.valueOf("BPFLoaderUpgradeab1e11111111111111111111111")
                )
        );

        transaction.addInstruction(
                BPFLoader.initializeBuffer(
                        bufferAccount.getPublicKey(),
                        account.getPublicKey()
                )
        );

        String hash = client.getApi().getRecentBlockhash();
        transaction.setRecentBlockHash(hash);

        System.out.println("TX: " + client.getApi().sendTransaction(transaction, List.of(account, bufferAccount), hash));

    }
}
