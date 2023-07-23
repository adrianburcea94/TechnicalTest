package org.phi.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderBookTest {

    private OrderBook orderBook;

    @BeforeEach
    void setUp() {
        orderBook = new OrderBook();
    }

    @Test
    void testAddNewOrder() {
        Order newBidsOrder = new Order(1, 110.0, 'B', 10);
        orderBook.addNewOrder(newBidsOrder);
        orderBook.addNewOrder(new Order(2, 105.0, 'B', 25));
        orderBook.addNewOrder(new Order(3, 105.0, 'B', 40));

        Order newOffersOrder = new Order(4, 100.0, 'O', 10);
        orderBook.addNewOrder(newOffersOrder);
        orderBook.addNewOrder(new Order(5, 100.0, 'O', 100));
        orderBook.addNewOrder(new Order(6, 105.0, 'O', 250));
        orderBook.addNewOrder(new Order(7, 105.0, 'O', 300));
        orderBook.addNewOrder(new Order(8, 103.0, 'O', 200));


        List<Order> bidsPriceTimePriority = orderBook.getAllOrdersBySide('B');
        List<Order> offersPriceTimePriority = orderBook.getAllOrdersBySide('O');

        assertEquals(3, bidsPriceTimePriority.size());
        assertEquals(5, offersPriceTimePriority.size());

        assertEquals(1, bidsPriceTimePriority.get(0).getId());
        assertEquals(2, bidsPriceTimePriority.get(1).getId());
        assertEquals(3, bidsPriceTimePriority.get(2).getId());

        assertEquals(4, offersPriceTimePriority.get(0).getId());
        assertEquals(5, offersPriceTimePriority.get(1).getId());
        assertEquals(8, offersPriceTimePriority.get(2).getId());
        assertEquals(6, offersPriceTimePriority.get(3).getId());
        assertEquals(7, offersPriceTimePriority.get(4).getId());
    }

    @Test
    void testRemoveOrderById() {
        Order newOrder = new Order(1, 100.0, 'B', 10);
        orderBook.addNewOrder(newOrder);
        boolean result = orderBook.removeOrderById(1);
        assertTrue(result);
        assertEquals(0, orderBook.getAllOrdersBySide('B').size());
    }

    @Test
    void testChangeOrderSize() {
        Order newOrder = new Order(1, 100.0, 'B', 10);
        orderBook.addNewOrder(newOrder);
        orderBook.changeOrderSize(1, 20);
        assertEquals(20, orderBook.getAllOrdersBySide('B').get(0).getSize());
    }

    @Test
    void testGetPriceAtLevel() {
        Order order1 = new Order(1, 100.0, 'B', 10);
        Order order2 = new Order(2, 105.0, 'B', 10);
        orderBook.addNewOrder(order1);
        orderBook.addNewOrder(order2);
        double priceAtLevel = orderBook.getPriceAtLevel('B', 1);
        assertEquals(105.0, priceAtLevel);

        orderBook.addNewOrder(new Order(5, 100.0, 'O', 100));
        orderBook.addNewOrder(new Order(6, 105.0, 'O', 250));
        orderBook.addNewOrder(new Order(7, 105.0, 'O', 300));
        orderBook.addNewOrder(new Order(8, 103.0, 'O', 200));

        double priceAtLevelForOffers = orderBook.getPriceAtLevel('O', 1);
        assertEquals(100.0, priceAtLevelForOffers);
    }

    @Test
    void testGetTotalSizeOfOrdersAtLevel() {
        Order order1 = new Order(1, 100.0, 'B', 10);
        Order order2 = new Order(2, 100.0, 'B', 15);
        orderBook.addNewOrder(order1);
        orderBook.addNewOrder(order2);
        long totalSizeAtLevel = orderBook.getTotalSizeOfOrdersAtLevel('B', 1);
        assertEquals(25, totalSizeAtLevel);
    }
}
