package org.p2p.solanaj.rpc.types.config;

import java.util.List;

import org.p2p.solanaj.rpc.types.config.RpcSendTransactionConfig.Encoding;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProgramAccountConfig {

    private Encoding encoding = null;

    private List<Object> filters = null;

    private String commitment = "processed";

    private Long changedSinceSlot;

    public ProgramAccountConfig(List<Object> filters) {
        this.filters = filters;
    }

    public ProgramAccountConfig(Encoding encoding) {
        this.encoding = encoding;
    }
}