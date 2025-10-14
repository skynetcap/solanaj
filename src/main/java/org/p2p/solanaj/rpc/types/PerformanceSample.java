package org.p2p.solanaj.rpc.types;

import lombok.Getter;

import java.util.AbstractMap;

@Getter
public class PerformanceSample {

    private final double slot;
    private final double numTransactions;
    private final double numSlots;
    private final double samplePeriodsSecs;

    @SuppressWarnings("rawtypes")
    public PerformanceSample(AbstractMap am) {
        Object slotObj = am.get("slot");
        this.slot = slotObj instanceof Number ? ((Number) slotObj).doubleValue() : 0.0;
        
        Object numTransactionsObj = am.get("numTransactions");
        this.numTransactions = numTransactionsObj instanceof Number ? ((Number) numTransactionsObj).doubleValue() : 0.0;
        
        Object numSlotsObj = am.get("numSlots");
        this.numSlots = numSlotsObj instanceof Number ? ((Number) numSlotsObj).doubleValue() : 0.0;
        
        Object samplePeriodSecsObj = am.get("samplePeriodSecs");
        this.samplePeriodsSecs = samplePeriodSecsObj instanceof Number ? ((Number) samplePeriodSecsObj).doubleValue() : 0.0;
    }
}
