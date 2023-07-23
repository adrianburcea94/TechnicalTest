package org.phi.model;

import java.util.*;

/**
 * An OrderBook maintains a collection of buy and sell orders, representing a financial instrument's
 * demand and supply at various price levels in a marketplace. The OrderBook data structure supports
 * operations to add, remove and modify orders.
 *
 * <p>Each side of the book (BID and OFFER) is stored as a map of price levels to a set of orders at that price,
 * sorted by their placement time in ascending order. An additional map for each side maintains the total
 * size of all orders at a particular price level.
 *
 * <p>A TreeSet is used to maintain sorted price levels for each side of the book. For bids, price levels are
 * sorted in descending order, whereas for offers, price levels are sorted in ascending order.
 *
 * <p>The orders are stored in a LinkedHashSet at each price level, which ensures that the orders are sorted
 * in the order they were placed (price-time priority).
 *
 * <p>For quick access, all orders are also stored in a map keyed by their unique order id.
 *
 * <p>The OrderBook ensures efficient execution of key operations - order addition, removal, modification, and
 * querying price/size at a particular level, with time complexity largely being O(1).
 *
 * <p>This implementation is not thread-safe. If concurrent access is required, the class should be appropriately
 * synchronized externally.
 *
 * @author adrian.burcea
 */
public class OrderBook {

    private final Map<Double, LinkedHashSet<Order>> bids;
    private final Map<Double, LinkedHashSet<Order>> offers;
    private final TreeSet<Double> priceLevelsBids;
    private final TreeSet<Double> priceLevelsOffers;
    private final Map<Double, Long> totalSizeOfBids;
    private final Map<Double, Long> totalSizeOfOffers;
    private final Map<Long, Order> allOrders;

    private static final char BID = 'B';
    private static final char OFFER = 'O';

    public OrderBook() {
        this.bids = new HashMap<>();
        this.offers = new HashMap<>();
        this.priceLevelsBids = new TreeSet<>(Comparator.reverseOrder());
        this.priceLevelsOffers = new TreeSet<>();
        this.totalSizeOfBids = new HashMap<>();
        this.totalSizeOfOffers = new HashMap<>();
        this.allOrders = new HashMap<>();
    }

    /**
     * Adds a new order to the order book
     *
     * @param newOrder the new order
     */
    public void addNewOrder(Order newOrder) {
        if (newOrder.getSide() == BID) {
            addOrder(newOrder, bids, priceLevelsBids, totalSizeOfBids);
        } else if (newOrder.getSide() == OFFER) {
            addOrder(newOrder, offers, priceLevelsOffers, totalSizeOfOffers);
        } else {
            throw new IllegalArgumentException("Invalid order side");
        }

        allOrders.put(newOrder.getId(), newOrder);
    }

    /**
     * Removes an order from the OrderBook by order id
     *
     * @param orderId the order id
     *
     * @return true if the order was removed, false otherwise
     */
    public boolean removeOrderById(long orderId) {
        Order orderToRemove = allOrders.get(orderId);

        if (orderToRemove != null) {
            double price = orderToRemove.getPrice();
            Map<Double, LinkedHashSet<Order>> book = getOrdersByPriceMap(orderToRemove.getSide());
            LinkedHashSet<Order> ordersAtPrice = book.get(price);

            if (ordersAtPrice != null) {
                ordersAtPrice.remove(orderToRemove);
                if (ordersAtPrice.isEmpty()) {
                    book.remove(price);
                    TreeSet<Double> priceLevels = getPriceLevelsSet(orderToRemove.getSide());
                    priceLevels.remove(price);
                }

                allOrders.remove(orderId);
                return true;
            }
        }

        return false;
    }

    /**
     * Change the order size for an order, using the order id
     *
     * @param orderId the order id
     * @param newOrderSize the new size of the order
     */
    public void changeOrderSize(long orderId, long newOrderSize) {
        Order orderToChange = allOrders.get(orderId);
        if (orderToChange != null) {
            orderToChange.setSize(newOrderSize);
        } else {
            throw new IllegalArgumentException("Invalid order id: " + orderId);
        }
    }

    /**
     * Returns the price level for a given side (the highest bid and lowest offer are considered level 1)
     *
     * @param orderBookSide the side of the order book: B (BID) or O (OFFER)
     * @param orderBookLevel the level of the order book: it starts from level 1
     *
     * @return the price for the level and side
     */
    public double getPriceAtLevel(char orderBookSide, int orderBookLevel) {
        TreeSet<Double> priceLevels = getPriceLevelsSet(orderBookSide);

        if (orderBookLevel < 1 || orderBookLevel > priceLevels.size()) {
            throw new IllegalArgumentException("Invalid level: " + orderBookLevel);
        }

        return priceLevels.stream()
                .skip(orderBookLevel - 1)
                .findFirst()
                .orElseThrow();
    }


