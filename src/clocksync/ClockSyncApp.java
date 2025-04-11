package clocksync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import clocksync.algorithms.AlgorithmManager;
import clocksync.algorithms.SyncResult;

/**
 * Main application class for the clock synchronization system.
 * This class demonstrates the usage of the physical clock synchronization.
 */
public class ClockSyncApp {
    private static final Logger logger = LoggerFactory.getLogger(ClockSyncApp.class);
    
    // Configuration for the multicast network
    private static final String MULTICAST_GROUP = "224.0.0.1";
    private static final int MULTICAST_PORT = 9876;
    
    // List of running nodes
    private static final List<Node> nodes = new ArrayList<>();
    
    // Formatter for dates
    private static final DateTimeFormatter DATE_FORMATTER = 
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());
    
    public static void main(String[] args) {
        printWelcomeBanner();
        
        logger.info("Starting Clock Synchronization Application");
        
        // Start with a master node
        try {
            System.out.println("\n[SYSTEM] Creating a master time server node...");
            Node masterNode = createNode("MasterNode", 9000, true);
            nodes.add(masterNode);
            masterNode.start();
            System.out.println("[SYSTEM] ✓ Master node created and started successfully!");
            
            // Create some regular nodes with different drift rates
            System.out.println("\n[SYSTEM] Creating regular nodes with different clock drift rates...");
            
            // Node 1 - Slightly fast clock
            Node node1 = createNode("Node1-Fast", 9001, false);
            node1.getPhysicalClock().setDriftRate(1.02); // 2% faster
            nodes.add(node1);
            node1.start();
            System.out.println("[SYSTEM] ✓ Created Node1-Fast with drift rate: 1.02 (2% faster than real time)");
            
            // Node 2 - Normal clock
            Node node2 = createNode("Node2-Normal", 9002, false);
            node2.getPhysicalClock().setDriftRate(1.0); // normal rate
            nodes.add(node2);
            node2.start();
            System.out.println("[SYSTEM] ✓ Created Node2-Normal with drift rate: 1.00 (normal speed)");
            
            // Node 3 - Slow clock
            Node node3 = createNode("Node3-Slow", 9003, false);
            node3.getPhysicalClock().setDriftRate(0.98); // 2% slower
            nodes.add(node3);
            node3.start();
            System.out.println("[SYSTEM] ✓ Created Node3-Slow with drift rate: 0.98 (2% slower than real time)");
            
            System.out.println("\n[SYSTEM] All nodes are now running. Without synchronization, their clocks will drift apart!");
            System.out.println("[SYSTEM] Physical clock synchronization will help keep them synchronized.");
            
            // Show available synchronization algorithms
            showAlgorithmSelectionMenu();
            
            // Start a console for user interaction
            System.out.println("\n-------------------------------------------------------------");
            System.out.println("INTERACTIVE CONSOLE READY - Type 'help' for available commands");
            System.out.println("-------------------------------------------------------------");
            startConsole();
            
        } catch (IOException e) {
            logger.error("Failed to start the application", e);
            System.out.println("[ERROR] Failed to start the application: " + e.getMessage());
        } finally {
            // Ensure all nodes are properly stopped
            stopAllNodes();
        }
    }
    
    /**
     * Prints a welcome banner explaining the application
     */
    private static void printWelcomeBanner() {
        System.out.println("\n=================================================================");
        System.out.println("           PHYSICAL CLOCK SYNCHRONIZATION SYSTEM                 ");
        System.out.println("=================================================================");
        System.out.println("This application demonstrates how physical clocks are synchronized");
        System.out.println("in a distributed system. Key concepts illustrated:");
        System.out.println();
        System.out.println("1. Each node has its own physical clock that can drift");
        System.out.println("2. Clocks naturally run at slightly different rates");
        System.out.println("3. Without synchronization, distributed clocks would drift apart");
        System.out.println("4. Multiple algorithms available for clock synchronization:");
        System.out.println("   - Cristian's algorithm for simple master-slave synchronization");
        System.out.println("   - Berkeley algorithm for democratic, fault-tolerant synchronization");
        System.out.println("5. Use the 'algorithm' command to select and learn about each method");
        System.out.println("=================================================================\n");
    }
    
    /**
     * Creates a new node with the specified configuration.
     *
     * @param name The node name
     * @param port The port to use
     * @param isMaster Whether this is a master node
     * @return The created node
     * @throws IOException If there's an error setting up the node
     */
    private static Node createNode(String name, int port, boolean isMaster) throws IOException {
        return new Node(name, port, MULTICAST_GROUP, MULTICAST_PORT, isMaster);
    }
    
    /**
     * Starts an interactive console for user commands.
     */
    private static void startConsole() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        
        while (running) {
            System.out.print("\n> ");
            String command = scanner.nextLine().trim();
            
            try {
                running = executeCommand(command);
            } catch (Exception e) {
                System.out.println("[ERROR] " + e.getMessage());
            }
        }
        
        scanner.close();
    }
    
    /**
     * Executes a user command.
     *
     * @param command The command to execute
     * @return True if the application should continue running, false to exit
     */
    private static boolean executeCommand(String command) {
        if (command.isEmpty()) {
            return true;
        }
        
        String[] parts = command.split("\\s+", 2);
        String cmd = parts[0].toLowerCase();
        
        switch (cmd) {
            case "help":
                showHelp();
                break;
                
            case "list":
                listNodes();
                break;
                
            case "time":
                showTime();
                break;
                
            case "monitor":
                if (parts.length > 1) {
                    try {
                        int seconds = Integer.parseInt(parts[1]);
                        monitorClocks(seconds);
                    } catch (NumberFormatException e) {
                        System.out.println("[ERROR] Invalid number format. Usage: monitor <seconds>");
                    } catch (InterruptedException e) {
                        System.out.println("[INFO] Monitoring interrupted.");
                        Thread.currentThread().interrupt();
                    }
                } else {
                    System.out.println("[INFO] Usage: monitor <seconds>");
                }
                break;
                
            case "drift":
                showDrift();
                break;
                
            case "add":
                if (parts.length > 1) {
                    addNode(parts[1]);
                } else {
                    System.out.println("[INFO] Usage: add <n>");
                }
                break;
                
            case "sync":
                if (parts.length > 1) {
                    try {
                        int nodeIndex = Integer.parseInt(parts[1]) - 1;
                        if (nodeIndex >= 0 && nodeIndex < nodes.size()) {
                            syncUsingAlgorithm(nodes.get(nodeIndex));
                        } else {
                            System.out.println("[ERROR] Invalid node index. Use 'list' to see available nodes.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("[INFO] Usage: sync <node_index>");
                    }
                } else {
                    System.out.println("[INFO] Usage: sync <node_index>");
                }
                break;
                
            case "algorithm":
                if (parts.length > 1) {
                    setAlgorithm(parts[1]);
                } else {
                    showAlgorithms();
                }
                break;
                
            case "explain":
                explainPhysicalClockSync();
                break;
                
            case "exit":
            case "quit":
                System.out.println("[SYSTEM] Shutting down all nodes and exiting...");
                return false;
                
            default:
                System.out.println("[ERROR] Unknown command. Type 'help' for available commands.");
                break;
        }
        
        return true;
    }
    
    /**
     * Displays help information.
     */
    private static void showHelp() {
        System.out.println("\n=== AVAILABLE COMMANDS ===");
        System.out.println("  ALGORITHM SELECTION:");
        System.out.println("  algorithm    - Show available synchronization algorithms");
        System.out.println("  algorithm <name> - Change the active synchronization algorithm");
        System.out.println();
        System.out.println("  SYNCHRONIZATION:");
        System.out.println("  sync <idx>   - Synchronize node at index <idx> using current algorithm");
        System.out.println();
        System.out.println("  MONITORING AND INFORMATION:");
        System.out.println("  help         - Display this help message");
        System.out.println("  list         - List all nodes");
        System.out.println("  time         - Show current time on all nodes");
        System.out.println("  drift        - Show drift rates of all nodes");
        System.out.println("  monitor <s>  - Monitor clock times for s seconds");
        System.out.println("  explain      - Explanation of physical clock synchronization");
        System.out.println();
        System.out.println("  NODE MANAGEMENT:");
        System.out.println("  add <name>   - Add a new regular node");
        System.out.println();
        System.out.println("  SYSTEM:");
        System.out.println("  exit/quit    - Exit the application");
        System.out.println("=========================");
        
        // Show the currently active algorithm
        System.out.println("\nCurrent algorithm: " + AlgorithmManager.getActiveAlgorithm().getName());
    }
    
    /**
     * Lists all running nodes.
     */
    private static void listNodes() {
        System.out.println("\n=== NODES IN THE SYSTEM ===");
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            String roleSymbol = node.isMaster() ? "★" : "•";
            System.out.printf("%d. %s %s (ID: %s) - %s%n", 
                    i + 1, 
                    roleSymbol,
                    node.getNodeName(), 
                    node.getNodeId().substring(0, 8), 
                    node.isMaster() ? "MASTER (Time source)" : "REGULAR (Syncs with master)");
        }
        System.out.println("==========================");
    }
    
    /**
     * Shows the current time on all nodes.
     */
    private static void showTime() {
        System.out.println("\n=== CURRENT CLOCK TIMES ===");
        
        // Use the master node's time as reference if available, otherwise system time
        long referenceTime = nodes.stream()
                .filter(Node::isMaster)
                .findFirst()
                .map(Node::getTime)
                .orElse(System.currentTimeMillis());
        
        System.out.println("Reference time: " + DATE_FORMATTER.format(Instant.ofEpochMilli(referenceTime)));
        System.out.println("-----------------------------------");
        
        // Calculate max name length for formatting
        int maxNameLength = nodes.stream()
                .map(n -> n.getNodeName().length())
                .max(Integer::compare)
                .orElse(10);
        
        String format = "%-" + (maxNameLength + 3) + "s | %s | %+6d ms%s%n";
        
        for (Node node : nodes) {
            long time = node.getTime();
            long diff = time - referenceTime;
            
            // Add indicators for significant drift
            String indicator = "";
            if (Math.abs(diff) > 500) {
                indicator = " ⚠️ SIGNIFICANT DRIFT";
            } else if (Math.abs(diff) > 100) {
                indicator = " ⚠️ NOTICEABLE DRIFT";
            }
            
            // Mark master node
            String nodeName = node.getNodeName();
            if (node.isMaster()) {
                nodeName += " ★";
            }
            
            System.out.printf(format, 
                    nodeName, 
                    DATE_FORMATTER.format(Instant.ofEpochMilli(time)),
                    diff,
                    indicator);
        }
        System.out.println("==========================");
    }
    
    /**
     * Shows the drift rates of all nodes.
     */
    private static void showDrift() {
        System.out.println("\n=== CLOCK DRIFT RATES ===");
        System.out.println("A drift rate of 1.0 means the clock runs at normal speed.");
        System.out.println("Values > 1.0 mean faster clocks, < 1.0 mean slower clocks.");
        System.out.println("-----------------------------------");
        
        for (Node node : nodes) {
            double driftRate = node.getPhysicalClock().getDriftRate();
            String indicator = "";
            
            if (driftRate > 1.01) {
                indicator = " (FAST)";
            } else if (driftRate < 0.99) {
                indicator = " (SLOW)";
            } else {
                indicator = " (NORMAL)";
            }
            
            String nodeName = node.getNodeName();
            if (node.isMaster()) {
                nodeName += " ★";
            }
            
            System.out.printf("%-15s | Drift Rate: %.4f%s%n", 
                    nodeName, 
                    driftRate,
                    indicator);
        }
        System.out.println("==========================");
    }
    
    /**
     * Creates a string of repeated characters
     * 
     * @param ch The character to repeat
     * @param count Number of times to repeat
     * @return The resulting string
     */
    private static String repeatChar(char ch, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }
    
    /**
     * Monitors the clocks for a specified number of seconds.
     * 
     * @param seconds The number of seconds to monitor
     * @throws InterruptedException If interrupted during sleep
     */
    private static void monitorClocks(int seconds) throws InterruptedException {
        if (seconds <= 0 || seconds > 300) {
            System.out.println("[ERROR] Please specify a monitoring period between 1 and 300 seconds.");
            return;
        }
        
        System.out.println("\n[MONITOR] Starting clock monitoring for " + seconds + " seconds...");
        System.out.println("[MONITOR] Press Ctrl+C to stop monitoring early.");
        System.out.println("\n=== CLOCK MONITORING ===");
        
        // Get reference node (master)
        Node referenceNode = nodes.stream()
                .filter(Node::isMaster)
                .findFirst()
                .orElse(nodes.get(0));
        
        // Print header
        System.out.println("Time              | " + String.join(" | ", 
                nodes.stream().map(n -> String.format("%-10s", n.getNodeName())).toArray(String[]::new)));
        System.out.println(repeatChar('-', 20 + nodes.size() * 14));
        
        // Monitor for the specified duration
        for (int i = 0; i <= seconds; i++) {
            long refTime = referenceNode.getTime();
            String timeStr = DATE_FORMATTER.format(Instant.ofEpochMilli(refTime));
            
            StringBuilder line = new StringBuilder();
            line.append(String.format("%-17s | ", timeStr));
            
            for (Node node : nodes) {
                long diff = node.getTime() - refTime;
                line.append(String.format("%+10d | ", diff));
            }
            
            System.out.println(line.toString());
            
            if (i < seconds) {
                Thread.sleep(1000);
            }
        }
        
        System.out.println(repeatChar('-', 20 + nodes.size() * 14));
        System.out.println("[MONITOR] Monitoring complete.");
        System.out.println("=======================");
    }
    
    /**
     * Adds a new regular node.
     *
     * @param name The name for the new node
     */
    private static void addNode(String name) {
        try {
            // Generate a port number
            int port = 9100 + nodes.size();
            
            // Ask for drift rate
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter drift rate for the new node (0.9 to 1.1, default 1.0): ");
            String driftInput = scanner.nextLine().trim();
            
            double driftRate = 1.0;
            if (!driftInput.isEmpty()) {
                try {
                    driftRate = Double.parseDouble(driftInput);
                    if (driftRate < 0.9 || driftRate > 1.1) {
                        System.out.println("[WARNING] Drift rate outside recommended range. Using 1.0 instead.");
                        driftRate = 1.0;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("[WARNING] Invalid drift rate. Using default 1.0.");
                }
            }
            
            // Create and start the node
            System.out.println("[SYSTEM] Creating new node '" + name + "' with drift rate " + driftRate + "...");
            Node node = createNode(name, port, false);
            node.getPhysicalClock().setDriftRate(driftRate);
            nodes.add(node);
            node.start();
            
            System.out.println("[SYSTEM] ✓ Added new node: " + name + " on port " + port + " with drift rate " + driftRate);
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to add node: " + e.getMessage());
        }
    }
    
    /**
     * Synchronizes a node with the master using the currently selected clock synchronization algorithm.
     *
     * @param node The node to synchronize
     */
    private static void syncUsingAlgorithm(Node node) {
        // First, find the master node
        Node masterNode = null;
        for (Node n : nodes) {
            if (n.isMaster()) {
                masterNode = n;
                break;
            }
        }
        
        if (masterNode == null) {
            System.out.println("[ERROR] No master node found for synchronization");
            return;
        }
        
        if (node.isMaster()) {
            System.out.println("[ERROR] The master node does not need to be synchronized");
            return;
        }
        
        System.out.println("\n=== CLOCK SYNCHRONIZATION ===");
        System.out.println("Algorithm: " + AlgorithmManager.getActiveAlgorithm().getName());
        System.out.println("Node to sync: " + node.getNodeName());
        
        // Display algorithm-specific setup information
        boolean isBerkeley = AlgorithmManager.getActiveAlgorithm().getName().toLowerCase().contains("berkeley");
        if (isBerkeley) {
            System.out.println("Synchronization type: Averaging all nodes");
            System.out.println("Nodes participating: " + nodes.size());
        } else {
            System.out.println("Synchronization type: Master-slave");
            System.out.println("Master node: " + masterNode.getNodeName());
        }
        
        // Show time before synchronization
        System.out.printf("\nBefore synchronization:\n");
        System.out.printf("  %-15s: %s\n", node.getNodeName(), 
                DATE_FORMATTER.format(node.getPhysicalClock().getTimeAsInstant()));
        if (!isBerkeley) {
            System.out.printf("  %-15s: %s\n", masterNode.getNodeName(), 
                DATE_FORMATTER.format(masterNode.getPhysicalClock().getTimeAsInstant()));
        }
        
        try {
            System.out.println("\nPerforming synchronization...");
            SyncResult result;
            
            // Use the appropriate reference based on the algorithm type
            if (isBerkeley) {
                // Berkeley algorithm needs all nodes
                result = AlgorithmManager.getActiveAlgorithm().synchronize(node, new ArrayList<>(nodes));
            } else {
                // Other algorithms typically use a master node
                result = AlgorithmManager.getActiveAlgorithm().synchronize(node, masterNode);
            }
            
            // Display results
            if (result.isSuccess()) {
                System.out.println("\n✓ Synchronization successful!");
                System.out.println("Clock adjustment: " + result.getAdjustmentMs() + " ms");
                System.out.println("Round-trip time: " + result.getRoundTripTimeMs() + " ms");
                System.out.println("Estimated error: ±" + String.format("%.2f", result.getEstimatedErrorMs()) + " ms");
                
                if (result.getBeforeSync() != null && result.getAfterSync() != null) {
                    System.out.println("Before sync: " + DATE_FORMATTER.format(result.getBeforeSync()));
                    System.out.println("After sync:  " + DATE_FORMATTER.format(result.getAfterSync()));
                }
                
                System.out.println("\nDetails: " + result.getDetails());
                
                // Show time after synchronization
                System.out.printf("\nAfter synchronization:\n");
                System.out.printf("  %-15s: %s\n", node.getNodeName(), 
                        DATE_FORMATTER.format(node.getPhysicalClock().getTimeAsInstant()));
                if (!isBerkeley) {
                    System.out.printf("  %-15s: %s\n", masterNode.getNodeName(), 
                        DATE_FORMATTER.format(masterNode.getPhysicalClock().getTimeAsInstant()));
                }
                
                // Show current time difference
                if (!isBerkeley) {
                    long timeDiff = Math.abs(node.getTime() - masterNode.getTime());
                    System.out.println("\nCurrent time difference: " + timeDiff + " ms");
                }
            } else {
                System.out.println("\n✗ Synchronization failed: " + result.getDetails());
            }
        } catch (Exception e) {
            System.out.println("\n✗ Synchronization failed with error: " + e.getMessage());
        }
    }
    
    /**
     * Sets the active clock synchronization algorithm.
     *
     * @param algorithmName The name of the algorithm to set
     */
    private static void setAlgorithm(String algorithmName) {
        // Save the current algorithm name for comparison
        String currentAlgorithm = AlgorithmManager.getActiveAlgorithm().getName();
        
        if (AlgorithmManager.setActiveAlgorithm(algorithmName.toLowerCase())) {
            String newAlgorithm = AlgorithmManager.getActiveAlgorithm().getName();
            
            // Check if it's actually a change
            if (currentAlgorithm.equalsIgnoreCase(newAlgorithm)) {
                System.out.println("[INFO] Algorithm '" + newAlgorithm + "' is already active.");
                return;
            }
            
            System.out.println("\n[SYSTEM] Algorithm changed!");
            System.out.println("  From: " + currentAlgorithm);
            System.out.println("  To:   " + newAlgorithm);
            
            // Show the description of the new algorithm
            System.out.println("\nALGORITHM DETAILS:");
            System.out.println("  " + AlgorithmManager.getActiveAlgorithm().getDescription());
            
            // Inform about how to use the algorithm
            System.out.println("\n[INFO] Use 'sync <node_index>' to synchronize nodes with this algorithm.");
            
            // Give a hint about the algorithm type
            if (newAlgorithm.toLowerCase().contains("berkeley")) {
                System.out.println("[INFO] This algorithm will use all nodes for synchronization.");
            } else {
                System.out.println("[INFO] This algorithm will use the master node for synchronization.");
            }
        } else {
            System.out.println("[ERROR] Unknown algorithm: " + algorithmName);
            System.out.println("\nAvailable algorithms:");
            for (String algo : AlgorithmManager.getAlgorithmNames()) {
                System.out.println("  - " + algo);
            }
        }
    }
    
    /**
     * Displays information about available clock synchronization algorithms.
     */
    private static void showAlgorithms() {
        System.out.println(AlgorithmManager.getAlgorithmInfo());
    }
    
    /**
     * Provides an explanation of physical clock synchronization.
     */
    private static void explainPhysicalClockSync() {
        System.out.println("\n=== PHYSICAL CLOCK SYNCHRONIZATION EXPLAINED ===");
        System.out.println("Physical clock synchronization is essential in distributed systems because");
        System.out.println("each computer's physical clock tends to drift at different rates due to");
        System.out.println("hardware variations, temperature, and other factors.");
        System.out.println();
        System.out.println("KEY CONCEPTS:");
        System.out.println("-------------");
        System.out.println("1. Clock Drift: A physical clock's tendency to run faster or slower than");
        System.out.println("   real time, typically measured in parts per million (ppm).");
        System.out.println();
        System.out.println("2. Drift Rate: In this simulation, we've implemented drift rates as");
        System.out.println("   multipliers (e.g., 1.02 = 2% faster than normal).");
        System.out.println();
        System.out.println("3. Synchronization Algorithms: Different methods to keep clocks aligned:");
        System.out.println();
        
        // Add algorithm-specific explanations
        System.out.println("   CRISTIAN'S ALGORITHM:");
        System.out.println("   - Simple master-slave approach");
        System.out.println("   - Clients request time from a time server");
        System.out.println("   - Accounts for network delay using round-trip time");
        System.out.println("   - Formula: server_time + (RTT/2)");
        System.out.println("   - Advantages: Simple, low overhead");
        System.out.println("   - Disadvantages: Single point of failure (the master)");
        System.out.println();
        
        System.out.println("   BERKELEY ALGORITHM:");
        System.out.println("   - Fault-tolerant, democratic approach");
        System.out.println("   - Coordinator collects time from all nodes");
        System.out.println("   - Calculates average time and sends adjustments to each node");
        System.out.println("   - Doesn't require an accurate time source");
        System.out.println("   - Advantages: More resilient to node failures");
        System.out.println("   - Disadvantages: More complex, higher message overhead");
        System.out.println();
        
        System.out.println("This simulation allows you to observe how these algorithms perform");
        System.out.println("with clocks running at different rates. Use the 'monitor' command");
        System.out.println("to watch clocks drift, and then see how they are corrected through");
        System.out.println("synchronization with the 'sync' command.");
        System.out.println();
        
        System.out.println("To switch between algorithms, use the 'algorithm' command.");
        System.out.println("=================================================");
    }
    
    /**
     * Shows the algorithm selection menu at startup.
     */
    private static void showAlgorithmSelectionMenu() {
        Scanner scanner = new Scanner(System.in);
        
        // Show algorithm info
        showAlgorithms();
        
        // Prompt for algorithm selection
        System.out.println("\n[SYSTEM] Please select a synchronization algorithm to use:");
        String[] algorithms = AlgorithmManager.getAlgorithmNames();
        
        for (int i = 0; i < algorithms.length; i++) {
            System.out.printf("  %d. %s\n", i+1, algorithms[i]);
        }
        
        boolean validSelection = false;
        while (!validSelection) {
            System.out.print("\nEnter your choice (1-" + algorithms.length + "): ");
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice >= 1 && choice <= algorithms.length) {
                    String selectedAlgorithm = algorithms[choice - 1];
                    AlgorithmManager.setActiveAlgorithm(selectedAlgorithm);
                    System.out.println("\n[SYSTEM] ✓ Selected algorithm: " + 
                        AlgorithmManager.getActiveAlgorithm().getName());
                    validSelection = true;
                } else {
                    System.out.println("[ERROR] Invalid choice. Please enter a number between 1 and " + 
                                    algorithms.length);
                }
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Please enter a valid number.");
            }
        }
        
        // Display additional info about the selected algorithm
        System.out.println("\n=== SELECTED ALGORITHM DETAILS ===");
        System.out.println(AlgorithmManager.getActiveAlgorithm().getName() + ":");
        System.out.println("  " + AlgorithmManager.getActiveAlgorithm().getDescription());
        System.out.println("\n[SYSTEM] Use the 'sync <node_index>' command to synchronize nodes using this algorithm.");
        System.out.println("[SYSTEM] You can change the algorithm anytime with the 'algorithm' command.");
    }
    
    /**
     * Stops all running nodes.
     */
    private static void stopAllNodes() {
        for (Node node : nodes) {
            try {
                node.stop();
            } catch (Exception e) {
                logger.error("Error stopping node " + node.getNodeName(), e);
            }
        }
        nodes.clear();
    }
} 