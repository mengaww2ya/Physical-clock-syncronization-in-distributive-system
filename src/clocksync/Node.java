package clocksync;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Represents a node in a distributed system that maintains a physical clock.
 * The node can synchronize its clock with other nodes using a simple protocol.
 */
public class Node {
    private static final Logger logger = LoggerFactory.getLogger(Node.class);
    private static final int BUFFER_SIZE = 4096;
    private static final Gson gson = new Gson();
    
    // Node identity
    private final String nodeId;
    private final String nodeName;
    private boolean isMaster;
    
    // Physical clock for this node
    private final PhysicalClock clock;
    
    // Network configuration
    private final int port;
    private final MulticastSocket socket;
    private final InetAddress multicastGroup;
    private final int multicastPort;
    
    // Known peers in the network
    private final Map<String, NodeInfo> knownNodes = new ConcurrentHashMap<>();
    
    // Thread pools for scheduled tasks
    private final ScheduledExecutorService scheduler;
    
    /**
     * Creates a new node with the specified configuration.
     *
     * @param nodeName The human-readable name of this node
     * @param port The port for this node to listen on
     * @param multicastGroup The multicast group address (e.g., "224.0.0.1")
     * @param multicastPort The multicast port
     * @param isMaster Whether this node is a master node
     * @throws IOException If there's an error setting up the network
     */
    public Node(String nodeName, int port, String multicastGroup, int multicastPort, boolean isMaster) throws IOException {
        this.nodeId = UUID.randomUUID().toString();
        this.nodeName = nodeName;
        this.port = port;
        this.isMaster = isMaster;
        this.clock = new PhysicalClock();
        
        // Set up a random drift rate to simulate clock drift
        this.clock.setDriftRate(0.98 + Math.random() * 0.04);
        
        // Set up networking
        this.socket = new MulticastSocket(port);
        this.multicastGroup = InetAddress.getByName(multicastGroup);
        this.multicastPort = multicastPort;
        
        // Join the multicast group
        socket.joinGroup(new InetSocketAddress(this.multicastGroup, 0), NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));
        
        // Set up thread pool
        this.scheduler = Executors.newScheduledThreadPool(2);
        
