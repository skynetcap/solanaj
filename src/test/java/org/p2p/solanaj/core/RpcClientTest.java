package org.p2p.solanaj.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.WeightedCluster;
import org.p2p.solanaj.rpc.types.WeightedEndpoint;

import java.util.Arrays;

public class RpcClientTest {

    private final String ZERO_ENDPOINT = "0";
    private final String ONE_ENDPOINT = "1";
    private final String TWO_ENDPOINT = "2";
    private final String THREE_ENDPOINT = "3";
    private final int WEIGHTED_TEST_OCCURRENCE = 10000000;

    @Test
    public void WeightRpcClientTest() {
        WeightedEndpoint endpoint0 = new WeightedEndpoint(ZERO_ENDPOINT, 10);
        WeightedEndpoint endpoint1 = new WeightedEndpoint(ONE_ENDPOINT, 20);
        WeightedEndpoint endpoint2 = new WeightedEndpoint(TWO_ENDPOINT, 30);
        WeightedEndpoint endpoint3 = new WeightedEndpoint(THREE_ENDPOINT, 40);
        WeightedCluster cluster = new WeightedCluster(Arrays.asList(endpoint0, endpoint1, endpoint2, endpoint3));
        RpcClient client = new RpcClient(cluster);
        int endpoint0Occurence = 0;
        int endpoint1Occurence = 0;
        int endpoint2Occurence = 0;
        int endpoint3Occurence = 0;
        for (int i = 0; i < WEIGHTED_TEST_OCCURRENCE; i++) {
            String endpoint = client.getEndpoint();
            if (endpoint.equals("0")) {
                endpoint0Occurence++;
            } else if (endpoint.equals("1")) {
                endpoint1Occurence++;
            } else if (endpoint.equals("2")) {
                endpoint2Occurence++;
            } else {
                endpoint3Occurence++;
            }
        }
        float endpoint0Percentage = (float)endpoint0Occurence / (float)WEIGHTED_TEST_OCCURRENCE;
        float endpoint1Percentage = (float)endpoint1Occurence / (float)WEIGHTED_TEST_OCCURRENCE;
        float endpoint2Percentage = (float)endpoint2Occurence / (float)WEIGHTED_TEST_OCCURRENCE;
        float endpoint3Percentage = (float)endpoint3Occurence / (float)WEIGHTED_TEST_OCCURRENCE;
        assertEquals(0.1f, endpoint0Percentage, 0.03);
        assertEquals(0.2f, endpoint1Percentage, 0.03);
        assertEquals(0.3f, endpoint2Percentage, 0.03);
        assertEquals(0.4f, endpoint3Percentage, 0.03);
    }

}
