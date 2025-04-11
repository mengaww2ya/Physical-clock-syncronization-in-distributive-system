package clocksync;

import java.io.Serializable;

/**
 * Represents a message used for clock synchronization between nodes.
 * This message contains the sender's time and other metadata.
 */
public class ClockSyncMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    // The ID of the node that sent this message
    private final String nodeId;
    
    // The time at the sender node when the message was sent (in milliseconds)
    private final long senderTime;
    
    // The type of message (request, response, etc.)
    private final MessageType type;
    
    // Enum to represent different types of synchronization messages
    public enum MessageType {
        SYNC_REQUEST,    // Request for time synchronization
        SYNC_RESPONSE,   // Response to a synchronization request
        MASTER_ANNOUNCE  // Announcement from a master node
    }
    
    /**
     * Creates a new clock synchronization message.
     * 
     * @param nodeId The ID of the sender node
     * @param senderTime The time at the sender when the message was created
     * @param type The type of message
     */
    public ClockSyncMessage(String nodeId, long senderTime, MessageType type) {
        this.nodeId = nodeId;
        this.senderTime = senderTime;
        this.type = type;
    }
    
    /**
     * Gets the ID of the node that sent this message.
     * 
     * @return The node ID
     */
    public String getNodeId() {
        return nodeId;
    }
    
    /**
     * Gets the time at the sender when this message was created.
     * 
     * @return The sender's time in milliseconds
     */
    public long getSenderTime() {
        return senderTime;
    }
    
    /**
     * Gets the type of this message.
     * 
     * @return The message type
     */
    public MessageType getType() {
        return type;
    }
    
    @Override
    public String toString() {
        return "ClockSyncMessage{" +
                "nodeId='" + nodeId + '\'' +
                ", senderTime=" + senderTime +
                ", type=" + type +
                '}';
    }
} 