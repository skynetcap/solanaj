package org.p2p.solanaj.rpc.types.config;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RpcEpochConfig {

    private Long epoch;

    private String commitment;

}