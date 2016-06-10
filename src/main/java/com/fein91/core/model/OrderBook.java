package com.fein91.core.model;

import com.fein91.model.Invoice;
import com.fein91.model.OrderType;
import com.fein91.service.InvoiceService;
import com.fein91.service.OrderRequestService;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

// TODO for precision, change prices from double to java.math.BigDecimal

@Component
public class OrderBook {

	private OrderRequestService orderRequestService;
	private InvoiceService invoiceService;

	private List<Trade> tape = new ArrayList<Trade>();
	private OrderTree bids = new OrderTree();
	private OrderTree asks = new OrderTree();
	private double tickSize;
	private long time;
	private int nextQuoteID;
	private int lastOrderSign;

	public OrderBook() {
	}

	public OrderBook(double tickSize) {
		this.tickSize = tickSize;
		this.reset();
	}
	
	public void reset() {
		tape.clear();
		bids.reset();
		asks.reset();
		time = 0;
		nextQuoteID = 0;
		lastOrderSign=1;
	}
	
	
	/**
	 * Clips price according to tickSize
	 * 
	 * @param price
	 * @return
	 */
	private double clipPrice(double price) {
		int numDecPlaces = (int)Math.log10(1 / this.tickSize);
		BigDecimal bd = new BigDecimal(price);
		BigDecimal rounded = bd.setScale(numDecPlaces, BigDecimal.ROUND_HALF_UP);
		return rounded.doubleValue();
	}
	
	
	public OrderReport processOrder(Order quote, boolean verbose) {
		OrderType orderType = quote.getOrderType();
		OrderReport oReport;
		// Update time
		this.time = quote.getTimestamp();
		if (quote.getQuantity() <= 0 ) {
			throw new IllegalArgumentException("processOrder() given qty <= 0");
		}
		if (OrderType.LIMIT == orderType) {
			double clippedPrice = clipPrice(quote.getPrice());
			quote.setPrice(clippedPrice);
			oReport = processLimitOrder(quote, verbose);
		} else {
			oReport = processMarketOrder(quote, verbose);
		}
		return oReport;
	}
	
	
	private OrderReport processMarketOrder(Order quote, boolean verbose) {
		ArrayList<Trade> trades = new ArrayList<Trade>();
		OrderSide side = quote.getOrderSide();
		int qtyRemaining = quote.getQuantity();
		if (side == OrderSide.BID) {
			this.lastOrderSign = 1;
			Iterator<OrderList> orderListIterator = this.asks.getOLsSortedByPriceIterator();
			while ((qtyRemaining > 0) && (orderListIterator.hasNext())) {
				OrderList ordersAtBest = orderListIterator.next();
				qtyRemaining = processOrderList(trades, ordersAtBest, qtyRemaining,
												quote, verbose);
			}
		} else if (side == OrderSide.ASK) {
			this.lastOrderSign = -1;
            Iterator<OrderList> orderListIterator = this.bids.getOLsInverseSortedByPriceIterator();
			while ((qtyRemaining > 0) && (orderListIterator.hasNext())) {
				OrderList ordersAtBest = orderListIterator.next();
				qtyRemaining = processOrderList(trades, ordersAtBest, qtyRemaining,
												quote, verbose);
			}
		} else {
			throw new IllegalArgumentException("order neither market nor limit: " + 
				    						    side);
		}
		OrderReport report = new OrderReport(trades, false, qtyRemaining);
		return  report;
	}
	
	
	private OrderReport processLimitOrder(Order quote, 
										  boolean verbose) {
		boolean orderInBook = false;
		ArrayList<Trade> trades = new ArrayList<Trade>();
		OrderSide side = quote.getOrderSide();
		int qtyRemaining = quote.getQuantity();
		double price = quote.getPrice();
		if (side == OrderSide.BID) {
			this.lastOrderSign = 1;
			Iterator<Map.Entry<Double, OrderList>> priceTreeIter = this.asks.getPriceTreeIterator();
			while ((priceTreeIter.hasNext()) &&
					(qtyRemaining > 0)) {
				Map.Entry<Double, OrderList> priceTreeEntry = priceTreeIter.next();
				double minPrice = priceTreeEntry.getKey();
				if (price >= minPrice) {
					OrderList ordersAtBest = priceTreeEntry.getValue();
					qtyRemaining = processOrderList(trades, ordersAtBest, qtyRemaining,
							quote, verbose);
				}

			}
			// If volume remains, add order to book
			if (qtyRemaining > 0) {
				quote.setqId(this.nextQuoteID);
				quote.setQuantity(qtyRemaining);
				this.bids.insertOrder(quote);
				orderInBook = true;
				this.nextQuoteID+=1;
			} else {
				orderInBook = false;
			}
		} else if (side == OrderSide.ASK) {
			this.lastOrderSign = -1;
			Iterator<Map.Entry<Double, OrderList>> inversePriceTreeIter = this.bids.getPriceTreeInverseIterator();
			while ((inversePriceTreeIter.hasNext()) &&
					(qtyRemaining > 0)) {
				Map.Entry<Double, OrderList> priceTreeEntry = inversePriceTreeIter.next();
				double maxPrice = priceTreeEntry.getKey();
				if (price <= maxPrice) {
					OrderList ordersAtBest = priceTreeEntry.getValue();
					qtyRemaining = processOrderList(trades, ordersAtBest, qtyRemaining,
							quote, verbose);
				}
			}
			// If volume remains, add to book
			if (qtyRemaining > 0) {
				quote.setqId(this.nextQuoteID);
				quote.setQuantity(qtyRemaining);
				this.asks.insertOrder(quote);
				orderInBook = true;
				this.nextQuoteID+=1;
			} else {
				orderInBook = false;
			}
		} else {
			throw new IllegalArgumentException("order neither market nor limit: " + 
				    						    side);
		}
		OrderReport report = new OrderReport(trades, orderInBook, qtyRemaining);
		if (orderInBook) {
			report.setOrder(quote);
		}
		return report;
	}
	
	
	private int processOrderList(ArrayList<Trade> trades, OrderList orders,
								int qtyRemaining, Order quote,
								boolean verbose) {
		OrderSide side = quote.getOrderSide();
		long buyer, seller;
		long takerId = quote.getTakerId();
		long time = quote.getTimestamp();
        Iterator<Order> iter = orders.iterator();
		while ((orders.getLength()>0) && (qtyRemaining>0) && iter.hasNext()) {
			int qtyTraded = 0;
			Order headOrder = iter.next();

			List<Invoice> invoices = side == OrderSide.ASK
					? invoiceService.findBySourceAndTarget(headOrder.getTakerId(), takerId)
					: invoiceService.findBySourceAndTarget(takerId, headOrder.getTakerId());
			if (CollectionUtils.isEmpty(invoices)) {
				continue;
			}

			//TODO how to define first invoice to calculate
			for (Invoice currentInvoice : invoices) {
				if (currentInvoice.isProcessed()) {
					continue;
				}

				BigDecimal unpaidInvoiceValue = currentInvoice.getValue().subtract(currentInvoice.getPrepaidValue());
				int localOrderQty = Math.min(headOrder.getQuantity(), unpaidInvoiceValue.intValue());
				BigDecimal discountPercent = calculateDiscount(headOrder.getPrice(), currentInvoice.getPaymentDate());
				BigDecimal maxPrepaidValue = unpaidInvoiceValue.divide(BigDecimal.ONE.add(discountPercent), BigDecimal.ROUND_HALF_UP);
				BigDecimal realDiscountValue = unpaidInvoiceValue.subtract(maxPrepaidValue);
				System.out.println("discountPercent " + discountPercent);
				System.out.println("maxPrepaidValue " + maxPrepaidValue);
				System.out.println("realDiscountValue " + realDiscountValue);

				if (localOrderQty < headOrder.getQuantity()) {
					if (qtyRemaining <= localOrderQty) {
						//обновляем значением ASK - qtyRem
						qtyTraded = qtyRemaining;
						invoiceService.updateInvoice(currentInvoice, BigDecimal.valueOf(qtyTraded));

						int newQty = headOrder.getQuantity() - qtyRemaining;
						if (side == OrderSide.ASK) {
							this.bids.updateOrderQty(newQty,
									headOrder.getqId());
							orderRequestService.updateOrderRequest(headOrder.getId(), BigDecimal.valueOf(newQty));
						} else {
							this.asks.updateOrderQty(newQty,
									headOrder.getqId());
							orderRequestService.updateOrderRequest(headOrder.getId(), BigDecimal.valueOf(newQty));
						}
						qtyRemaining -= qtyTraded;
					} else {
						//обновляем значением ASK - localASK
						qtyTraded = localOrderQty;
						invoiceService.updateInvoice(currentInvoice, BigDecimal.valueOf(qtyTraded));

						int newQty = headOrder.getQuantity() - localOrderQty;
						if (side == OrderSide.ASK) {
							this.bids.updateOrderQty(newQty,
									headOrder.getqId());
							orderRequestService.updateOrderRequest(headOrder.getId(), BigDecimal.valueOf(newQty));
						} else {
							this.asks.updateOrderQty(newQty,
									headOrder.getqId());
							orderRequestService.updateOrderRequest(headOrder.getId(), BigDecimal.valueOf(newQty));
						}
						qtyRemaining -= qtyTraded;
					}
				} else if (localOrderQty == headOrder.getQuantity()) {
					if (localOrderQty <= qtyRemaining) {
						//поглощаем
						qtyTraded = localOrderQty;
						invoiceService.updateInvoice(currentInvoice, BigDecimal.valueOf(qtyTraded));

						if (side == OrderSide.ASK) {
							this.bids.removeOrderByID(headOrder.getqId());
							orderRequestService.removeOrderRequest(headOrder.getId());
						} else {
							this.asks.removeOrderByID(headOrder.getqId());
							orderRequestService.removeOrderRequest(headOrder.getId());
						}
						qtyRemaining -= qtyTraded;
					} else {
						//обновляем значением ASK - qtyRem
						qtyTraded = qtyRemaining;
						invoiceService.updateInvoice(currentInvoice, BigDecimal.valueOf(qtyTraded));

						int newQty = headOrder.getQuantity() - qtyRemaining;
						if (side == OrderSide.ASK) {
							this.bids.updateOrderQty(newQty,
									headOrder.getqId());
							orderRequestService.updateOrderRequest(headOrder.getId(), BigDecimal.valueOf(newQty));
						} else {
							this.asks.updateOrderQty(newQty,
									headOrder.getqId());
							orderRequestService.updateOrderRequest(headOrder.getId(), BigDecimal.valueOf(newQty));
						}
						qtyRemaining -= qtyTraded;
					}
				} else {
					throw new IllegalStateException("Shouldn't be here");
				}

				currentInvoice.setProcessed(true);
			}
			//ALL invoices and orders are distributed and processed

			if (side == OrderSide.ASK) {
				buyer = headOrder.getTakerId();
				seller = takerId;
			} else {
				buyer = takerId;
				seller = headOrder.getTakerId();
			}
			Trade trade = new Trade(time, headOrder.getPrice(), qtyTraded, 
									headOrder.getTakerId(), takerId, buyer, seller,
									headOrder.getqId());
			trades.add(trade);
			this.tape.add(trade);
			if (verbose) {
				System.out.println(trade);
			}
		}
		return qtyRemaining;
	}

