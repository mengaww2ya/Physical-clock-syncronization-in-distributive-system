package clocksync.algorithms;

import clocksync.Node;

/**
 * Interface for clock synchronization algorithms.
 * All synchronization algorithms should implement this interface.
 */
public interface ClockSyncAlgorithm {
    
    /**
     * Name of the algorithm
     *
     * @return The algorithm's name
     */
    String getName();
    
    /**
     * Description of how the algorithm works
     *
     * @return The algorithm's description
     */
    String getDescription();
    
    /**
     * Performs clock synchronization between the specified node and a reference
     * (typically a master node or a set of nodes depending on the algorithm).
     *
     * @param node The node to synchronize
     * @param reference Can be a master node or any reference required by the algorithm
     * @return SyncResult containing information about the synchronization process
     */
    SyncResult synchronize(Node node, Object reference);
} 