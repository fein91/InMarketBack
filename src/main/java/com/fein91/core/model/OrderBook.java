package com.fein91.core.model;

import com.fein91.model.Invoice;
import com.fein91.model.OrderType;
import com.fein91.service.CalculationService;
import com.fein91.service.InvoiceService;
import com.fein91.service.OrderRequestService;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;

// TODO for precision, change prices from double to java.math.BigDecimal

@Component
public class OrderBook {

    private final static Logger LOGGER = Logger.getLogger(OrderBook.class.getName());

    private OrderRequestService orderRequestService;
    private InvoiceService invoiceService;
    private CalculationService calculationService;

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
            LOGGER.info("Head order is processing: " + headOrder.getId());

            List<Invoice> invoices = side == OrderSide.ASK
                    ? invoiceService.findBySourceAndTarget(headOrder.getTakerId(), takerId)
                    : invoiceService.findBySourceAndTarget(takerId, headOrder.getTakerId());
            if (CollectionUtils.isEmpty(invoices)) {
                continue;
            }

            //TODO how to define first invoice to calculate
            for (Invoice currentInvoice : invoices) {
                if (currentInvoice.isProcessed()
                        || (OrderSide.ASK == side && this.bids.length() <= 0)
                        || (OrderSide.BID == side && this.asks.length() <= 0)
                        || qtyRemaining.signum() <= 0) {
                    continue;
                }
                LOGGER.info("Invoice is processing: " + currentInvoice);

                BigDecimal unpaidInvoiceValue = currentInvoice.getValue().subtract(currentInvoice.getPrepaidValue());
                int daysToPayment = calculationService.getDaysToPayment(currentInvoice.getPaymentDate());
                BigDecimal discountPercent = calculationService.calculateDiscountPercent(BigDecimal.valueOf(headOrder.getPrice()), daysToPayment);
                BigDecimal maxPrepaidInvoiceValue = calculationService.calculateMaxPossibleInvoicePrepaidValue(unpaidInvoiceValue, discountPercent);
                LOGGER.info("discountPercent " + discountPercent);
                LOGGER.info("maxPrepaidInvoiceValue " + maxPrepaidInvoiceValue);

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
                            updateBidOrder(headOrder, newQty);
                        } else {
                            updateAskOrder(headOrder, newQty);
                        }
                        qtyRemaining = qtyRemaining.subtract(qtyTraded);
                    } else {
                        //обновляем значением ASK - localASK
                        qtyTraded = localOrderQty;
                        discountValue = qtyTraded.multiply(discountPercent);
                        invoiceService.updateInvoice(currentInvoice, qtyTraded.add(discountValue));

                        BigDecimal newQty = headOrder.getQuantity().subtract(qtyTraded);
                        if (side == OrderSide.ASK) {
                            updateBidOrder(headOrder, newQty);
                        } else {
                            updateAskOrder(headOrder, newQty);
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
                            removeBidOrder(headOrder);
                        } else {
                            removeAskOrder(headOrder);
                        }
                        qtyRemaining = qtyRemaining.subtract(qtyTraded);
                    } else {
                        //обновляем значением ASK - qtyRem
                        qtyTraded = qtyRemaining;
                        discountValue = qtyTraded.multiply(discountPercent);
                        invoiceService.updateInvoice(currentInvoice, qtyTraded.add(discountValue));

                        BigDecimal newQty = headOrder.getQuantity().subtract(qtyTraded);
                        if (side == OrderSide.ASK) {
                            updateBidOrder(headOrder, newQty);
                        } else {
                            updateAskOrder(headOrder, newQty);
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
                Trade trade = new Trade(headOrder.getPrice(), qtyTraded, discountValue, BigDecimal.valueOf(100).multiply(discountPercent),
                        unpaidInvoiceValue, currentInvoice.getValue(), daysToPaymentMultQtyTraded,
                        headOrder.getTakerId(), takerId, buyer, seller,
                        headOrder.getId(), currentInvoice.getId());
                trades.add(trade);
                this.tape.add(trade);
                if (verbose) {
                    LOGGER.info(trade.toString());
                }
            }
            //ALL invoices and orders are distributed and processed

        }
        return qtyRemaining;
    }

    private void removeBidOrder(Order order) {
        this.bids.removeOrderByID(order.getqId());
        orderRequestService.removeById(order.getId());
    }

    private void removeAskOrder(Order order) {
        this.asks.removeOrderByID(order.getqId());
        orderRequestService.removeById(order.getId());
    }

    private void updateAskOrder(Order order, BigDecimal newQty) {
        if (newQty.signum() < 0) {
            throw new IllegalArgumentException("Can't update order: " + order + " with new quantity: " + newQty);
        }
        this.asks.updateOrderQty(newQty, order.getqId());
        orderRequestService.update(order.getId(), newQty);
    }

    private void updateBidOrder(Order order, BigDecimal newQty) {
        if (newQty.signum() < 0) {
            throw new IllegalArgumentException("Can't update order: " + order + " with new quantity: " + newQty);
        }

        this.bids.updateOrderQty(newQty, order.getqId());
        orderRequestService.update(order.getId(), newQty);
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
            LOGGER.info("cancelOrder() given neither 'bid' nor 'offer'");
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
            LOGGER.info("modifyOrder() given neither 'bid' nor 'offer'");
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

    public void setCalculationService(CalculationService calculationService) {
        this.calculationService = calculationService;
    }
}
