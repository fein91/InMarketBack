package com.fein91.core.model;


import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public class Order {
	private final BigInteger id;
	private long timestamp;
	private boolean limit;
	private int quantity;
	private String side;
	private double price;
	private int qId;
	private int takerId;
	private Order nextOrder;
	private Order prevOrder;
	private OrderList oL;
	@Deprecated
    private Map<Integer, List<Integer>> invoicesQtyByGiverId;
	
	public Order(BigInteger id, long time, boolean limit, int quantity, int takerId, String side) {
		this(id, time, limit, quantity, takerId, side, null);
	}	
	
	public Order(BigInteger id, long time, boolean limit, int quantity,
				int takerId, String side, Double price) {

		this.id = id;
		this.timestamp = time;
		this.limit = limit;
		this.side = side;
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
        		"\ttakerId=" + Integer.toString(takerId);
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

	public int getTakerId() {
		return takerId;
	}

	public void setTakerId(int takerId) {
		this.takerId = takerId;
	}

	public OrderList getoL() {
		return oL;
	}

	public boolean isLimit() {
		return limit;
	}

	public String getSide() {
		return side;
	}

	public void setoL(OrderList oL) {
		this.oL = oL;
	}

	@Deprecated
    public Map<Integer, List<Integer>> getInvoicesQtyByGiverId() {
        return invoicesQtyByGiverId;
    }

	@Deprecated
    public void setInvoicesQtyByGiverId(Map<Integer, List<Integer>> invoicesQtyByGiverId) {
        this.invoicesQtyByGiverId = invoicesQtyByGiverId;
    }

	public BigInteger getId() {
		return id;
	}
}
