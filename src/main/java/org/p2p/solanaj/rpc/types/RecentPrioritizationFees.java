package org.p2p.solanaj.rpc.types;

import java.util.Map;

public class RecentPrioritizationFees {
    private long slot;
    private long prioritizationFee;

    public RecentPrioritizationFees(Map<String, Object> jsonMap) {
        this.slot = ((Number) jsonMap.get("slot")).longValue();
        this.prioritizationFee = ((Number) jsonMap.get("prioritizationFee")).longValue();
    }

    public long getSlot() {
        return slot;
    }

    public long getPrioritizationFee() {
        return prioritizationFee;
    }
}