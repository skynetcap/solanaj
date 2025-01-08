package org.p2p.solanaj.core;

public class Sysvar {
    public static final PublicKey SYSVAR_RENT_ADDRESS = new PublicKey("SysvarRent111111111111111111111111111111111");

    /**
     * The public key for the recent blockhashes sysvar account.
     * <p>
     * This constant defines the public key associated with the Solana account for recent blockhashes.
     * It is set to the value returned by {@link } using the Base58-encoded string for the sysvar account.
     * </p>
     */
    public static final PublicKey RECENT_BLOCKHASHES = new PublicKey("SysvarRecentB1ockHashes11111111111111111111");
}
