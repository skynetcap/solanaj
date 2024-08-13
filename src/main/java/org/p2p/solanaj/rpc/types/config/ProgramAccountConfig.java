package org.p2p.solanaj.rpc.types.config;

import java.util.List;

import lombok.Setter;
import org.p2p.solanaj.rpc.types.config.RpcSendTransactionConfig.Encoding;

public class ProgramAccountConfig {

    @Setter
    private Encoding encoding = null;

    @Setter
    private List<Object> filters = null;

    private final String commitment = "processed";

    public ProgramAccountConfig(List<Object> filters) {
        this.filters = filters;
    }

    public ProgramAccountConfig(Encoding encoding) {
        this.encoding = encoding;
    }

}