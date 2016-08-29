package com.fein91.core.model;

import com.fein91.model.CalculableTrade;

import java.math.BigDecimal;

public class Trade implements CalculableTrade {

	private double price;
	private BigDecimal quantity;
	private long provider;
	private long taker;
	private long buyer;
	private long seller;
	private Long orderHit;
	private long invoiceId;
	private BigDecimal discountValue;
	private BigDecimal periodReturn;
	private BigDecimal unpaidInvoiceValue;
	private BigDecimal invoiceValue;
	//TODO maybe find more suitable name
	/**
	 * its needed to calculate avg days left to payment date
	 */
	private BigDecimal daysToPaymentMultQtyTraded;
	
	public Trade(double price, BigDecimal qty, BigDecimal discountValue, BigDecimal periodReturn, BigDecimal unpaidInvoiceValue,
				 BigDecimal invoiceValue, BigDecimal daysToPaymentMultQtyTraded,
				 long provider, long taker, long buyer, long seller, Long orderHit, long invoiceId) {
		this.price = price;
		this.quantity = qty;
		this.discountValue = discountValue;
		this.periodReturn = periodReturn;
		this.unpaidInvoiceValue = unpaidInvoiceValue;
		this.invoiceValue = invoiceValue;
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
				"\tquantity = " + quantity +
				"\tdiscountValue = " + discountValue +
				"\tunpaidInvoiceValue = " + unpaidInvoiceValue +
				"\tProvider = " + provider +
				"\tTaker = " + taker +
				"\tBuyer = " + buyer +
				"\tSeller = " + seller);
	}
	
	public String toCSV() {
		return (price + ", " +
				quantity + ", " +
				provider + ", " + 
				taker + ", " + 
				buyer + ", " + 
				seller + "\n");
	}

	public double getPrice() {
		return price;
	}


	@Override
	public BigDecimal getQuantity() {
		return quantity;
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


	public Long getOrderHit() {
		return orderHit;
	}

	public BigDecimal getDiscountValue() {
		return discountValue;
	}

	public BigDecimal getPeriodReturn() {
		return periodReturn;
	}

	public BigDecimal getUnpaidInvoiceValue() {
		return unpaidInvoiceValue;
	}

	public BigDecimal getInvoiceValue() {
		return invoiceValue;
	}

	@Override
	public BigDecimal getDaysToPaymentMultQtyTraded() {
		return daysToPaymentMultQtyTraded;
	}

	public long getInvoiceId() {
		return invoiceId;
	}
}
