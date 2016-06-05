package com.fein91.core.service;

import com.fein91.InMarketApplication;
import com.fein91.core.model.OrderBook;
import com.fein91.model.OrderResult;
import com.fein91.core.model.OrderSide;
import com.fein91.core.model.Trade;
import com.fein91.dao.CounterpartyRepository;
import com.fein91.model.Counterparty;
import com.fein91.service.CounterPartyService;
import com.fein91.service.InvoiceService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = InMarketApplication.class)
public class LimitOrderBookServiceTest {

    @Autowired
    LimitOrderBookService limitOrderBookService;
    @Autowired
    CounterPartyService counterPartyService;
    @Autowired
    CounterpartyRepository counterpartyRepository;
    @Autowired
    InvoiceService invoiceService;
    @Autowired
    OrderBookBuilder orderBookBuilder;

    /*
    *     b1  b2  b3
    * s1 100 200  50
    *    ASK
    * b3 100 29
    * b2 150 28
    * b1 200 27
    * s1 BID market order == 350
    * */
    @Test
    @Transactional
    @Rollback(true)
    public void marketOrderTest1() throws Exception {
        OrderBook lob = orderBookBuilder.getInstance();

        Counterparty supplier = counterPartyService.addCounterParty(BigInteger.valueOf(1), "supplier");
        Counterparty buyer1 = counterPartyService.addCounterParty(BigInteger.valueOf(2), "buyer1");
        Counterparty buyer2 = counterPartyService.addCounterParty(BigInteger.valueOf(3), "buyer2");
        Counterparty buyer3 = counterPartyService.addCounterParty(BigInteger.valueOf(4), "buyer3");

        invoiceService.addInvoice(BigInteger.valueOf(11), supplier, buyer1, BigDecimal.valueOf(100));
        invoiceService.addInvoice(BigInteger.valueOf(12), supplier, buyer2, BigDecimal.valueOf(200));
        invoiceService.addInvoice(BigInteger.valueOf(13), supplier, buyer3, BigDecimal.valueOf(50));


        double buyer1AskPrice = 27d;
        limitOrderBookService.addAskLimitOrder(lob, BigInteger.ONE, buyer1, 200, buyer1AskPrice);
        double buyer2AskPrice = 28d;
        limitOrderBookService.addAskLimitOrder(lob, BigInteger.ONE, buyer2, 150, buyer2AskPrice);
        double buyer3AskPrice = 29d;
        limitOrderBookService.addAskLimitOrder(lob, BigInteger.ONE, buyer3, 100, buyer3AskPrice);

        OrderResult result = limitOrderBookService.addBidMarketOrder(lob, BigInteger.ONE, supplier, 350);

        Trade trade1 = findTradeByBuyerAndSeller(lob.getTape(), supplier.getId().intValue(), buyer1.getId().intValue());
        Assert.assertNotNull(trade1);
        Assert.assertEquals(trade1.getPrice(), buyer1AskPrice, 0d);
        Assert.assertEquals(trade1.getQty(), 100);
        Trade trade2 = findTradeByBuyerAndSeller(lob.getTape(), supplier.getId().intValue(), buyer2.getId().intValue());
        Assert.assertNotNull(trade2);
        Assert.assertEquals(trade2.getPrice(), buyer2AskPrice, 0d);
        Assert.assertEquals(trade2.getQty(), 150);
        Trade trade3 = findTradeByBuyerAndSeller(lob.getTape(), supplier.getId().intValue(), buyer3.getId().intValue());
        Assert.assertNotNull(trade3);
        Assert.assertEquals(trade3.getPrice(), buyer3AskPrice, 0d);
        Assert.assertEquals(trade3.getQty(), 50);

        Assert.assertEquals(300, result.getSatisfiedDemand());
        Assert.assertEquals(0, BigDecimal.valueOf(27.8).compareTo(result.getApr()));
    }

