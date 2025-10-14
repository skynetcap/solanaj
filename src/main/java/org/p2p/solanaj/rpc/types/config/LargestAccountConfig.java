package org.p2p.solanaj.rpc.types.config;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LargestAccountConfig {

    private String filter;

    private String commitment;

}