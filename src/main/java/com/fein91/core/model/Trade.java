package com.fein91.core.model;

import java.math.BigDecimal;

public class Trade {
	private long timestamp;
	private double price;
	private BigDecimal qty;
	private long provider;
	private long taker;
	private long buyer;
	private long seller;
	private int orderHit;
	
	public Trade(long time, double price, BigDecimal qty, long provider,
				 long taker, long buyer, long seller, int orderHit) {
		this.timestamp = time;
		this.price = price;
		this.qty = qty;
		this.provider = provider;
		this.taker = taker;
		this.buyer = buyer;
		this.seller = seller;
		this.orderHit = orderHit; // the qId of the order that was in the book
	}
	
	
	@Override
	public String toString() {
		return ("\n| TRADE\tt= " + timestamp + 
				"\tprice = " + price +
				"\tquantity = " + qty +
				"\tProvider = " + provider +
				"\tTaker = " + taker +
				"\tBuyer = " + buyer +
				"\tSeller = " + seller);
	}
	
	public String toCSV() {
		return (timestamp + ", " + 
				price + ", " + 
				qty + ", " + 
				provider + ", " + 
				taker + ", " + 
				buyer + ", " + 
				seller + "\n");
	}


	public long getTimestamp() {
		return timestamp;
	}



	public double getPrice() {
		return price;
	}



	public BigDecimal getQty() {
		return qty;
	}



	public long getProvider() {
		return provider;
	}



	public long getTaker() {
		return taker;
	}



	public long getBuyer() {
		return buyer;
	}



	public long getSeller() {
		return seller;
	}


	public int getOrderHit() {
		return orderHit;
	}
}