    /**
     * Returns the total size of the orders for a given side and level
     *
     * @param orderBookSide the side of the order book: B (BID) or O (OFFER)
     * @param orderBookLevel the level of the order book: it starts from level 1
     *
     * @return the total size of the orders for a given side and level
     */
    public long getTotalSizeOfOrdersAtLevel(char orderBookSide, int orderBookLevel) {
        double priceAtLevel = getPriceAtLevel(orderBookSide, orderBookLevel);
        Map<Double, Long> totalSizeMap = getTotalSizeMap(orderBookSide);

        Long totalSize = totalSizeMap.get(priceAtLevel);

        return totalSize != null ? totalSize : 0L;
    }

    /**
     * Returns all the orders from a side of a book, sorted on a price time priority basis
     *
     * @param orderBookSide the side of the order book: B (BID) or O (OFFER)
     *
     * @return a list of all the orders from a side of a book, sorted on a price time priority basis
     */
    public List<Order> getAllOrdersBySide(char orderBookSide) {
        Map<Double, LinkedHashSet<Order>> ordersByPriceMap = getOrdersByPriceMap(orderBookSide);
        TreeSet<Double> priceLevels = getPriceLevelsSet(orderBookSide);

        List<Order> orders = new ArrayList<>();

        for (Double price : priceLevels) {
            orders.addAll(ordersByPriceMap.get(price));
        }

        return orders;
    }

    /**
     * Returns the bids or the offers map, depending on the order book side
     *
     * @param orderBookSide the order book side (B or O)
     *
     * @return the bids or the offers map
     */
    private Map<Double, LinkedHashSet<Order>> getOrdersByPriceMap(char orderBookSide) {
        Map<Double, LinkedHashSet<Order>> ordersByPriceMap;

        if (orderBookSide == BID) {
            ordersByPriceMap = bids;
        } else if(orderBookSide == OFFER) {
            ordersByPriceMap = offers;
        } else {
            throw new IllegalArgumentException("Invalid order side");
        }

        return ordersByPriceMap;
    }

    /**
     * Returns the price levels for bids or offers, depending on the order book side
     *
     * @param orderBookSide the order book side (B or O)
     *
     * @return the price levels for bids or offers
     */
    private TreeSet<Double> getPriceLevelsSet(char orderBookSide) {
        TreeSet<Double> priceLevels;

        if (orderBookSide == BID) {
            priceLevels = priceLevelsBids;
        } else if(orderBookSide == OFFER) {
            priceLevels = priceLevelsOffers;
        } else {
            throw new IllegalArgumentException("Invalid order side");
        }

        return priceLevels;
    }

    /**
     * Returns the total size map (bids or offers), depending on the order book side
     *
     * @param orderBookSide the order book side (B or O)
     *
     * @return the total size map (bids or offers)
     */
    private Map<Double, Long> getTotalSizeMap(char orderBookSide) {
        Map<Double, Long> totalSizeMap;

        if (orderBookSide == BID) {
            totalSizeMap = totalSizeOfBids;
        } else if (orderBookSide == OFFER) {
            totalSizeMap = totalSizeOfOffers;
        } else {
            throw new IllegalArgumentException("Invalid order side");
        }

        return totalSizeMap;
    }

    /**
     * Adds an order to the set of orders (identified by price).
     * If the price does not exist yet, it creates an entry in the order book, price levels set, and total size map.
     * If the price exists, it updates the existing orders set and increments the total size of orders for that price.
     *
     * @param newOrder the new order to be added
     * @param ordersByPriceMap the map containing sets of orders, keyed by price, on the order book side (either bids or offers)
     * @param priceLevels the sorted set of price levels on the order book side (either bids or offers)
     * @param totalSizeMap the map containing the total size of orders at each price level on the order book side (either bids or offers)
     */
    private void addOrder(Order newOrder, Map<Double, LinkedHashSet<Order>> ordersByPriceMap, TreeSet<Double> priceLevels, Map<Double, Long> totalSizeMap) {
        double price = newOrder.getPrice();
        long size = newOrder.getSize();

        if (!ordersByPriceMap.containsKey(price)) {
            ordersByPriceMap.put(price, new LinkedHashSet<>());
            priceLevels.add(price);
            totalSizeMap.put(price, size);
        } else {
            totalSizeMap.put(price, totalSizeMap.get(price) + size);
        }

        ordersByPriceMap.get(price).add(newOrder);
    }
}
