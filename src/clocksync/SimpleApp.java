package clocksync;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

/**
 * A simplified version of the clock synchronization application that doesn't require 
 * external dependencies.
 */
public class SimpleApp {
    
    private static final List<SimpleClock> clocks = new ArrayList<>();
    private static final DateTimeFormatter formatter = 
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());
    
    // Available algorithms
    private static final String[] algorithms = {
        "Cristian's Algorithm", 
        "Berkeley Algorithm",
        "NTP-like Algorithm"
    };
    private static String currentAlgorithm = algorithms[0];
    
    public static void main(String[] args) {
        printWelcomeBanner();
        
        // Create clocks with different drift rates
        System.out.println("\n[SYSTEM] Creating clocks with different drift rates...");
        
        SimpleClock masterClock = new SimpleClock("Master Clock", 1.0);
        clocks.add(masterClock);
        System.out.println("[SYSTEM] ✓ Created Master Clock with drift rate: 1.0 (reference time)");
        
        SimpleClock fastClock = new SimpleClock("Fast Clock", 1.02);
        clocks.add(fastClock);
        System.out.println("[SYSTEM] ✓ Created Fast Clock with drift rate: 1.02 (2% faster)");
        
        SimpleClock normalClock = new SimpleClock("Normal Clock", 1.0);
        clocks.add(normalClock);
        System.out.println("[SYSTEM] ✓ Created Normal Clock with drift rate: 1.0 (normal speed)");
        
        SimpleClock slowClock = new SimpleClock("Slow Clock", 0.98);
        clocks.add(slowClock);
        System.out.println("[SYSTEM] ✓ Created Slow Clock with drift rate: 0.98 (2% slower)");
        
        // Show algorithm selection
        showAlgorithmSelection();
        
        // Start console
        System.out.println("\n-------------------------------------------------------------");
        System.out.println("INTERACTIVE CONSOLE READY - Type 'help' for available commands");
        System.out.println("-------------------------------------------------------------");
        runConsole();
    }
    
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
        System.out.println("   - NTP-like algorithm for enhanced precision using multiple samples");
        System.out.println("=================================================================\n");
    }
    
    private static void showAlgorithmSelection() {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("\n=============== ALGORITHM SELECTION ===============");
        System.out.println("Please select a clock synchronization algorithm:");
        
        for (int i = 0; i < algorithms.length; i++) {
            System.out.println("\n" + (i+1) + ". " + algorithms[i]);
            
            // Add description for each algorithm
            switch (i) {
                case 0: // Cristian's Algorithm
                    System.out.println("   Description: Master-slave synchronization where clients");
                    System.out.println("   request time from a server and adjust for network delay.");
                    System.out.println("   Formula: server_time + (round_trip_time/2)");
                    break;
                case 1: // Berkeley Algorithm
                    System.out.println("   Description: Democratic approach where a coordinator");
                    System.out.println("   calculates average time from all nodes and distributes");
                    System.out.println("   adjustment values. Both master and clients adjust their time.");
                    break;
                case 2: // NTP-like Algorithm
                    System.out.println("   Description: Enhanced precision using multiple samples and");
                    System.out.println("   statistical techniques to filter inaccurate readings.");
                    System.out.println("   Uses multiple measurements to improve accuracy.");
                    break;
            }
        }
        
        boolean validSelection = false;
        while (!validSelection) {
            System.out.print("\nEnter your choice (1-" + algorithms.length + "): ");
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice >= 1 && choice <= algorithms.length) {
                    currentAlgorithm = algorithms[choice-1];
                    validSelection = true;
                    
                    System.out.println("\n[SYSTEM] ✓ Selected algorithm: " + currentAlgorithm);
                    System.out.println("[SYSTEM] All clock synchronization will use this algorithm.");
                    
                    // Show brief reminder of how to change algorithm later
                    System.out.println("[SYSTEM] You can change algorithms anytime with the 'algorithm' command.");
                } else {
                    System.out.println("[ERROR] Invalid choice. Please enter a number between 1 and " + algorithms.length);
                }
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Please enter a valid number.");
            }
        }
    }
    
    private static void runConsole() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        
        while (running) {
            System.out.print("\n> ");
            String command = scanner.nextLine().trim();
            
            if (command.isEmpty()) {
                continue;
            }
            
            String[] parts = command.split("\\s+", 2);
            String cmd = parts[0].toLowerCase();
            
            switch (cmd) {
                case "help":
                    showHelp();
                    break;
                    
                case "list":
                    listClocks();
                    break;
                    
                case "time":
                    showTime();
                    break;
                    
                case "sync":
                    if (parts.length > 1) {
                        try {
                            int index = Integer.parseInt(parts[1]) - 1;
                            if (index >= 0 && index < clocks.size()) {
                                syncClock(clocks.get(index));
                            } else {
                                System.out.println("[ERROR] Invalid clock index. Use 'list' to see available clocks.");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("[ERROR] Invalid number format. Usage: sync <index>");
                        }
                    } else {
                        System.out.println("[INFO] Usage: sync <index>");
                    }
                    break;
                    
                case "algorithm":
                    if (parts.length > 1) {
                        int algo = 0;
                        try {
                            algo = Integer.parseInt(parts[1]) - 1;
                            if (algo >= 0 && algo < algorithms.length) {
                                currentAlgorithm = algorithms[algo];
                                System.out.println("[SYSTEM] Changed algorithm to: " + currentAlgorithm);
                            } else {
                                System.out.println("[ERROR] Invalid algorithm index.");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("[ERROR] Invalid number. Usage: algorithm <index>");
                        }
                    } else {
                        showAlgorithms();
                    }
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
                    
                case "exit":
                case "quit":
                    running = false;
                    System.out.println("[SYSTEM] Exiting application...");
                    break;
                    
                default:
                    System.out.println("[ERROR] Unknown command. Type 'help' for available commands.");
            }
        }
        
        scanner.close();
    }
    
    private static void showHelp() {
        System.out.println("\n=== AVAILABLE COMMANDS ===");
        System.out.println("  ALGORITHM SELECTION:");
        System.out.println("  algorithm    - Show available synchronization algorithms");
        System.out.println("  algorithm <n> - Change the active synchronization algorithm");
        System.out.println();
        System.out.println("  SYNCHRONIZATION:");
        System.out.println("  sync <idx>   - Synchronize clock at index <idx> using the selected algorithm");
        System.out.println();
        System.out.println("  MONITORING AND INFORMATION:");
        System.out.println("  help         - Display this help message");
        System.out.println("  list         - List all clocks");
        System.out.println("  time         - Show current time on all clocks");
        System.out.println("  monitor <s>  - Monitor clock times for s seconds");
        System.out.println();
        System.out.println("  SYSTEM:");
        System.out.println("  exit/quit    - Exit the application");
        System.out.println("=========================");
        
        System.out.println("\nACTIVE ALGORITHM: " + currentAlgorithm);
        
        if (currentAlgorithm.contains("Cristian")) {
            System.out.println("Uses master-slave approach with round-trip time correction");
        } else if (currentAlgorithm.contains("Berkeley")) {
            System.out.println("Uses a democratic averaging approach adjusting all clocks");
        } else if (currentAlgorithm.contains("NTP")) {
            System.out.println("Uses multiple samples to select the most accurate time adjustment");
        }
    }
    
    private static void listClocks() {
        System.out.println("\n=== CLOCKS IN THE SYSTEM ===");
        for (int i = 0; i < clocks.size(); i++) {
            SimpleClock clock = clocks.get(i);
            System.out.printf("%d. %-15s (Drift rate: %.2f)%s\n", 
                i+1, clock.getName(), clock.getDriftRate(),
                i == 0 ? " [MASTER]" : "");
        }
    }
    
    private static void showTime() {
        System.out.println("\n=== CURRENT CLOCK TIMES ===");
        SimpleClock masterClock = clocks.get(0);
        for (int i = 0; i < clocks.size(); i++) {
            SimpleClock clock = clocks.get(i);
            Instant time = clock.getTime();
            System.out.printf("%d. %-15s: %s", i+1, clock.getName(), formatter.format(time));
            
            if (i > 0) {
                long diff = Math.abs(clock.getTimeMillis() - masterClock.getTimeMillis());
                String indicator = "";
                if (diff > 0) {
                    indicator = clock.getTimeMillis() > masterClock.getTimeMillis() ? " (ahead)" : " (behind)";
                }
                System.out.printf(" [Diff: %d ms%s]", diff, indicator);
            }
            System.out.println();
        }
    }
    
    private static void showAlgorithms() {
        System.out.println("\n=== AVAILABLE SYNCHRONIZATION ALGORITHMS ===");
        
        for (int i = 0; i < algorithms.length; i++) {
            String active = algorithms[i].equals(currentAlgorithm) ? " [ACTIVE]" : "";
            System.out.println("\n" + (i+1) + ". " + algorithms[i] + active);
            
            // Add description for each algorithm
            switch (i) {
                case 0: // Cristian's Algorithm
                    System.out.println("   Description: Master-slave synchronization where clients");
                    System.out.println("   request time from a server and adjust for network delay.");
                    System.out.println("   Formula: server_time + (round_trip_time/2)");
                    System.out.println("   Best for: Simple hierarchical systems with a reliable time source");
                    break;
                case 1: // Berkeley Algorithm
                    System.out.println("   Description: Democratic approach where a coordinator");
                    System.out.println("   calculates average time from all nodes and distributes");
                    System.out.println("   adjustment values. Both master and clients adjust their time.");
                    System.out.println("   Best for: Distributed systems where no node has an authoritative time");
                    break;
                case 2: // NTP-like Algorithm
                    System.out.println("   Description: Enhanced precision using multiple samples and");
                    System.out.println("   statistical techniques to filter inaccurate readings.");
                    System.out.println("   Uses multiple measurements to improve accuracy.");
                    System.out.println("   Best for: High-precision requirements with variable network delays");
                    break;
            }
        }
        
        System.out.println("\nTo change algorithm: algorithm <number>");
    }
    
    private static void syncClock(SimpleClock clock) {
        if (clock == clocks.get(0)) {
            System.out.println("[ERROR] Cannot synchronize the master clock.");
            return;
        }
        
        System.out.println("\n=== CLOCK SYNCHRONIZATION ===");
        System.out.println("Algorithm: " + currentAlgorithm);
        System.out.println("Clock to sync: " + clock.getName());
        
        // Get times before sync
        SimpleClock masterClock = clocks.get(0);
        long masterTime = masterClock.getTimeMillis();
        long clockTime = clock.getTimeMillis();
        
        System.out.println("\nBefore synchronization:");
        System.out.printf("  %-15s: %s\n", clock.getName(), formatter.format(clock.getTime()));
        System.out.printf("  %-15s: %s\n", masterClock.getName(), formatter.format(masterClock.getTime()));
        
        // Perform synchronization
        System.out.println("\nPerforming synchronization...");
        
        long adjustment = 0;
        long rtt = 0;
        
        if (currentAlgorithm.contains("Cristian")) {
            // Simulate network delay
            rtt = (long)(Math.random() * 50) + 10; // 10-60ms RTT
            long oneWayDelay = rtt / 2;
            
            // Calculate new time using Cristian's algorithm
            masterTime = masterClock.getTimeMillis();
            adjustment = (masterTime + oneWayDelay) - clock.getTimeMillis();
            
            // Apply adjustment
            clock.adjustTime(adjustment);
            
            System.out.println("\n[Cristian's Algorithm]");
            System.out.println("Master time: " + masterTime + " ms");
            System.out.println("Network delay (RTT): " + rtt + " ms");
            System.out.println("One-way delay estimate: " + oneWayDelay + " ms");
            
        } else if (currentAlgorithm.contains("Berkeley")) {
            // Calculate average time of all clocks
            long sum = 0;
            StringBuilder clockReadings = new StringBuilder("Clock readings:\n");
            
            for (SimpleClock c : clocks) {
                long time = c.getTimeMillis();
                sum += time;
                clockReadings.append(String.format("  %-15s: %d ms\n", c.getName(), time));
            }
            long avgTime = sum / clocks.size();
            
            // Calculate and apply adjustment
            adjustment = avgTime - clock.getTimeMillis();
            clock.adjustTime(adjustment);
            
            // For Berkeley, we also adjust the master clock slightly
            long masterAdjustment = avgTime - masterClock.getTimeMillis();
            masterClock.adjustTime(masterAdjustment);
            
            System.out.println("\n[Berkeley Algorithm]");
            System.out.println(clockReadings.toString());
            System.out.println("Average time: " + avgTime + " ms");
            System.out.println("Master adjustment: " + masterAdjustment + " ms");
            
        } else if (currentAlgorithm.contains("NTP")) {
            try {
                // Simulate multiple time requests (NTP typically uses 8 samples)
                final int SAMPLES = 8;
                long[] roundTrips = new long[SAMPLES];
                long[] offsets = new long[SAMPLES];
                
                System.out.println("\n[NTP-like Algorithm]");
                System.out.println("Taking " + SAMPLES + " time samples...");
                
                for (int i = 0; i < SAMPLES; i++) {
                    // Simulate request time
                    long t1 = System.currentTimeMillis();
                    
                    // Simulate network delay (more variable for realism)
                    Thread.sleep(5 + (long)(Math.random() * 70));
                    
                    // Get server time
                    long t2 = masterClock.getTimeMillis();
                    
                    // Simulate network delay for return
                    Thread.sleep(5 + (long)(Math.random() * 70));
                    
                    // Receive time
                    long t3 = System.currentTimeMillis();
                    
                    // Calculate round trip time and offset
                    roundTrips[i] = (t3 - t1) - (t2 - t2); // In real NTP this would use server processing time
                    offsets[i] = ((t2 - t1) + (t2 - t3)) / 2;
                    
                    System.out.printf("Sample %d: RTT=%d ms, Offset=%d ms\n", i+1, roundTrips[i], offsets[i]);
                }
                
                // Find the sample with the smallest round trip time
                int bestSample = 0;
                for (int i = 1; i < SAMPLES; i++) {
                    if (roundTrips[i] < roundTrips[bestSample]) {
                        bestSample = i;
                    }
                }
                
                rtt = roundTrips[bestSample];
                adjustment = offsets[bestSample];
                
                // Apply the offset with the smallest round trip time
                clock.adjustTime(adjustment);
                
                System.out.println("\nSelected sample " + (bestSample+1) + " with RTT: " + rtt + " ms");
            } catch (InterruptedException e) {
                System.out.println("[ERROR] NTP synchronization was interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
                return;
            }
        }
        
        // Show results
        System.out.println("\n✓ Synchronization successful!");
        System.out.println("Clock adjustment: " + adjustment + " ms");
        if (currentAlgorithm.contains("Cristian") || currentAlgorithm.contains("NTP")) {
            System.out.println("Round-trip time: " + rtt + " ms");
            System.out.println("Estimated error: ±" + (rtt/2) + " ms");
        }
        
        // Show after times
        System.out.println("\nAfter synchronization:");
        System.out.printf("  %-15s: %s\n", clock.getName(), formatter.format(clock.getTime()));
        System.out.printf("  %-15s: %s\n", masterClock.getName(), formatter.format(masterClock.getTime()));
        
        // Show current difference
        long currentDiff = Math.abs(clock.getTimeMillis() - masterClock.getTimeMillis());
        System.out.println("\nCurrent time difference: " + currentDiff + " ms");
    }
    
    private static void monitorClocks(int seconds) throws InterruptedException {
        if (seconds <= 0 || seconds > 60) {
            System.out.println("[ERROR] Please specify a monitoring period between 1 and 60 seconds.");
            return;
        }
        
        System.out.println("\n=== MONITORING CLOCK TIMES FOR " + seconds + " SECONDS ===");
        System.out.println("Press Ctrl+C to stop monitoring early.");
        System.out.println();
        
        // Column headers
        System.out.printf("%-10s", "TIME");
        for (SimpleClock clock : clocks) {
            System.out.printf(" | %-15s", clock.getName());
        }
        System.out.println();
        
        // Separator line
        System.out.print("-".repeat(10));
        for (int i = 0; i < clocks.size(); i++) {
            System.out.print("-|-" + "-".repeat(15));
        }
        System.out.println();
        
        // Monitor for the specified duration
        for (int i = 0; i <= seconds; i++) {
            System.out.printf("%-10d", i);
            
            for (SimpleClock clock : clocks) {
                System.out.printf(" | %-15s", formatter.format(clock.getTime()));
            }
            System.out.println();
            
            if (i < seconds) {
                Thread.sleep(1000);
            }
        }
    }
    
    /**
     * A simple clock implementation with drift.
     */
    private static class SimpleClock {
        private final String name;
        private long time;
        private double driftRate;
        private long lastUpdateTime;
        
        public SimpleClock(String name, double driftRate) {
            this.name = name;
            this.driftRate = driftRate;
            this.time = System.currentTimeMillis();
            this.lastUpdateTime = System.currentTimeMillis();
            
            // Start the clock thread
            Thread clockThread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Thread.sleep(100);
                        updateTime();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
            clockThread.setDaemon(true);
            clockThread.start();
        }
        
        private synchronized void updateTime() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastUpdateTime;
            time += (long) (elapsed * driftRate);
            lastUpdateTime = now;
        }
        
        public synchronized long getTimeMillis() {
            updateTime();
            return time;
        }
        
        public synchronized Instant getTime() {
            return Instant.ofEpochMilli(getTimeMillis());
        }
        
        public synchronized void adjustTime(long adjustment) {
            updateTime();
            time += adjustment;
        }
        
        public String getName() {
            return name;
        }
        
        public double getDriftRate() {
            return driftRate;
        }
    }
} 