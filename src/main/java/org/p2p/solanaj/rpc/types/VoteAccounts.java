package org.p2p.solanaj.rpc.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class VoteAccounts {

    @Getter
    @ToString
    public static class VoteAccountValue {

        @JsonProperty("commission")
        private long commission;

        @JsonProperty("epochVoteAccount")
        private boolean epochVoteAccount;

        @JsonProperty("epochCredits")
        private List<List<Long>> epochCredits;

        @JsonProperty("nodePubkey")
        private String nodePubkey;

        @JsonProperty("lastVote")
        private long lastVote;

        @JsonProperty("activatedStake")
        private long activatedStake;

        @JsonProperty("votePubkey")
        private String votePubkey;


    }

    @JsonProperty("current")
    private List<VoteAccountValue> current;

    @JsonProperty("delinquent")
    private List<VoteAccountValue> delinquent;

}
