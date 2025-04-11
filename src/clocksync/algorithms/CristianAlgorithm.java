package clocksync.algorithms;

import clocksync.Node;
import java.time.Instant;

/**
 * Implementation of Cristian's algorithm for clock synchronization.
 * 
 * This algorithm works by:
 * 1. Client requests time from a time server
 * 2. Server responds with its current time
 * 3. Client adjusts for network latency by adding RTT/2 to the server time
 */
public class CristianAlgorithm implements ClockSyncAlgorithm {

    private static final int MAX_ATTEMPTS = 5;
    
    @Override
    public String getName() {
        return "Cristian's Algorithm";
    }
    
    @Override
    public String getDescription() {
        return "A simple master-slave synchronization algorithm where clients synchronize " +
               "with a time server by requesting the server's time and adjusting for " +
               "network latency (RTT/2). Provides good accuracy with minimal overhead.";
    }
    
    @Override
    public SyncResult synchronize(Node node, Object reference) {
        if (!(reference instanceof Node) || !((Node) reference).isMaster()) {
            throw new IllegalArgumentException("Reference must be a master node for Cristian's algorithm");
        }
        
        Node masterNode = (Node) reference;
        SyncResult.Builder resultBuilder = new SyncResult.Builder();
        
        try {
            // Record time before sync
            Instant beforeSync = node.getPhysicalClock().getTimeAsInstant();
            resultBuilder.beforeSync(beforeSync);
            
            // Get local time before request
            long requestSentTime = System.currentTimeMillis();
            
            // Get master's time
            long masterTime = masterNode.getTime();
            
            // Get local time after response
            long responseReceivedTime = System.currentTimeMillis();
            
            // Calculate round trip time
            long roundTripTime = responseReceivedTime - requestSentTime;
            resultBuilder.roundTripTimeMs(roundTripTime);
            
            // Calculate estimated one-way delay (half of RTT)
            long estimatedOneWayDelay = roundTripTime / 2;
            
            // Calculate the new time (master's time + one-way delay)
            long newTime = masterTime + estimatedOneWayDelay;
            
            // Calculate adjustment (how much to change local time by)
            long currentLocalTime = node.getPhysicalClock().getTime();
            long adjustment = newTime - currentLocalTime;
            
            // Apply the adjustment
            long actualAdjustment = node.getPhysicalClock().synchronize(newTime);
            
            // Record time after sync
            Instant afterSync = node.getPhysicalClock().getTimeAsInstant();
            resultBuilder.afterSync(afterSync);
            
            // Estimated error is half of the round-trip time
            double estimatedError = roundTripTime / 2.0;
            
            String details = String.format(
                "Master time: %d ms, RTT: %d ms, Adjustment: %d ms, Est. error: %.2f ms",
                masterTime, roundTripTime, actualAdjustment, estimatedError
            );
            
            // Build the result
            return resultBuilder
                    .success(true)
                    .adjustmentMs(actualAdjustment)
                    .estimatedErrorMs(estimatedError)
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