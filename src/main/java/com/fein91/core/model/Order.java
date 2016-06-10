package com.fein91.core.model;


import com.fein91.model.OrderType;

public class Order {
	private final long id;
	private long timestamp;
	private OrderType orderType;
	private int quantity;
	private OrderSide orderSide;
	private double price;
	private int qId;
	private long takerId;
	private Order nextOrder;
	private Order prevOrder;
	private OrderList oL;
	
	public Order(long id, long time, OrderType orderType, int quantity, long takerId, OrderSide orderSide) {
		this(id, time, orderType, quantity, takerId, orderSide, null);
	}	
	
	public Order(long id, long time, OrderType orderType, int quantity,
				long takerId, OrderSide orderSide, Double price) {

		this.id = id;
		this.timestamp = time;
		this.orderType = orderType;
		this.orderSide = orderSide;
		this.quantity = quantity;
		if (price!=null) {
			this.price = (double)price;
		}
		this.takerId = takerId;
	}
	
	public void updateQty(int qty, long tstamp) {
		if ((qty > this.quantity) && (this.oL.getTailOrder() != this)) {
			// Move order to the end of the list. i.e. loses time priority
			this.oL.moveTail(this);
			this.timestamp = tstamp;
		}
		oL.setVolume(oL.getVolume()-(this.quantity-qty));
		this.quantity = qty;
	}
	
	public String toString() {
        return Integer.toString(quantity) + "\t@\t" + Double.toString(price) + 
        		"\tt=" + Long.toString(timestamp) +
        		"\tqId=" + Integer.toString(qId) +
        		"\ttakerId=" + Long.toString(takerId);
    }

	
	// Getters and Setters
	public Order getNextOrder() {
		return nextOrder;
	}

	public void setNextOrder(Order nextOrder) {
		this.nextOrder = nextOrder;
	}

	public Order getPrevOrder() {
		return prevOrder;
	}

	public void setPrevOrder(Order prevOrder) {
		this.prevOrder = prevOrder;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public int getqId() {
		return qId;
	}

	public void setqId(int qId) {
		this.qId = qId;
	}

	public long getTakerId() {
		return takerId;
	}

	public void setTakerId(long takerId) {
		this.takerId = takerId;
	}

	public OrderList getoL() {
		return oL;
	}

	public OrderType getOrderType() {
		return orderType;
	}

	public OrderSide getOrderSide() {
		return orderSide;
	}

	public void setoL(OrderList oL) {
		this.oL = oL;
	}

	public long getId() {
		return id;
	}
}
