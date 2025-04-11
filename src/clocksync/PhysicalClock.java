package clocksync;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents a physical clock in a distributed system.
 * The clock can be adjusted based on time from other nodes.
 */
public class PhysicalClock {
    // The current clock time in milliseconds
    private final AtomicLong clockTime;
    
    // The drift rate (how fast this clock runs compared to real time)
    // 1.0 means perfect synchronization with real time
    private volatile double driftRate = 1.0;
    
    // Maximum adjustment allowed in a single sync (to prevent major jumps)
    private static final long MAX_ADJUSTMENT_MS = 1000;

    /**
     * Creates a new physical clock initialized with the current system time.
     */
    public PhysicalClock() {
        this.clockTime = new AtomicLong(System.currentTimeMillis());
        
        // Start a thread to advance the clock regularly
        startClockThread();
    }

    /**
     * Creates a new physical clock with a specified initial time.
     * 
     * @param initialTimeMs The initial time in milliseconds
     */
    public PhysicalClock(long initialTimeMs) {
        this.clockTime = new AtomicLong(initialTimeMs);
        
        // Start a thread to advance the clock regularly
        startClockThread();
    }

    /**
     * Gets the current clock time.
     * 
     * @return The current time in milliseconds
     */
    public long getTime() {
        return clockTime.get();
    }

    /**
     * Gets the current clock time as an Instant.
     * 
     * @return The current time as an Instant
     */
    public Instant getTimeAsInstant() {
        return Instant.ofEpochMilli(getTime());
    }

    /**
     * Adjusts the clock based on the received time from another node.
     * Uses a simple algorithm to prevent drastic changes.
     * 
     * @param receivedTimeMs The time received from another node
     * @return The adjustment made in milliseconds
     */
    public long synchronize(long receivedTimeMs) {
        long currentTime = clockTime.get();
        long timeDifference = receivedTimeMs - currentTime;
        
        // Limit the adjustment to prevent drastic changes
        long adjustment = Math.min(Math.max(timeDifference, -MAX_ADJUSTMENT_MS), MAX_ADJUSTMENT_MS);
        
        if (adjustment != 0) {
            // Apply the adjustment
            clockTime.addAndGet(adjustment);
            
            // Adjust drift rate based on the synchronization
            // This is a simple approach - in production systems more complex algorithms would be used
            if (timeDifference > 0) {
                // Our clock is running slow, increase the drift rate slightly
                driftRate = Math.min(driftRate + 0.001, 1.1);
            } else if (timeDifference < 0) {
                // Our clock is running fast, decrease the drift rate slightly
                driftRate = Math.max(driftRate - 0.001, 0.9);
            }
        }
        
        return adjustment;
    }
    
    /**
     * Starts a background thread that advances the clock based on the drift rate.
     */
    private void startClockThread() {
        Thread clockThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Sleep for a short time
                    Thread.sleep(100);
                    
                    // Calculate how much time should pass based on the drift rate
                    long timeDelta = (long)(100 * driftRate);
                    
                    // Advance the clock
                    clockTime.addAndGet(timeDelta);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        
        clockThread.setDaemon(true);
        clockThread.setName("clock-advancement-thread");
        clockThread.start();
    }
    
    /**
     * Gets the current drift rate of this clock.
     * 
     * @return The drift rate
     */
    public double getDriftRate() {
        return driftRate;
    }
    
    /**
     * Sets the drift rate of this clock.
     * 
     * @param driftRate The new drift rate
     */
    public void setDriftRate(double driftRate) {
        if (driftRate < 0.5 || driftRate > 1.5) {
            throw new IllegalArgumentException("Drift rate must be between 0.5 and 1.5");
        }
        this.driftRate = driftRate;
    }
    
    @Override
    public String toString() {
        return "PhysicalClock{time=" + getTimeAsInstant() + ", drift=" + driftRate + "}";
    }
} 