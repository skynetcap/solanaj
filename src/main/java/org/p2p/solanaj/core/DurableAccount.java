package org.p2p.solanaj.core;


import com.syntifi.near.borshj.Borsh;
import com.syntifi.near.borshj.annotation.BorshField;
import org.bitcoinj.core.Base58;

import java.util.Arrays;

public class DurableAccount implements Borsh {

    @BorshField(order = 0)
    private long first;

    @BorshField(order = 1)
    private byte[] authority = new byte[32];

    @BorshField(order = 2)
    private byte[] blockhash = new byte[32];

    @BorshField(order = 3)
    private long fee;

    public long getFirst() {
        return first;
    }

    public void setFirst(long first) {
        this.first = first;
    }

    public byte[] getAuthority() {
        return authority;
    }

    public String getAuthorityBase58(){
        return Base58.encode(authority);
    }

    public void setAuthority(byte[] authority) {
        this.authority = authority;
    }

    public byte[] getBlockhash() {
        return blockhash;
    }
    public String getBlockhashBase58(){
        return Base58.encode(blockhash);
    }
    public void setBlockhash(byte[] blockhash) {
        this.blockhash = blockhash;
    }

    public long getFee() {
        return fee;
    }

    public void setFee(long fee) {
        this.fee = fee;
    }

    @Override
    public String toString() {
        return "NonceAccount [first=" + first + ", authority=" + Arrays.toString(authority) + ", blockhash="
                + Arrays.toString(blockhash) + ", fee=" + fee + "]";
    }

}
