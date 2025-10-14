package org.p2p.solanaj.rpc.types.config;

import lombok.Setter;
import lombok.Getter;

@Getter
@Setter
public class VoteAccountConfig {

    private String votePubkey;

    private String commitment;

}
