package clocksync.algorithms;

import clocksync.Node;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Implementation of Berkeley's algorithm for clock synchronization.
 * 
 * This algorithm works by:
 * 1. A coordinator node polls all nodes for their time
 * 2. The coordinator calculates the average time from all responses
 * 3. The coordinator tells each node how much to adjust its clock
 */
public class BerkeleyAlgorithm implements ClockSyncAlgorithm {
    
    @Override
    public String getName() {
        return "Berkeley Algorithm";
    }
    
    @Override
    public String getDescription() {
        return "A fault-tolerant clock synchronization algorithm where a coordinator " +
               "collects time samples from all nodes, calculates their average, and " +
               "sends adjustment values to each node. Ideal for systems without access to " +
               "an external time reference.";
    }
    
    @Override
    public SyncResult synchronize(Node node, Object reference) {
        if (!(reference instanceof List)) {
            throw new IllegalArgumentException("Reference must be a list of nodes for Berkeley algorithm");
        }
        
        @SuppressWarnings("unchecked")
        List<Node> nodes = (List<Node>) reference;
        
        if (nodes.isEmpty()) {
            throw new IllegalArgumentException("Node list cannot be empty");
        }
        
        // Use this node as coordinator if it's in the list, otherwise use the first node
        Node coordinator = nodes.contains(node) ? node : nodes.get(0);
        
        SyncResult.Builder resultBuilder = new SyncResult.Builder();
        
        try {
            // Record time before sync
            Instant beforeSync = node.getPhysicalClock().getTimeAsInstant();
            resultBuilder.beforeSync(beforeSync);
            
            // Step 1: Coordinator collects time from all nodes and measures round-trip time
            Map<Node, Long> clockTimes = new HashMap<>();
            Map<Node, Long> roundTripTimes = new HashMap<>();
            
            for (Node n : nodes) {
                long requestTime = System.currentTimeMillis();
                long nodeTime = n.getTime();
                long responseTime = System.currentTimeMillis();
                
                clockTimes.put(n, nodeTime);
                roundTripTimes.put(n, responseTime - requestTime);
            }
            
            // Step 2: Coordinator calculates time differences accounting for network delay
            Map<Node, Long> adjustedTimes = new HashMap<>();
            long sum = 0;
            int count = 0;
            
            for (Node n : nodes) {
                // Adjust time for network delay (RTT/2)
                long rtt = roundTripTimes.get(n);
                long oneWayDelay = rtt / 2;
                long adjustedTime = clockTimes.get(n) + oneWayDelay;
                
                adjustedTimes.put(n, adjustedTime);
                sum += adjustedTime;
                count++;
            }
            
            // Step 3: Calculate the average time
            long averageTime = sum / count;
            
            // Step 4: Calculate adjustments for each node
            Map<Node, Long> adjustments = new HashMap<>();
            for (Node n : nodes) {
                long adjustment = averageTime - adjustedTimes.get(n);
                adjustments.put(n, adjustment);
            }
            
            // Step 5: Apply adjustment to this node
            long adjustment = adjustments.get(node);
            long actualAdjustment = node.getPhysicalClock().synchronize(averageTime);
            
            // Record time after sync
            Instant afterSync = node.getPhysicalClock().getTimeAsInstant();
            resultBuilder.afterSync(afterSync);
            
            // Build the result
            String details = String.format(
                "Average time: %d ms, Adjustment: %d ms, Nodes: %d",
                averageTime, actualAdjustment, nodes.size()
            );
            
            return resultBuilder
                    .success(true)
                    .adjustmentMs(actualAdjustment)
                    .roundTripTimeMs(roundTripTimes.getOrDefault(node, 0L))
                    .estimatedErrorMs(roundTripTimes.getOrDefault(node, 0L) / 2.0)
                    .details(details)
                    .build();
                    
        } catch (Exception e) {
            return resultBuilder
                    .success(false)
                    .details("Error: " + e.getMessage())
                    .build();
        }
    }
} 