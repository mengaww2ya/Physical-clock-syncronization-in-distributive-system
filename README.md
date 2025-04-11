# Physical Clock Synchronization System

A simulation of different clock synchronization algorithms used in distributed systems. This application demonstrates how physical clocks are synchronized using various algorithms and allows interactive comparison between them.

## Features

- Multiple clock synchronization algorithms:
  - **Cristian's Algorithm**: Simple master-slave synchronization
  - **Berkeley Algorithm**: Democratic, fault-tolerant synchronization
  - **NTP-like Algorithm**: Enhanced precision using multiple samples

- Interactive command console
- Real-time clock monitoring
- Configurable drift rates
- Detailed synchronization metrics
- Educational output with algorithm explanations

## Requirements

- Java JDK 8 or higher

## Installation

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/clock-synchronization.git
   cd clock-synchronization
   ```

2. Make the run script executable:
   ```
   chmod +x run-simple.sh
   ```

## Running the Application

Execute the run script:
```
./run-simple.sh
```

This will compile and run the application. You'll be presented with an algorithm selection menu at startup.

## Usage Guide

After starting the application:

1. Select a synchronization algorithm when prompted
2. Use the interactive console to explore the system
3. Type `help` to see all available commands
4. Try the example command sequence in `commands.txt`

### Common Command Sequence

```
help                   (show available commands)
list                   (see all clocks)
time                   (check current times)
monitor 10             (observe drift for 10 seconds)
sync 2                 (synchronize the Fast Clock)
time                   (verify synchronization)
algorithm 2            (switch to Berkeley Algorithm)
sync 4                 (synchronize with Berkeley)
time                   (check results)
```

## Reference Files

- `commands.txt` - All available commands with explanations
- `algorithm_guide.txt` - Detailed explanation of each algorithm

## Understanding Clock Synchronization

This simulation demonstrates key concepts in distributed systems:

1. **Clock Drift**: Physical clocks naturally drift at different rates
2. **Synchronization Need**: Without correction, distributed systems would have inconsistent time
3. **Algorithm Tradeoffs**: Different approaches have varying precision, complexity, and fault-tolerance

Each algorithm in this system represents a different approach to solving the clock synchronization problem, allowing for educational comparison. 