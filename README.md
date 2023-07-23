# Limit Order Book Project

This project simulates a Limit Order Book like those commonly used in trading systems. A Limit Order Book is a data structure used in trading for the purpose of maintaining a list of orders from market participants. This list can be used by a matching engine to determine the best price to match buy and sell orders.

## Features
- Add new orders to the order book
- Remove existing orders by id
- Change the size of existing orders
- Get the price at a specific level on a side of the book
- Get the total size of orders at a specific level on a side of the book
- Get all orders on a side of the book sorted by price-time priority

## Getting Started

### Prerequisites
- Java 17
- Maven

### Building the Project
To build the project, navigate to the root directory of the project in your terminal and run:

```shell
mvn clean install
```

### Running Tests
To run the unit tests, you can use the following Maven command:

```shell
mvn test
```

## Modifications or additions to the Order and/or OrderBook classes make them better suited to support real-life, latency-sensitive trading operations
1. Multithreading Support
   - Use thread-safe data structures like `ConcurentHashMap` instead of `HashMap`, and `ConcurrentSkipListSet` instead of `TreeSet`
2. Add a `timestamp` field to the `Order` class, which could help maintaining the order in the OrderBook
3. Add Performance Metrics: Implement metrics to measure the latency and throughput of operations on the order book
4. Caching: To reduce latency, the top levels of the order book can be cached in a different data structure