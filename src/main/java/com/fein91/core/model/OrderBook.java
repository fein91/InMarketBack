package com.fein91.core.model;

import com.fein91.model.Invoice;
import com.fein91.model.OrderType;
import com.fein91.service.InvoiceService;
import com.fein91.service.OrderRequestService;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.*;

// TODO for precision, change prices from double to java.math.BigDecimal

@Component
public class OrderBook {

    private static Logger log = Logger.getLogger(OrderBook.class);

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
        lastOrderSign = 1;
    }


    /**
     * Clips price according to tickSize
     *
     * @param price
     * @return
     */
    private double clipPrice(double price) {
        int numDecPlaces = (int) Math.log10(1 / this.tickSize);
        BigDecimal bd = new BigDecimal(price);
        BigDecimal rounded = bd.setScale(numDecPlaces, BigDecimal.ROUND_HALF_UP);
        return rounded.doubleValue();
    }


    public OrderReport processOrder(Order quote, boolean verbose) {
        OrderType orderType = quote.getOrderType();
        OrderReport oReport;
        // Update time
        this.time = quote.getTimestamp();
        if (quote.getQuantity().signum() <= 0) {
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
        BigDecimal qtyRemaining = quote.getQuantity();
        if (side == OrderSide.BID) {
            this.lastOrderSign = 1;
            Iterator<OrderList> orderListIterator = this.asks.getOLsSortedByPriceIterator();
            while ((qtyRemaining.signum() > 0) && (orderListIterator.hasNext())) {
                OrderList ordersAtBest = orderListIterator.next();
                qtyRemaining = processOrderList(trades, ordersAtBest, qtyRemaining,
                        quote, verbose);
            }
        } else if (side == OrderSide.ASK) {
            this.lastOrderSign = -1;
            Iterator<OrderList> orderListIterator = this.bids.getOLsInverseSortedByPriceIterator();
            while ((qtyRemaining.signum() > 0) && (orderListIterator.hasNext())) {
                OrderList ordersAtBest = orderListIterator.next();
                qtyRemaining = processOrderList(trades, ordersAtBest, qtyRemaining,
                        quote, verbose);
            }
        } else {
            throw new IllegalArgumentException("order neither market nor limit: " +
                    side);
        }
        OrderReport report = new OrderReport(trades, false, qtyRemaining);
        return report;
    }


    private OrderReport processLimitOrder(Order quote,
                                          boolean verbose) {
        boolean orderInBook = false;
        ArrayList<Trade> trades = new ArrayList<Trade>();
        OrderSide side = quote.getOrderSide();
        BigDecimal qtyRemaining = quote.getQuantity();
        double price = quote.getPrice();
        if (side == OrderSide.BID) {
            this.lastOrderSign = 1;
            Iterator<Map.Entry<Double, OrderList>> priceTreeIter = this.asks.getPriceTreeIterator();
            while ((priceTreeIter.hasNext()) &&
                    (qtyRemaining.signum() > 0)) {
                Map.Entry<Double, OrderList> priceTreeEntry = priceTreeIter.next();
                double minPrice = priceTreeEntry.getKey();
                if (price >= minPrice) {
                    OrderList ordersAtBest = priceTreeEntry.getValue();
                    qtyRemaining = processOrderList(trades, ordersAtBest, qtyRemaining,
                            quote, verbose);
                }

            }
            // If volume remains, add order to book
            if (qtyRemaining.signum() > 0) {
                quote.setqId(this.nextQuoteID);
                quote.setQuantity(qtyRemaining);
                this.bids.insertOrder(quote);
                orderInBook = true;
                this.nextQuoteID += 1;
            } else {
                orderInBook = false;
            }
        } else if (side == OrderSide.ASK) {
            this.lastOrderSign = -1;
            Iterator<Map.Entry<Double, OrderList>> inversePriceTreeIter = this.bids.getPriceTreeInverseIterator();
            while ((inversePriceTreeIter.hasNext()) &&
                    (qtyRemaining.signum() > 0)) {
                Map.Entry<Double, OrderList> priceTreeEntry = inversePriceTreeIter.next();
                double maxPrice = priceTreeEntry.getKey();
                if (price <= maxPrice) {
                    OrderList ordersAtBest = priceTreeEntry.getValue();
                    qtyRemaining = processOrderList(trades, ordersAtBest, qtyRemaining,
                            quote, verbose);
                }
            }
            // If volume remains, add to book
            if (qtyRemaining.signum() > 0) {
                quote.setqId(this.nextQuoteID);
                quote.setQuantity(qtyRemaining);
                this.asks.insertOrder(quote);
                orderInBook = true;
                this.nextQuoteID += 1;
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


    private BigDecimal processOrderList(ArrayList<Trade> trades, OrderList orders,
                                 BigDecimal qtyRemaining, Order quote,
                                 boolean verbose) {
        OrderSide side = quote.getOrderSide();
        long buyer, seller;
        long takerId = quote.getTakerId();
        Iterator<Order> ordersIter = orders.iterator();
        while ((orders.getLength() > 0) && (qtyRemaining.signum() > 0) && ordersIter.hasNext()) {

            Order headOrder = ordersIter.next();
            log.info("Head order is processing: " + headOrder.getId());

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
                log.info("Invoice is processing: " + currentInvoice);

                BigDecimal unpaidInvoiceValue = currentInvoice.getValue().subtract(currentInvoice.getPrepaidValue());
                int daysToPayment = getDaysToPayment(currentInvoice.getPaymentDate());
                BigDecimal discountPercent = calculateDiscount(BigDecimal.valueOf(headOrder.getPrice()), daysToPayment);
                BigDecimal maxPrepaidInvoiceValue = unpaidInvoiceValue.divide(BigDecimal.ONE.add(discountPercent), BigDecimal.ROUND_HALF_UP);
                log.info("discountPercent " + discountPercent);
                log.info("maxPrepaidInvoiceValue " + maxPrepaidInvoiceValue);

                BigDecimal localOrderQty = headOrder.getQuantity().min(maxPrepaidInvoiceValue);
                BigDecimal discountValue;
                BigDecimal qtyTraded;
                BigDecimal daysToPaymentMultQtyTraded;
                if (localOrderQty.compareTo(headOrder.getQuantity()) < 0) {
                    if (qtyRemaining.compareTo(localOrderQty) <= 0) {
                        //обновляем значением ASK - qtyRem
                        qtyTraded = qtyRemaining;
                        discountValue = qtyTraded.multiply(discountPercent);

                        invoiceService.updateInvoice(currentInvoice, qtyTraded.add(discountValue));

                        BigDecimal newQty = headOrder.getQuantity().subtract(qtyTraded);
                        if (side == OrderSide.ASK) {
                            this.bids.updateOrderQty(newQty, headOrder.getqId());
                            orderRequestService.updateOrderRequest(headOrder.getId(), newQty);
                        } else {
                            this.asks.updateOrderQty(newQty, headOrder.getqId());
                            orderRequestService.updateOrderRequest(headOrder.getId(), newQty);
                        }
                        qtyRemaining = qtyRemaining.subtract(qtyTraded);
                    } else {
                        //обновляем значением ASK - localASK
                        qtyTraded = localOrderQty;
                        discountValue = qtyTraded.multiply(discountPercent);
                        invoiceService.updateInvoice(currentInvoice, qtyTraded.add(discountValue));

                        BigDecimal newQty = headOrder.getQuantity().subtract(qtyTraded);
                        if (side == OrderSide.ASK) {
                            this.bids.updateOrderQty(newQty, headOrder.getqId());
                            orderRequestService.updateOrderRequest(headOrder.getId(), newQty);
                        } else {
                            this.asks.updateOrderQty(newQty, headOrder.getqId());
                            orderRequestService.updateOrderRequest(headOrder.getId(), newQty);
                        }
                        qtyRemaining = qtyRemaining.subtract(qtyTraded);
                    }
                } else if (localOrderQty.compareTo(headOrder.getQuantity()) == 0) {
                    if (localOrderQty.compareTo(qtyRemaining) <= 0) {
                        //поглощаем
                        qtyTraded = localOrderQty;
                        discountValue = qtyTraded.multiply(discountPercent);
                        invoiceService.updateInvoice(currentInvoice, qtyTraded.add(discountValue));

                        if (side == OrderSide.ASK) {
                            this.bids.removeOrderByID(headOrder.getqId());
                            //TODO implement
                            //ordersIter.remove();
                            orderRequestService.removeById(headOrder.getId());
                        } else {
                            this.asks.removeOrderByID(headOrder.getqId());
                            //TODO implement 
                            //ordersIter.remove();
                            orderRequestService.removeById(headOrder.getId());
                        }
                        qtyRemaining = qtyRemaining.subtract(qtyTraded);
                    } else {
                        //обновляем значением ASK - qtyRem
                        qtyTraded = qtyRemaining;
                        discountValue = qtyTraded.multiply(discountPercent);
                        invoiceService.updateInvoice(currentInvoice, qtyTraded.add(discountValue));

                        BigDecimal newQty = headOrder.getQuantity().subtract(qtyTraded);
                        if (side == OrderSide.ASK) {
                            this.bids.updateOrderQty(newQty, headOrder.getqId());
                            orderRequestService.updateOrderRequest(headOrder.getId(), newQty);
                        } else {
                            this.asks.updateOrderQty(newQty, headOrder.getqId());
                            orderRequestService.updateOrderRequest(headOrder.getId(), newQty);
                        }
                        qtyRemaining = qtyRemaining.subtract(qtyTraded) ;
                    }
                } else {
                    throw new IllegalStateException("Shouldn't be here");
                }

                daysToPaymentMultQtyTraded = qtyTraded.multiply(BigDecimal.valueOf(daysToPayment));

                if (side == OrderSide.ASK) {
                    buyer = headOrder.getTakerId();
                    seller = takerId;
                } else {
                    buyer = takerId;
                    seller = headOrder.getTakerId();
                }
                Trade trade = new Trade(headOrder.getPrice(), qtyTraded, discountValue, currentInvoice.getValue(), daysToPaymentMultQtyTraded,
                        headOrder.getTakerId(), takerId, buyer, seller,
                        headOrder.getId(), currentInvoice.getId());
                trades.add(trade);
                this.tape.add(trade);
                if (verbose) {
                    System.out.println(trade);
                }
            }
            //ALL invoices and orders are distributed and processed

        }
        return qtyRemaining;
    }

    @Deprecated
    private BigDecimal calculateDiscount(BigDecimal apr, int daysToPayment) {
        //double discount = Math.pow(1 + apr / 100, daysBetween.getDays() / 365d) - 1;
        return apr.multiply(BigDecimal.valueOf(daysToPayment))
                .divide(BigDecimal.valueOf(365), 10, BigDecimal.ROUND_HALF_UP)
                .divide(BigDecimal.valueOf(100), 10, BigDecimal.ROUND_HALF_UP);
    }

    @Deprecated
    private int getDaysToPayment(Date paymentDate) {
        DateTime paymentDT = new DateTime(paymentDate);
        DateTime currDT = new DateTime();
        Days daysBetween = Days.daysBetween(currDT.toLocalDate(), paymentDT.toLocalDate());
        int daysToPayment = daysBetween.getDays();
        log.info("Days to payment date left: " + daysToPayment);
        return daysToPayment;
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
        if (side == "bid") {
            if (bids.priceExists(price)) {
                vol = bids.getPriceList(price).getVolume();
            }
        } else if (side == "offer") {
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
        if (side == "bid") {
            return this.bids.getVolume();
        } else if (side == "offer") {
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
        return this.asks.minPrice() - this.bids.maxPrice();
    }

    public double getMid() {
        return this.getBestBid() + (this.getSpread() / 2.0);
    }

    public boolean bidsAndAsksExist() {
        return ((this.bids.nOrders > 0) && (this.asks.nOrders > 0));
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
        fileStr.write("|   ------ Ask  Book -------   |\n");
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