    @Test
    @Transactional
    @Rollback(true)
    public void marketOrderTest2() throws Exception {
        OrderBook lob = orderBookBuilder.getInstance();

        Counterparty buyer = counterPartyService.addCounterParty(BigInteger.valueOf(1), "buyer");
        Counterparty supplier1 = counterPartyService.addCounterParty(BigInteger.valueOf(2), "supplier1");
        Counterparty supplier2 = counterPartyService.addCounterParty(BigInteger.valueOf(3), "supplier2");
        Counterparty supplier3 = counterPartyService.addCounterParty(BigInteger.valueOf(4), "supplier3");
        Counterparty supplier4 = counterPartyService.addCounterParty(BigInteger.valueOf(6), "supplier4");

        invoiceService.addInvoice(BigInteger.valueOf(11), supplier1, buyer, BigDecimal.valueOf(100));
        invoiceService.addInvoice(BigInteger.valueOf(12), supplier2, buyer, BigDecimal.valueOf(200));
        invoiceService.addInvoice(BigInteger.valueOf(13), supplier3, buyer, BigDecimal.valueOf(50));
        invoiceService.addInvoice(BigInteger.valueOf(14), supplier4, buyer, BigDecimal.valueOf(50));


        double supplier1AskPrice = 27d;
        limitOrderBookService.addBidLimitOrder(lob, BigInteger.ONE, supplier1, 200, supplier1AskPrice);
        double supplier2AskPrice = 28d;
        limitOrderBookService.addBidLimitOrder(lob, BigInteger.ONE, supplier2, 150, supplier2AskPrice);
        double supplier3AskPrice = 29d;
        limitOrderBookService.addBidLimitOrder(lob, BigInteger.ONE, supplier3, 100, supplier3AskPrice);
        double supplier4AskPrice = 31d;
        limitOrderBookService.addBidLimitOrder(lob, BigInteger.ONE, supplier4, 100, supplier4AskPrice);

        OrderResult result = limitOrderBookService.addAskMarketOrder(lob, BigInteger.ONE, buyer, 350);

        Trade trade1 = findTradeByBuyerAndSeller(lob.getTape(), supplier1.getId().intValue(), buyer.getId().intValue());
        Assert.assertNotNull(trade1);
        Assert.assertEquals(trade1.getPrice(), supplier1AskPrice, 0d);
        Assert.assertEquals(trade1.getQty(), 100);

        Trade trade2 = findTradeByBuyerAndSeller(lob.getTape(), supplier2.getId().intValue(), buyer.getId().intValue());
        Assert.assertNotNull(trade2);
        Assert.assertEquals(trade2.getPrice(), supplier2AskPrice, 0d);
        Assert.assertEquals(trade2.getQty(), 150);

        Trade trade3 = findTradeByBuyerAndSeller(lob.getTape(), supplier3.getId().intValue(), buyer.getId().intValue());
        Assert.assertNotNull(trade3);
        Assert.assertEquals(trade3.getPrice(), supplier3AskPrice, 0d);
        Assert.assertEquals(trade3.getQty(), 50);

        Trade trade4 = findTradeByBuyerAndSeller(lob.getTape(), supplier4.getId().intValue(), buyer.getId().intValue());
        Assert.assertNotNull(trade4);
        Assert.assertEquals(trade4.getPrice(), supplier4AskPrice, 0d);
        Assert.assertEquals(trade4.getQty(), 50);

        Assert.assertEquals(350, result.getSatisfiedDemand());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void marketOrderTest3() throws Exception {
        OrderBook lob = orderBookBuilder.getInstance();

        Counterparty buyer1 = counterPartyService.addCounterParty(BigInteger.valueOf(1), "buyer1");
        Counterparty buyer2 = counterPartyService.addCounterParty(BigInteger.valueOf(2), "buyer2");

        Counterparty supplier1 = counterPartyService.addCounterParty(BigInteger.valueOf(3), "supplier1");
        Counterparty supplier5 = counterPartyService.addCounterParty(BigInteger.valueOf(7), "supplier5");

        invoiceService.addInvoice(BigInteger.valueOf(11), supplier1, buyer1, BigDecimal.valueOf(100));
        invoiceService.addInvoice(BigInteger.valueOf(14), supplier5, buyer2, BigDecimal.valueOf(150));

        double supplier1AskPrice = 27d;
        limitOrderBookService.addBidLimitOrder(lob, BigInteger.ONE, supplier1, 300, supplier1AskPrice);
        double supplier5AskPrice = 32d;
        limitOrderBookService.addBidLimitOrder(lob, BigInteger.ONE, supplier5, 400, supplier5AskPrice);

        OrderResult result = limitOrderBookService.addAskMarketOrder(lob, BigInteger.ONE, buyer1, 350);

        Trade trade1 = findTradeByBuyerAndSeller(lob.getTape(), supplier1.getId().intValue(), buyer1.getId().intValue());
        Assert.assertNotNull(trade1);
        Assert.assertEquals(trade1.getPrice(), supplier1AskPrice, 0d);
        Assert.assertEquals(trade1.getQty(), 100);

        Assert.assertEquals(100, result.getSatisfiedDemand());
    }

    /*
    *     b1  b2  b3
    * s1 100 100 800
    *    ASK
    * b3 500 31
    * b2 100 30
    * b1 600 29
    * s1 market order == 450
    * */
    @Test
    @Transactional
    @Rollback(true)
    public void marketOrderTest4() throws Exception {
        OrderBook lob = orderBookBuilder.getInstance();

        Counterparty supplier = counterPartyService.addCounterParty(BigInteger.valueOf(1), "supplier");
        Counterparty buyer1 = counterPartyService.addCounterParty(BigInteger.valueOf(2), "buyer1");
        Counterparty buyer2 = counterPartyService.addCounterParty(BigInteger.valueOf(3), "buyer2");
        Counterparty buyer3 = counterPartyService.addCounterParty(BigInteger.valueOf(4), "buyer3");

        invoiceService.addInvoice(BigInteger.valueOf(11), supplier, buyer1, BigDecimal.valueOf(100));
        invoiceService.addInvoice(BigInteger.valueOf(12), supplier, buyer2, BigDecimal.valueOf(100));
        invoiceService.addInvoice(BigInteger.valueOf(13), supplier, buyer3, BigDecimal.valueOf(800));


        double buyer1AskPrice = 29d;
        limitOrderBookService.addAskLimitOrder(lob, BigInteger.ONE, buyer1, 600, buyer1AskPrice);
        double buyer2AskPrice = 30d;
        limitOrderBookService.addAskLimitOrder(lob, BigInteger.ONE, buyer2, 100, buyer2AskPrice);
        double buyer3AskPrice = 31d;
        limitOrderBookService.addAskLimitOrder(lob, BigInteger.ONE, buyer3, 500, buyer3AskPrice);

        OrderResult result = limitOrderBookService.addBidMarketOrder(lob, BigInteger.ONE, supplier, 450);

        Trade trade1 = findTradeByBuyerAndSeller(lob.getTape(), supplier.getId().intValue(), buyer1.getId().intValue());
        Assert.assertNotNull(trade1);
        Assert.assertEquals(trade1.getPrice(), buyer1AskPrice, 0d);
        Assert.assertEquals(trade1.getQty(), 100);
        Trade trade2 = findTradeByBuyerAndSeller(lob.getTape(), supplier.getId().intValue(), buyer2.getId().intValue());
        Assert.assertNotNull(trade2);
        Assert.assertEquals(trade2.getPrice(), buyer2AskPrice, 0d);
        Assert.assertEquals(trade2.getQty(), 100);
        Trade trade3 = findTradeByBuyerAndSeller(lob.getTape(), supplier.getId().intValue(), buyer3.getId().intValue());
        Assert.assertNotNull(trade3);
        Assert.assertEquals(trade3.getPrice(), buyer3AskPrice, 0d);
        Assert.assertEquals(trade3.getQty(), 250);

        Assert.assertEquals(450, result.getSatisfiedDemand());
        Assert.assertEquals(0, BigDecimal.valueOf(30.3).compareTo(result.getApr()));
    }


    /*
    *     b1
    * s1 550
    * s2  0
    * s3 200
    *       BID
    * s3 28 550
    * s2 27  0
    * s1 26 200
    * b1 market order == 250
    * */
    @Test
    @Transactional
    @Rollback(true)
    public void marketOrderTest5() throws Exception {
        OrderBook lob = orderBookBuilder.getInstance();

        Counterparty buyer = counterPartyService.addCounterParty(BigInteger.valueOf(1), "buyer");
        Counterparty supplier1 = counterPartyService.addCounterParty(BigInteger.valueOf(2), "supplier1");
        Counterparty supplier2 = counterPartyService.addCounterParty(BigInteger.valueOf(3), "supplier2");
        Counterparty supplier3 = counterPartyService.addCounterParty(BigInteger.valueOf(4), "supplier3");

        invoiceService.addInvoice(BigInteger.valueOf(11), supplier1, buyer, BigDecimal.valueOf(550));
        invoiceService.addInvoice(BigInteger.valueOf(13), supplier3, buyer, BigDecimal.valueOf(200));

        double supplier1AskPrice = 28d;
        limitOrderBookService.addBidLimitOrder(lob, BigInteger.ONE, supplier1, 550, supplier1AskPrice);
        double supplier3AskPrice = 26d;
        limitOrderBookService.addBidLimitOrder(lob, BigInteger.ONE, supplier3, 200, supplier3AskPrice);

        OrderResult result = limitOrderBookService.addAskMarketOrder(lob, BigInteger.ONE, buyer, 250);

        Trade trade1 = findTradeByBuyerAndSeller(lob.getTape(), supplier1.getId().intValue(), buyer.getId().intValue());
        Assert.assertNotNull(trade1);
        Assert.assertEquals(trade1.getPrice(), supplier1AskPrice, 0d);
        Assert.assertEquals(trade1.getQty(), 250);

        Assert.assertEquals(250, result.getSatisfiedDemand());
        Assert.assertEquals(0, BigDecimal.valueOf(28).compareTo(result.getApr()));
    }

    /*
       *     b1  b2
       * s1 150 200
       *    ASK
       * b1 100  30
       * b2 150  28
       * s1 bid limit order qty = 200 price = 29
       * */
    @Test
    @Transactional
    @Rollback(true)
    public void limitOrderTest6() throws Exception {
        OrderBook lob = orderBookBuilder.getInstance();

        Counterparty buyer1 = counterPartyService.addCounterParty(BigInteger.valueOf(1), "buyer1");
        Counterparty buyer2 = counterPartyService.addCounterParty(BigInteger.valueOf(4), "buyer2");
        Counterparty supplier1 = counterPartyService.addCounterParty(BigInteger.valueOf(2), "supplier1");

        invoiceService.addInvoice(BigInteger.valueOf(11), supplier1, buyer1, BigDecimal.valueOf(150));
        invoiceService.addInvoice(BigInteger.valueOf(13), supplier1, buyer2, BigDecimal.valueOf(200));

        limitOrderBookService.addAskLimitOrder(lob, BigInteger.ONE, buyer1, 100, 30d);
        limitOrderBookService.addAskLimitOrder(lob, BigInteger.ONE, buyer2, 150, 28d);

        double supplier1BidPrice = 29d;
        limitOrderBookService.addBidLimitOrder(lob, BigInteger.ONE, supplier1, 200, supplier1BidPrice);

        Trade trade1 = findTradeByBuyerAndSeller(lob.getTape(), supplier1.getId().intValue(), buyer2.getId().intValue());
        Assert.assertNotNull(trade1);
        Assert.assertEquals(28d, trade1.getPrice(), 0d);
        Assert.assertEquals(150, trade1.getQty());

        Assert.assertEquals(50, lob.getVolumeAtPrice(OrderSide.BID.getCoreName(), 29d));
        Assert.assertEquals(100, lob.getVolumeAtPrice(OrderSide.ASK.getCoreName(), 30d));
    }

    @Test
    @Transactional
    @Rollback(true)
    public void limitOrderTest7() throws Exception {
        OrderBook lob = orderBookBuilder.getInstance();

        Counterparty buyer1 = counterPartyService.addCounterParty(BigInteger.valueOf(1), "buyer1");
        Counterparty buyer2 = counterPartyService.addCounterParty(BigInteger.valueOf(4), "buyer2");
        Counterparty supplier1 = counterPartyService.addCounterParty(BigInteger.valueOf(2), "supplier1");

        invoiceService.addInvoice(BigInteger.valueOf(11), supplier1, buyer1, BigDecimal.valueOf(150));
        invoiceService.addInvoice(BigInteger.valueOf(13), supplier1, buyer2, BigDecimal.valueOf(100));

        limitOrderBookService.addAskLimitOrder(lob, BigInteger.ONE, buyer1, 100, 30d);
        limitOrderBookService.addAskLimitOrder(lob, BigInteger.ONE, buyer2, 150, 28d);

        double supplier1BidPrice = 29d;
        limitOrderBookService.addBidLimitOrder(lob, BigInteger.ONE, supplier1, 200, supplier1BidPrice);

        Trade trade1 = findTradeByBuyerAndSeller(lob.getTape(), supplier1.getId().intValue(), buyer2.getId().intValue());
        Assert.assertNotNull(trade1);
        Assert.assertEquals(28d, trade1.getPrice(), 0d);
        Assert.assertEquals(100, trade1.getQty());

        Assert.assertEquals(100, lob.getVolumeAtPrice(OrderSide.BID.getCoreName(), 29d));
        Assert.assertEquals(50, lob.getVolumeAtPrice(OrderSide.ASK.getCoreName(), 28d));
        Assert.assertEquals(100, lob.getVolumeAtPrice(OrderSide.ASK.getCoreName(), 30d));

    }

    private Trade findTradeByBuyerAndSeller(List<Trade> trades, int buyerID, int sellerId) {
        for (Trade trade : trades) {
            if (trade.getBuyer() == buyerID && trade.getSeller() == sellerId) {
                return trade;
            }
        }

        return null;
    }
}
