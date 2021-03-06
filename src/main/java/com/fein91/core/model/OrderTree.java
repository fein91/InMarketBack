package com.fein91.core.model;

import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.*;

public class OrderTree {
	TreeMap<Double, OrderList> priceTree = new TreeMap<Double, OrderList>();
	HashMap<Double, OrderList> priceMap = new HashMap<Double, OrderList>();;
	HashMap<Integer, Order> orderMap = new HashMap<Integer, Order>();
	int volume;
	int nOrders;
	int depth;
	
	public OrderTree() {
		reset();
	}
	
	public void reset() {
		priceTree.clear();
		priceMap.clear();
		orderMap.clear();
		volume = 0;
		nOrders = 0;
		depth = 0;
	}
	
	public Integer length() {
		return orderMap.size();
	}
	
	public OrderList getPriceList(double price) {
		/*
		 * Returns the OrderList object associated with 'price'
		 */
		return priceMap.get(price);
	}
	
	public Order getOrder(int id) {
		/*
		 * Returns the order given the order id
		 */
		return orderMap.get(id);
	}
	
	public void createPrice(double price) {
		depth += 1;
		OrderList newList = new OrderList();
		priceTree.put(price, newList);
		priceMap.put(price, newList);
	}
	
	public void removePrice(double price) {
		depth -= 1;
		priceTree.remove(price);
		priceMap.remove(price);
	}
	
	public boolean priceExists(double price) {
		return priceMap.containsKey(price);
	}
	
	public boolean orderExists(int id) {
		return orderMap.containsKey(id);
	}
	
	public void insertOrder(Order quote) {
		int quoteID = quote.getqId();
		double quotePrice = quote.getPrice();
		if (orderExists(quoteID)) {
			removeOrderByID(quoteID);
		}
		nOrders += 1;
		if (!priceExists(quotePrice)) {
			createPrice(quotePrice);
		}
		quote.setoL(priceMap.get(quotePrice));
		priceMap.get(quotePrice).appendOrder(quote);
		orderMap.put(quoteID, quote);
		volume += quote.getQuantity().intValue();
	}
	
	public void updateOrderQty(BigDecimal qty, int qId) {
		Order order = this.orderMap.get(qId);
		Assert.notNull(order, "Order: "+ qId +" was not found in order book");
		BigDecimal originalVol = order.getQuantity();
		order.updateQty(qty, order.getTimestamp());
		this.volume += (order.getQuantity().subtract(originalVol)).intValue();
	}
	
	public void updateOrder(Order orderUpdate) {
		int idNum = orderUpdate.getqId();
		double price = orderUpdate.getPrice();
		Order order = this.orderMap.get(idNum);
		BigDecimal originalVol = order.getQuantity();
		if (price != order.getPrice()) {
			// Price has been updated
			OrderList tempOL = this.priceMap.get(order.getPrice());
			tempOL.removeOrder(order);
			if (tempOL.getLength()==0) {
				removePrice(order.getPrice());
			}
			insertOrder(orderUpdate);
		} else {
			// The quantity has changed
			order.updateQty(orderUpdate.getQuantity(),
                    orderUpdate.getTimestamp());
		}
		this.volume += (order.getQuantity().subtract(originalVol).intValue());
	}
	
	public void removeOrderByID(int id) {
		this.nOrders -=1;
		Order order = orderMap.get(id);
		this.volume -= order.getQuantity().intValue();
		order.getoL().removeOrder(order);
		if (order.getoL().getLength() == 0) {
			this.removePrice(order.getPrice());
		}
		this.orderMap.remove(id);
	}
	
	public Double maxPrice() {
		if (this.depth>0) {
			return this.priceTree.lastKey();
		} else {
			return null;
		}
	}
	
	public Double minPrice() {
		if (this.depth>0) {
			return this.priceTree.firstKey();
		} else {
			return null;
		}
	}
	
	public OrderList maxPriceList() {
		if (this.depth>0) {
			return this.getPriceList(maxPrice());
		} else {
			return null;
		}
	}
	
	public OrderList minPriceList() {
		if (this.depth>0) {
			return this.getPriceList(minPrice());
		} else {
			return null;
		}
	}
	
	public String toString() {
		String outString = "| The Book:\n" + 
							"| Max price = " + maxPrice() +
							"\n| Min price = " + minPrice() +
							"\n| Volume in book = " + getVolume() +
							"\n| Depth of book = " + getDepth() +
							"\n| Orders in book = " + getnOrders() +
							"\n| Length of tree = " + length() + "\n";
		for (Map.Entry<Double, OrderList> entry : this.priceTree.entrySet()) {
			outString += entry.getValue().toString();
			outString += ("|\n");
		}
		return outString;
	}

	public Integer getVolume() {
		return volume;
	}

	public Integer getnOrders() {
		return nOrders;
	}

	public Integer getDepth() {
		return depth;
	}

    public Iterator<OrderList> getOLsSortedByPriceIterator() {
        return new ArrayList<>(priceTree.values()).iterator();
    }

    public Iterator<OrderList> getOLsInverseSortedByPriceIterator() {
        return new ArrayList<>(priceTree.descendingMap().values()).iterator();
    }

	public Iterator<Map.Entry<Double, OrderList>> getPriceTreeIterator() {
		return new ArrayList<>(priceTree.entrySet()).iterator();
	}

	public Iterator<Map.Entry<Double, OrderList>> getPriceTreeInverseIterator() {
		return new ArrayList<>(priceTree.descendingMap().entrySet()).iterator();
	}
}

