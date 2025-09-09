package org.p2p.solanaj.rpc.types.config;

import java.util.List;

import org.p2p.solanaj.rpc.types.config.RpcSendTransactionConfig.Encoding;

public class ProgramAccountConfig {

    private Encoding encoding = null;

    private List<Object> filters = null;

    private String commitment = "processed";

    // V2 specific / pagination & incremental update parameters
    private Integer limit;
    private String paginationKey;
    private Long changedSinceSlot;

    public ProgramAccountConfig(List<Object> filters) {
        this.filters = filters;
    }

    public ProgramAccountConfig(Encoding encoding) {
        this.encoding = encoding;
    }

    public void setEncoding(Encoding encoding) {
        this.encoding = encoding;
    }

    public void setFilters(List<Object> filters) {
        this.filters = filters;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public void setPaginationKey(String paginationKey) {
        this.paginationKey = paginationKey;
    }

    public void setChangedSinceSlot(Long changedSinceSlot) {
        this.changedSinceSlot = changedSinceSlot;
    }

    // Getters to support merging and advanced usage
    public Encoding getEncoding() {
        return encoding;
    }

    public List<Object> getFilters() {
        return filters;
    }

    public String getCommitment() {
        return commitment;
    }

    public Integer getLimit() {
        return limit;
    }

    public String getPaginationKey() {
        return paginationKey;
    }

    public Long getChangedSinceSlot() {
        return changedSinceSlot;
    }
}