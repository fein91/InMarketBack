package com.fein91.core.model;

import java.util.ArrayList;

public class OrderReport {
	/*
	 * Return after an order is submitted to the lob. Contains:
	 * 	- trades:
	 * 
	 * 	- orderInBook
	 */
	private ArrayList<Trade> trades = new ArrayList<Trade>();
	private boolean orderInBook = false;
	private Order order;
	private int qtyRemaining;
	
	public OrderReport(ArrayList<Trade> trades, 
					   boolean orderInBook, int qtyRemaining) {
		this.trades = trades;
		this.orderInBook = orderInBook;
		this.qtyRemaining = qtyRemaining;
	}

	public Order getOrder() {
		return order;
	}
	
	public void setOrder(Order order) {
		this.order = order;
	}

	public ArrayList<Trade> getTrades() {
		return trades;
	}

	public boolean isOrderInBook() {
		return orderInBook;
	}

	public int getQtyRemaining() {
		return qtyRemaining;
	}

	public String toString() {
		String retString = "--- Order Report ---:\nTrades:\n";
		for (Trade t : trades) {
			retString += ("\n" + t.toString());
		}
		retString += ("order in book? " + orderInBook + "\n");
		retString += ("remaining quantity: " + qtyRemaining + "\n");
		retString+= ("\nOrders:\n");
		if (order != null) {
			retString += (order.toString());
		}
		return  retString + "\n--------------------------";
	}
	
}
