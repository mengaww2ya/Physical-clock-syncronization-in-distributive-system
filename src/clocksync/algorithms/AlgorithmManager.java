package clocksync.algorithms;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Manages the available clock synchronization algorithms.
 * Provides functionality to register, retrieve, and select algorithms.
 */
public class AlgorithmManager {
    
    private static final Map<String, ClockSyncAlgorithm> algorithms = new HashMap<>();
    private static ClockSyncAlgorithm activeAlgorithm = null;
    
    static {
        // Register default algorithms
        registerAlgorithm(new CristianAlgorithm());
        registerAlgorithm(new BerkeleyAlgorithm());
    }
    
    /**
     * Registers a new algorithm.
     *
     * @param algorithm The algorithm to register
     */
    public static void registerAlgorithm(ClockSyncAlgorithm algorithm) {
        algorithms.put(algorithm.getName().toLowerCase(), algorithm);
        
        // Set the first registered algorithm as active by default
        if (activeAlgorithm == null) {
            activeAlgorithm = algorithm;
        }
    }
    
    /**
     * Gets the currently active algorithm.
     *
     * @return The active algorithm
     */
    public static ClockSyncAlgorithm getActiveAlgorithm() {
        return activeAlgorithm;
    }
    
    /**
     * Sets the active algorithm.
     *
     * @param algorithmName The name of the algorithm to set as active
     * @return True if algorithm was found and set as active, false otherwise
     */
    public static boolean setActiveAlgorithm(String algorithmName) {
        ClockSyncAlgorithm algorithm = algorithms.get(algorithmName.toLowerCase());
        
        if (algorithm != null) {
            activeAlgorithm = algorithm;
            return true;
        }
        
        return false;
    }
    
    /**
     * Gets a list of all registered algorithm names.
     *
     * @return List of algorithm names
     */
    public static String[] getAlgorithmNames() {
        return algorithms.keySet().toArray(new String[0]);
    }
    
    /**
     * Gets a specific algorithm by name.
     *
     * @param name The name of the algorithm
     * @return The algorithm, or null if not found
     */
    public static ClockSyncAlgorithm getAlgorithm(String name) {
        return algorithms.get(name.toLowerCase());
    }
    
    /**
     * Gets information about all available algorithms.
     *
     * @return A formatted string with algorithm information
     */
    public static String getAlgorithmInfo() {
        StringBuilder info = new StringBuilder();
        info.append("\n=== AVAILABLE SYNCHRONIZATION ALGORITHMS ===\n");
        
        for (ClockSyncAlgorithm algorithm : algorithms.values()) {
            String activeIndicator = (algorithm == activeAlgorithm) ? " [ACTIVE]" : "";
            info.append(String.format("%s%s:\n", algorithm.getName(), activeIndicator));
            info.append(String.format("  %s\n\n", algorithm.getDescription()));
        }
        
        return info.toString();
    }
} 