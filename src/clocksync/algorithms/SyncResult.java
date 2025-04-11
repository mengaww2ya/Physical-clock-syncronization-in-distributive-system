package clocksync.algorithms;

import java.time.Instant;

/**
 * Contains the results of a clock synchronization operation.
 */
public class SyncResult {
    private final boolean success;
    private final long adjustmentMs;
    private final long roundTripTimeMs;
    private final double estimatedErrorMs;
    private final Instant beforeSync;
    private final Instant afterSync;
    private final String details;
    
    private SyncResult(Builder builder) {
        this.success = builder.success;
        this.adjustmentMs = builder.adjustmentMs;
        this.roundTripTimeMs = builder.roundTripTimeMs;
        this.estimatedErrorMs = builder.estimatedErrorMs;
        this.beforeSync = builder.beforeSync;
        this.afterSync = builder.afterSync;
        this.details = builder.details;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public long getAdjustmentMs() {
        return adjustmentMs;
    }
    
    public long getRoundTripTimeMs() {
        return roundTripTimeMs;
    }
    
    public double getEstimatedErrorMs() {
        return estimatedErrorMs;
    }
    
    public Instant getBeforeSync() {
        return beforeSync;
    }
    
    public Instant getAfterSync() {
        return afterSync;
    }
    
    public String getDetails() {
        return details;
    }
    
    /**
     * Builder for SyncResult.
     */
    public static class Builder {
        private boolean success = false;
        private long adjustmentMs = 0;
        private long roundTripTimeMs = 0;
        private double estimatedErrorMs = 0;
        private Instant beforeSync = null;
        private Instant afterSync = null;
        private String details = "";
        
        public Builder success(boolean success) {
            this.success = success;
            return this;
        }
        
        public Builder adjustmentMs(long adjustmentMs) {
            this.adjustmentMs = adjustmentMs;
            return this;
        }
        
        public Builder roundTripTimeMs(long roundTripTimeMs) {
            this.roundTripTimeMs = roundTripTimeMs;
            return this;
        }
        
        public Builder estimatedErrorMs(double estimatedErrorMs) {
            this.estimatedErrorMs = estimatedErrorMs;
            return this;
        }
        
        public Builder beforeSync(Instant beforeSync) {
            this.beforeSync = beforeSync;
            return this;
        }
        
        public Builder afterSync(Instant afterSync) {
            this.afterSync = afterSync;
            return this;
        }
        
        public Builder details(String details) {
            this.details = details;
            return this;
        }
        
        public SyncResult build() {
            return new SyncResult(this);
        }
    }
} 