	//=((1+APR/100)^(daysUntilPaymentDate/365)-1)
	private BigDecimal calculateDiscount(double apr, Date paymentDate) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(paymentDate);

		int paymentDateDayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
		int currentDateDayOfYear = LocalDate.now().getDayOfYear();
		double v = apr / 100;
		double b = (paymentDateDayOfYear - currentDateDayOfYear) / 365d;
		return new BigDecimal(Math.pow(1 + v, b) - 1).setScale(2, BigDecimal.ROUND_HALF_UP);
	}


	public void cancelOrder(OrderSide orderSide, int qId, int time) {
		this.time = time;
		if (orderSide == OrderSide.BID) {
			if (bids.orderExists(qId)) {
				bids.removeOrderByID(qId);
			}
		} else if (orderSide == OrderSide.ASK) {
			if (asks.orderExists(qId)) {
				asks.removeOrderByID(qId);
			}
		} else {
			System.out.println("cancelOrder() given neither 'bid' nor 'offer'");
			System.exit(0);
		}
	}
	
	
	public void modifyOrder(int qId, HashMap<String, String> quote) {
		// TODO implement modify order
		// Remember if price is changed must check for clearing.
	}
	
	
	public int getVolumeAtPrice(String side, double price) {
		price = clipPrice(price);
		int vol = 0;
		if(side=="bid") {
			if (bids.priceExists(price)) {
				vol = bids.getPriceList(price).getVolume();
			}
		} else if (side=="offer") {
			if (asks.priceExists(price)) {
				vol = asks.getPriceList(price).getVolume();
			}
		} else {
			System.out.println("modifyOrder() given neither 'bid' nor 'offer'");
			System.exit(0);
		}
		return vol;
		
	}
	
	public double getBestBid() {
		return bids.maxPrice();
	}
	
	public double getWorstBid() {
		return bids.minPrice();
	}
	
	public double getBestOffer() {
		return asks.minPrice();
	}
	
	public double getWorstOffer() {
		return asks.maxPrice();
	}
	
	public int getLastOrderSign() {
		return lastOrderSign;
	}
	
	public int volumeOnSide(String side) {
		if (side=="bid") {
			return this.bids.getVolume();
		} else if (side=="offer") {
			return this.asks.getVolume();
		} else {
			throw new IllegalArgumentException("order neither market nor limit: " + 
				    							side);
		}
	}
	
	public double getTickSize() {
		return tickSize;
	}
	
	public double getSpread() {
		return this.asks.minPrice()-this.bids.maxPrice();
	}
	
	public double getMid() {
		return this.getBestBid()+(this.getSpread()/2.0);
	}
	
	public boolean bidsAndAsksExist() {
		return ((this.bids.nOrders>0)&&(this.asks.nOrders>0));
	}
	
	public String toString() {
		StringWriter fileStr = new StringWriter();
		fileStr.write("Time: " + this.time + "\n");
		fileStr.write(" -------- The Order Book --------\n");
		fileStr.write("|                                |\n");
		fileStr.write("|   ------- Bid  Book --------   |\n");
		if (bids.getnOrders() > 0) {
			fileStr.write(bids.toString());
		}
		fileStr.write("|   ------ Offer  Book -------   |\n");
		if (asks.getnOrders() > 0) {
			fileStr.write(asks.toString());
		}
		fileStr.write("|   -------- Trades  ---------   |");
		if (!tape.isEmpty()) {
			for (Trade t : tape) {
				fileStr.write(t.toString());
			}
		}
		fileStr.write("\n --------------------------------\n");
		return fileStr.toString();
	}


	public List<Trade> getTape() {
		return tape;
	}

	public void setOrderRequestService(OrderRequestService orderRequestService) {
		this.orderRequestService = orderRequestService;
	}

	public void setInvoiceService(InvoiceService invoiceService) {
		this.invoiceService = invoiceService;
	}
}
