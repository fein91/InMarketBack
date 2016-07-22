package com.fein91.core.model;

import java.math.BigDecimal;

public class Trade {

	private double price;
	private BigDecimal qty;
	private long provider;
	private long taker;
	private long buyer;
	private long seller;
	private long orderHit;
	private long invoiceId;
	private BigDecimal discountValue;
	private BigDecimal discountPercent;
	private BigDecimal unpaidInvoiceValue;
	//TODO maybe find more suitable name
	/**
	 * its needed to calculate avg days left to payment date
	 */
	private BigDecimal daysToPaymentMultQtyTraded;
	
	public Trade(double price, BigDecimal qty, BigDecimal discountValue, BigDecimal discountPercent, BigDecimal unpaidInvoiceValue, BigDecimal daysToPaymentMultQtyTraded,
				 long provider, long taker, long buyer, long seller, long orderHit, long invoiceId) {
		this.price = price;
		this.qty = qty;
		this.discountValue = discountValue;
		this.discountPercent = discountPercent;
		this.unpaidInvoiceValue = unpaidInvoiceValue;
		this.daysToPaymentMultQtyTraded = daysToPaymentMultQtyTraded;
		this.provider = provider;
		this.taker = taker;
		this.buyer = buyer;
		this.seller = seller;
		this.orderHit = orderHit; // the qId of the order that was in the book
		this.invoiceId = invoiceId;
	}
	
	
	@Override
	public String toString() {
		return ("\n| TRADE\tt= " +
				"\tprice = " + price +
				"\tquantity = " + qty +
				"\tdiscountValue = " + discountValue +
				"\tunpaidInvoiceValue = " + unpaidInvoiceValue +
				"\tProvider = " + provider +
				"\tTaker = " + taker +
				"\tBuyer = " + buyer +
				"\tSeller = " + seller);
	}
	
	public String toCSV() {
		return (price + ", " +
				qty + ", " + 
				provider + ", " + 
				taker + ", " + 
				buyer + ", " + 
				seller + "\n");
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


	public long getOrderHit() {
		return orderHit;
	}

	public BigDecimal getDiscountValue() {
		return discountValue;
	}

	public BigDecimal getDiscountPercent() {
		return discountPercent;
	}

	public BigDecimal getUnpaidInvoiceValue() {
		return unpaidInvoiceValue;
	}

	public BigDecimal getDaysToPaymentMultQtyTraded() {
		return daysToPaymentMultQtyTraded;
	}

	public long getInvoiceId() {
		return invoiceId;
	}
}
