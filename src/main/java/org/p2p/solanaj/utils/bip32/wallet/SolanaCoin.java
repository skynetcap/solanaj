package org.p2p.solanaj.utils.bip32.wallet;

import lombok.Getter;
import org.p2p.solanaj.utils.bip32.wallet.key.SolanaCurve;

public class SolanaCoin {
    /**
     * -- GETTER --
     *  Get the curve
     *
     * @return curve
     */
    @Getter
    private final SolanaCurve curve = new SolanaCurve();
    /**
     * -- GETTER --
     *  get the coin type
     *
     * @return coin type
     */
    @Getter
    private final long coinType = 501;
    /**
     * -- GETTER --
     *  get the coin purpose
     *
     * @return purpose
     */
    @Getter
    private final long purpose = 44;
    private final boolean alwaysHardened = true;

    /**
     * get whether the addresses must always be hardened
     *
     * @return always hardened
     */
    public boolean getAlwaysHardened() {
        return alwaysHardened;
    }

}
