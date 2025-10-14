package org.p2p.solanaj.rpc.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.p2p.solanaj.core.PublicKey;

import java.util.AbstractMap;

@Getter
@ToString
@NoArgsConstructor
public class ClusterNode {

    // Constructor for deserializing into List
    @SuppressWarnings({ "rawtypes" })
    public ClusterNode(AbstractMap pa) {
        this.pubkey = new PublicKey((String) pa.get("pubkey"));
        this.gossip = (String) pa.get("gossip");
        this.tpu = (String) pa.get("tpu");
        this.rpc = (String) pa.get("rpc");
        this.version = (String) pa.get("version");
    }

    @JsonProperty("pubkey")
    private PublicKey pubkey;

    @JsonProperty("gossip")
    private String gossip;

    @JsonProperty("tpu")
    private String tpu;

    @JsonProperty("rpc")
    private String rpc;

    @JsonProperty("version")
    private String version;
}