        logger.info("Node {} (ID: {}) started with clock drift rate {}", 
                nodeName, nodeId, clock.getDriftRate());
    }
    
    /**
     * Starts this node, beginning clock synchronization and discovery.
     */
    public void start() {
        // Start a thread to listen for incoming messages
        startListenerThread();
        
        // Schedule regular tasks
        if (isMaster) {
            // If this is a master node, announce time regularly
            scheduler.scheduleAtFixedRate(this::announceMasterTime, 1, 10, TimeUnit.SECONDS);
        } else {
            // If this is a regular node, request time sync regularly
            scheduler.scheduleAtFixedRate(this::requestTimeSync, 5, 30, TimeUnit.SECONDS);
        }
        
        // All nodes perform discovery to find other nodes
        scheduler.scheduleAtFixedRate(this::performDiscovery, 0, 20, TimeUnit.SECONDS);
    }
    
    /**
     * Stops this node, releasing all resources.
     */
    public void stop() {
        try {
            scheduler.shutdown();
            scheduler.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.warn("Shutdown interrupted", e);
            Thread.currentThread().interrupt();
        }
        
        try {
            socket.leaveGroup(new InetSocketAddress(this.multicastGroup, 0), 
                    NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));
            socket.close();
        } catch (IOException e) {
            logger.error("Error closing socket", e);
        }
        
        logger.info("Node {} stopped", nodeName);
    }
    
    /**
     * Starts a thread that listens for incoming messages.
     */
    private void startListenerThread() {
        Thread listenerThread = new Thread(() -> {
            byte[] buffer = new byte[BUFFER_SIZE];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            
            while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                try {
                    // Receive a packet
                    socket.receive(packet);
                    
                    // Extract the message
                    String json = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                    ClockSyncMessage message = gson.fromJson(json, ClockSyncMessage.class);
                    
                    // Process the message
                    handleMessage(message, packet.getAddress(), packet.getPort());
                    
                    // Reset the packet size for the next receive
                    packet.setLength(buffer.length);
                } catch (IOException e) {
                    if (!socket.isClosed()) {
                        logger.error("Error receiving packet", e);
                    }
                }
            }
        });
        
        listenerThread.setDaemon(true);
        listenerThread.setName("listener-thread");
        listenerThread.start();
    }
    
    /**
     * Handles an incoming synchronization message.
     *
     * @param message The received message
     * @param senderAddress The sender's address
     * @param senderPort The sender's port
     */
    private void handleMessage(ClockSyncMessage message, InetAddress senderAddress, int senderPort) {
        // Skip messages from self
        if (nodeId.equals(message.getNodeId())) {
            return;
        }
        
        // Update known nodes list
        knownNodes.put(message.getNodeId(), new NodeInfo(message.getNodeId(), senderAddress, senderPort));
        
        // Process based on message type
        switch (message.getType()) {
            case SYNC_REQUEST:
                if (isMaster) {
                    // Send a response with the current time
                    sendSyncResponse(message.getNodeId(), senderAddress, senderPort);
                }
                break;
                
            case SYNC_RESPONSE:
                // Only process response if we're not a master
                if (!isMaster) {
                    // Adjust our clock based on the received time
                    long adjustment = clock.synchronize(message.getSenderTime());
                    logger.info("Synchronized clock with node {}, adjustment: {} ms", 
                            message.getNodeId(), adjustment);
                }
                break;
                
            case MASTER_ANNOUNCE:
                // Only non-master nodes adjust their clocks based on master announcements
                if (!isMaster) {
                    long adjustment = clock.synchronize(message.getSenderTime());
                    logger.info("Received master time announcement from {}, adjustment: {} ms", 
                            message.getNodeId(), adjustment);
                }
                break;
        }
    }
    
    /**
     * Sends a synchronization response to a specific node.
     *
     * @param targetNodeId The ID of the node to send to
     * @param targetAddress The address of the target node
     * @param targetPort The port of the target node
     */
    private void sendSyncResponse(String targetNodeId, InetAddress targetAddress, int targetPort) {
        ClockSyncMessage response = new ClockSyncMessage(
                nodeId, 
                clock.getTime(), 
                ClockSyncMessage.MessageType.SYNC_RESPONSE
        );
        
        sendMessage(response, targetAddress, targetPort);
    }
    
    /**
     * Broadcasts a request for time synchronization.
     */
    private void requestTimeSync() {
        ClockSyncMessage request = new ClockSyncMessage(
                nodeId,
                clock.getTime(),
                ClockSyncMessage.MessageType.SYNC_REQUEST
        );
        
        sendMulticast(request);
        logger.debug("Sent time synchronization request");
    }
    
    /**
     * Broadcasts the master time to all nodes (only used by master nodes).
     */
    private void announceMasterTime() {
        if (!isMaster) {
            return;
        }
        
        ClockSyncMessage announcement = new ClockSyncMessage(
                nodeId,
                clock.getTime(),
                ClockSyncMessage.MessageType.MASTER_ANNOUNCE
        );
        
        sendMulticast(announcement);
        logger.debug("Announced master time: {}", clock.getTimeAsInstant());
    }
    
    /**
     * Performs node discovery by announcing presence on the network.
     */
    private void performDiscovery() {
        // Use a sync request as a form of discovery
        ClockSyncMessage discoveryMessage = new ClockSyncMessage(
                nodeId,
                clock.getTime(),
                ClockSyncMessage.MessageType.SYNC_REQUEST
        );
        
        sendMulticast(discoveryMessage);
        logger.debug("Performed discovery, known nodes: {}", knownNodes.size());
    }
    
    /**
     * Sends a message to a specific node.
     *
     * @param message The message to send
     * @param targetAddress The address to send to
     * @param targetPort The port to send to
     */
    public void sendMessage(ClockSyncMessage message, InetAddress targetAddress, int targetPort) {
        try {
            String json = gson.toJson(message);
            byte[] buffer = json.getBytes(StandardCharsets.UTF_8);
            
            DatagramPacket packet = new DatagramPacket(
                    buffer, buffer.length, targetAddress, targetPort);
            
            socket.send(packet);
        } catch (IOException e) {
            logger.error("Failed to send message to {}:{}", targetAddress, targetPort, e);
        }
    }
    
    /**
     * Sends a message to the multicast group.
     *
     * @param message The message to send
     */
    private void sendMulticast(ClockSyncMessage message) {
        try {
            String json = gson.toJson(message);
            byte[] buffer = json.getBytes(StandardCharsets.UTF_8);
            
            DatagramPacket packet = new DatagramPacket(
                    buffer, buffer.length, multicastGroup, multicastPort);
            
            socket.send(packet);
        } catch (IOException e) {
            logger.error("Failed to send multicast message", e);
        }
    }
    
    /**
     * Gets the current clock time of this node.
     *
     * @return The current time
     */
    public long getTime() {
        return clock.getTime();
    }
    
    /**
     * Gets the node ID.
     *
     * @return The node ID
     */
    public String getNodeId() {
        return nodeId;
    }
    
    /**
     * Gets the node name.
     *
     * @return The node name
     */
    public String getNodeName() {
        return nodeName;
    }
    
    /**
     * Gets the physical clock for this node.
     *
     * @return The physical clock
     */
    public PhysicalClock getPhysicalClock() {
        return clock;
    }
    
    /**
     * Checks if this node is a master node.
     *
     * @return True if this is a master node, false otherwise
     */
    public boolean isMaster() {
        return isMaster;
    }
    
    /**
     * Sets whether this node is a master node.
     *
     * @param master True to make this node a master, false otherwise
     */
    public void setMaster(boolean master) {
        isMaster = master;
    }
    
    /**
     * Information about a known node in the network.
     */
    private static class NodeInfo {
        private final String nodeId;
        private final InetAddress address;
        private final int port;
        
        public NodeInfo(String nodeId, InetAddress address, int port) {
            this.nodeId = nodeId;
            this.address = address;
            this.port = port;
        }
    }
} 