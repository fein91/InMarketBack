package com.fein91.core.service;

import com.fein91.InMarketApplication;
import com.fein91.OrderRequestBuilder;
import com.fein91.core.model.OrderBook;
import com.fein91.model.OrderRequest;
import com.fein91.model.OrderResult;
import com.fein91.core.model.OrderSide;
import com.fein91.core.model.Trade;
import com.fein91.dao.CounterpartyRepository;
import com.fein91.model.Counterparty;
import com.fein91.model.OrderType;
import com.fein91.service.CounterPartyService;
import com.fein91.service.InvoiceService;
import com.fein91.service.OrderRequestService;
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
    @Autowired
    OrderRequestService orderRequestService;

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
        Counterparty supplier = counterPartyService.addCounterParty(BigInteger.valueOf(1), "supplier");
        Counterparty buyer1 = counterPartyService.addCounterParty(BigInteger.valueOf(2), "buyer1");
        Counterparty buyer2 = counterPartyService.addCounterParty(BigInteger.valueOf(3), "buyer2");
        Counterparty buyer3 = counterPartyService.addCounterParty(BigInteger.valueOf(4), "buyer3");

        invoiceService.addInvoice(BigInteger.valueOf(11), supplier, buyer1, BigDecimal.valueOf(100), BigDecimal.ZERO);
        invoiceService.addInvoice(BigInteger.valueOf(12), supplier, buyer2, BigDecimal.valueOf(200), BigDecimal.ZERO);
        invoiceService.addInvoice(BigInteger.valueOf(13), supplier, buyer3, BigDecimal.valueOf(50), BigDecimal.ZERO);


        double buyer1AskPrice = 27d;
        OrderRequest askOrderRequest1 = new OrderRequestBuilder(BigInteger.valueOf(1235), buyer1)
                .quantity(BigDecimal.valueOf(200))
                .price(BigDecimal.valueOf(buyer1AskPrice))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestService.addOrderRequest(askOrderRequest1);

        double buyer2AskPrice = 28d;
        OrderRequest askOrderRequest2 = new OrderRequestBuilder(BigInteger.valueOf(1236), buyer2)
                .quantity(BigDecimal.valueOf(150))
                .price(BigDecimal.valueOf(buyer2AskPrice))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestService.addOrderRequest(askOrderRequest2);

        double buyer3AskPrice = 29d;
        OrderRequest askOrderRequest3 = new OrderRequestBuilder(BigInteger.valueOf(1237), buyer3)
                .quantity(BigDecimal.valueOf(100))
                .price(BigDecimal.valueOf(buyer3AskPrice))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestService.addOrderRequest(askOrderRequest3);

        OrderRequest marketOrderRequest1 = new OrderRequestBuilder(BigInteger.valueOf(1234), supplier)
                .quantity(BigDecimal.valueOf(350))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.MARKET)
                .build();
        OrderResult result = orderRequestService.processOrderRequest(marketOrderRequest1);

        Trade trade1 = findTradeByBuyerAndSeller(result.getTape(), supplier.getId().intValue(), buyer1.getId().intValue());
        Assert.assertNotNull(trade1);
        Assert.assertEquals(trade1.getPrice(), buyer1AskPrice, 0d);
        Assert.assertEquals(trade1.getQty(), 100);
        Trade trade2 = findTradeByBuyerAndSeller(result.getTape(), supplier.getId().intValue(), buyer2.getId().intValue());
        Assert.assertNotNull(trade2);
        Assert.assertEquals(trade2.getPrice(), buyer2AskPrice, 0d);
        Assert.assertEquals(trade2.getQty(), 150);
        Trade trade3 = findTradeByBuyerAndSeller(result.getTape(), supplier.getId().intValue(), buyer3.getId().intValue());
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
        Counterparty buyer = counterPartyService.addCounterParty(BigInteger.valueOf(1), "buyer");
        Counterparty supplier1 = counterPartyService.addCounterParty(BigInteger.valueOf(2), "supplier1");
        Counterparty supplier2 = counterPartyService.addCounterParty(BigInteger.valueOf(3), "supplier2");
        Counterparty supplier3 = counterPartyService.addCounterParty(BigInteger.valueOf(4), "supplier3");
        Counterparty supplier4 = counterPartyService.addCounterParty(BigInteger.valueOf(6), "supplier4");

        invoiceService.addInvoice(BigInteger.valueOf(11), supplier1, buyer, BigDecimal.valueOf(100), BigDecimal.ZERO);
        invoiceService.addInvoice(BigInteger.valueOf(12), supplier2, buyer, BigDecimal.valueOf(200), BigDecimal.ZERO);
        invoiceService.addInvoice(BigInteger.valueOf(13), supplier3, buyer, BigDecimal.valueOf(50), BigDecimal.ZERO);
        invoiceService.addInvoice(BigInteger.valueOf(14), supplier4, buyer, BigDecimal.valueOf(50), BigDecimal.ZERO);


        double supplier1AskPrice = 27d;
        OrderRequest bidOrderRequest1 = new OrderRequestBuilder(BigInteger.valueOf(1235), supplier1)
                .quantity(BigDecimal.valueOf(200))
                .price(BigDecimal.valueOf(supplier1AskPrice))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestService.addOrderRequest(bidOrderRequest1);

        double supplier2AskPrice = 28d;
        OrderRequest bidOrderRequest2 = new OrderRequestBuilder(BigInteger.valueOf(1236), supplier2)
                .quantity(BigDecimal.valueOf(150))
                .price(BigDecimal.valueOf(supplier2AskPrice))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestService.addOrderRequest(bidOrderRequest2);

        double supplier3AskPrice = 29d;
        OrderRequest bidOrderRequest3 = new OrderRequestBuilder(BigInteger.valueOf(1237), supplier3)
                .quantity(BigDecimal.valueOf(100))
                .price(BigDecimal.valueOf(supplier3AskPrice))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestService.addOrderRequest(bidOrderRequest3);

        double supplier4AskPrice = 31d;
        OrderRequest bidOrderRequest4 = new OrderRequestBuilder(BigInteger.valueOf(1238), supplier4)
                .quantity(BigDecimal.valueOf(100))
                .price(BigDecimal.valueOf(supplier4AskPrice))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestService.addOrderRequest(bidOrderRequest4);

        OrderRequest askMarketOrderRequest = new OrderRequestBuilder(BigInteger.valueOf(1239), buyer)
                .quantity(BigDecimal.valueOf(350))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.MARKET)
                .build();
        OrderResult result = orderRequestService.processOrderRequest(askMarketOrderRequest);

        Trade trade1 = findTradeByBuyerAndSeller(result.getTape(), supplier1.getId().intValue(), buyer.getId().intValue());
        Assert.assertNotNull(trade1);
        Assert.assertEquals(trade1.getPrice(), supplier1AskPrice, 0d);
        Assert.assertEquals(trade1.getQty(), 100);

        Trade trade2 = findTradeByBuyerAndSeller(result.getTape(), supplier2.getId().intValue(), buyer.getId().intValue());
        Assert.assertNotNull(trade2);
        Assert.assertEquals(trade2.getPrice(), supplier2AskPrice, 0d);
        Assert.assertEquals(trade2.getQty(), 150);

        Trade trade3 = findTradeByBuyerAndSeller(result.getTape(), supplier3.getId().intValue(), buyer.getId().intValue());
        Assert.assertNotNull(trade3);
        Assert.assertEquals(trade3.getPrice(), supplier3AskPrice, 0d);
        Assert.assertEquals(trade3.getQty(), 50);

        Trade trade4 = findTradeByBuyerAndSeller(result.getTape(), supplier4.getId().intValue(), buyer.getId().intValue());
        Assert.assertNotNull(trade4);
        Assert.assertEquals(trade4.getPrice(), supplier4AskPrice, 0d);
        Assert.assertEquals(trade4.getQty(), 50);

        Assert.assertEquals(350, result.getSatisfiedDemand());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void marketOrderTest3() throws Exception {
        Counterparty buyer1 = counterPartyService.addCounterParty(BigInteger.valueOf(1), "buyer1");
        Counterparty buyer2 = counterPartyService.addCounterParty(BigInteger.valueOf(2), "buyer2");

        Counterparty supplier1 = counterPartyService.addCounterParty(BigInteger.valueOf(3), "supplier1");
        Counterparty supplier5 = counterPartyService.addCounterParty(BigInteger.valueOf(7), "supplier5");

        invoiceService.addInvoice(BigInteger.valueOf(11), supplier1, buyer1, BigDecimal.valueOf(100), BigDecimal.ZERO);
        invoiceService.addInvoice(BigInteger.valueOf(14), supplier5, buyer2, BigDecimal.valueOf(150), BigDecimal.ZERO);

        double supplier1AskPrice = 27d;
        OrderRequest bidOrderRequest1 = new OrderRequestBuilder(BigInteger.valueOf(1238), supplier1)
                .quantity(BigDecimal.valueOf(300))
                .price(BigDecimal.valueOf(supplier1AskPrice))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestService.addOrderRequest(bidOrderRequest1);

        double supplier5AskPrice = 32d;
        OrderRequest bidOrderRequest2 = new OrderRequestBuilder(BigInteger.valueOf(1239), supplier5)
                .quantity(BigDecimal.valueOf(400))
                .price(BigDecimal.valueOf(supplier5AskPrice))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestService.addOrderRequest(bidOrderRequest2);

        OrderRequest askMarketOrderRequest = new OrderRequestBuilder(BigInteger.valueOf(1240), buyer1)
                .quantity(BigDecimal.valueOf(350))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.MARKET)
                .build();
        OrderResult result = orderRequestService.processOrderRequest(askMarketOrderRequest);

        Trade trade1 = findTradeByBuyerAndSeller(result.getTape(), supplier1.getId().intValue(), buyer1.getId().intValue());
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
        Counterparty supplier = counterPartyService.addCounterParty(BigInteger.valueOf(1), "supplier");
        Counterparty buyer1 = counterPartyService.addCounterParty(BigInteger.valueOf(2), "buyer1");
        Counterparty buyer2 = counterPartyService.addCounterParty(BigInteger.valueOf(3), "buyer2");
        Counterparty buyer3 = counterPartyService.addCounterParty(BigInteger.valueOf(4), "buyer3");

        invoiceService.addInvoice(BigInteger.valueOf(11), supplier, buyer1, BigDecimal.valueOf(100), BigDecimal.ZERO);
        invoiceService.addInvoice(BigInteger.valueOf(12), supplier, buyer2, BigDecimal.valueOf(100), BigDecimal.ZERO);
        invoiceService.addInvoice(BigInteger.valueOf(13), supplier, buyer3, BigDecimal.valueOf(800), BigDecimal.ZERO);


        double buyer1AskPrice = 29d;
        OrderRequest askOrderRequest1 = new OrderRequestBuilder(BigInteger.valueOf(1238), buyer1)
                .quantity(BigDecimal.valueOf(600))
                .price(BigDecimal.valueOf(buyer1AskPrice))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestService.addOrderRequest(askOrderRequest1);

        double buyer2AskPrice = 30d;
        OrderRequest askOrderRequest2 = new OrderRequestBuilder(BigInteger.valueOf(1239), buyer2)
                .quantity(BigDecimal.valueOf(100))
                .price(BigDecimal.valueOf(buyer2AskPrice))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestService.addOrderRequest(askOrderRequest2);

        double buyer3AskPrice = 31d;
        OrderRequest askOrderRequest3 = new OrderRequestBuilder(BigInteger.valueOf(1240), buyer3)
                .quantity(BigDecimal.valueOf(500))
                .price(BigDecimal.valueOf(buyer3AskPrice))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestService.addOrderRequest(askOrderRequest3);

        OrderRequest bidMarketOrderRequest = new OrderRequestBuilder(BigInteger.valueOf(1241), supplier)
                .quantity(BigDecimal.valueOf(450))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.MARKET)
                .build();
        OrderResult result = orderRequestService.processOrderRequest(bidMarketOrderRequest);

        Trade trade1 = findTradeByBuyerAndSeller(result.getTape(), supplier.getId().intValue(), buyer1.getId().intValue());
        Assert.assertNotNull(trade1);
        Assert.assertEquals(trade1.getPrice(), buyer1AskPrice, 0d);
        Assert.assertEquals(trade1.getQty(), 100);
        Trade trade2 = findTradeByBuyerAndSeller(result.getTape(), supplier.getId().intValue(), buyer2.getId().intValue());
        Assert.assertNotNull(trade2);
        Assert.assertEquals(trade2.getPrice(), buyer2AskPrice, 0d);
        Assert.assertEquals(trade2.getQty(), 100);
        Trade trade3 = findTradeByBuyerAndSeller(result.getTape(), supplier.getId().intValue(), buyer3.getId().intValue());
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
        Counterparty buyer = counterPartyService.addCounterParty(BigInteger.valueOf(1), "buyer");
        Counterparty supplier1 = counterPartyService.addCounterParty(BigInteger.valueOf(2), "supplier1");
        Counterparty supplier2 = counterPartyService.addCounterParty(BigInteger.valueOf(3), "supplier2");
        Counterparty supplier3 = counterPartyService.addCounterParty(BigInteger.valueOf(4), "supplier3");

        invoiceService.addInvoice(BigInteger.valueOf(11), supplier1, buyer, BigDecimal.valueOf(550), BigDecimal.ZERO);
        invoiceService.addInvoice(BigInteger.valueOf(13), supplier3, buyer, BigDecimal.valueOf(200), BigDecimal.ZERO);

        double supplier1AskPrice = 28d;
        OrderRequest bidOrderRequest1 = new OrderRequestBuilder(BigInteger.valueOf(1239), supplier1)
                .quantity(BigDecimal.valueOf(550))
                .price(BigDecimal.valueOf(supplier1AskPrice))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestService.addOrderRequest(bidOrderRequest1);

        double supplier3AskPrice = 26d;
        OrderRequest bidOrderRequest2 = new OrderRequestBuilder(BigInteger.valueOf(1240), supplier3)
                .quantity(BigDecimal.valueOf(200))
                .price(BigDecimal.valueOf(supplier3AskPrice))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestService.addOrderRequest(bidOrderRequest2);

        OrderRequest askMarketOrderRequest = new OrderRequestBuilder(BigInteger.valueOf(1241), buyer)
                .quantity(BigDecimal.valueOf(250))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.MARKET)
                .build();
        OrderResult result = orderRequestService.processOrderRequest(askMarketOrderRequest);

        Trade trade1 = findTradeByBuyerAndSeller(result.getTape(), supplier1.getId().intValue(), buyer.getId().intValue());
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
        Counterparty buyer1 = counterPartyService.addCounterParty(BigInteger.valueOf(1), "buyer1");
        Counterparty buyer2 = counterPartyService.addCounterParty(BigInteger.valueOf(4), "buyer2");
        Counterparty supplier1 = counterPartyService.addCounterParty(BigInteger.valueOf(2), "supplier1");

        invoiceService.addInvoice(BigInteger.valueOf(11), supplier1, buyer1, BigDecimal.valueOf(150), BigDecimal.ZERO);
        invoiceService.addInvoice(BigInteger.valueOf(13), supplier1, buyer2, BigDecimal.valueOf(200), BigDecimal.ZERO);

        OrderRequest askOrderRequest1 = new OrderRequestBuilder(BigInteger.valueOf(1240), buyer1)
                .quantity(BigDecimal.valueOf(100))
                .price(BigDecimal.valueOf(30d))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestService.addOrderRequest(askOrderRequest1);

        OrderRequest askOrderRequest2 = new OrderRequestBuilder(BigInteger.valueOf(1241), buyer2)
                .quantity(BigDecimal.valueOf(150))
                .price(BigDecimal.valueOf(28d))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestService.addOrderRequest(askOrderRequest2);

        double supplier1BidPrice = 29d;
        OrderRequest bidOrderRequest = new OrderRequestBuilder(BigInteger.valueOf(1242), supplier1)
                .quantity(BigDecimal.valueOf(200))
                .price(BigDecimal.valueOf(supplier1BidPrice))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .build();
        OrderResult result = orderRequestService.processOrderRequest(bidOrderRequest);

        Trade trade1 = findTradeByBuyerAndSeller(result.getTape(), supplier1.getId().intValue(), buyer2.getId().intValue());
        Assert.assertNotNull(trade1);
        Assert.assertEquals(28d, trade1.getPrice(), 0d);
        Assert.assertEquals(150, trade1.getQty());

        //TODO fix it
//        Assert.assertEquals(50, lob.getVolumeAtPrice(OrderSide.BID.getCoreName(), 29d));
//        Assert.assertEquals(100, lob.getVolumeAtPrice(OrderSide.ASK.getCoreName(), 30d));
    }

    @Test
    @Transactional
    @Rollback(true)
    public void limitOrderTest7() throws Exception {
        Counterparty buyer1 = counterPartyService.addCounterParty(BigInteger.valueOf(1), "buyer1");
        Counterparty buyer2 = counterPartyService.addCounterParty(BigInteger.valueOf(4), "buyer2");
        Counterparty supplier1 = counterPartyService.addCounterParty(BigInteger.valueOf(2), "supplier1");

        invoiceService.addInvoice(BigInteger.valueOf(11), supplier1, buyer1, BigDecimal.valueOf(150), BigDecimal.ZERO);
        invoiceService.addInvoice(BigInteger.valueOf(13), supplier1, buyer2, BigDecimal.valueOf(100), BigDecimal.ZERO);

        OrderRequest askOrderRequest1 = new OrderRequestBuilder(BigInteger.valueOf(1241), buyer1)
                .quantity(BigDecimal.valueOf(100))
                .price(BigDecimal.valueOf(30d))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestService.addOrderRequest(askOrderRequest1);

        OrderRequest askOrderRequest2 = new OrderRequestBuilder(BigInteger.valueOf(1242), buyer2)
                .quantity(BigDecimal.valueOf(150))
                .price(BigDecimal.valueOf(28d))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestService.addOrderRequest(askOrderRequest2);

        double supplier1BidPrice = 29d;
        OrderRequest bidOrderRequest = new OrderRequestBuilder(BigInteger.valueOf(1243), supplier1)
                .quantity(BigDecimal.valueOf(200))
                .price(BigDecimal.valueOf(supplier1BidPrice))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .build();
        OrderResult result = orderRequestService.processOrderRequest(bidOrderRequest);

        Trade trade1 = findTradeByBuyerAndSeller(result.getTape(), supplier1.getId().intValue(), buyer2.getId().intValue());
        Assert.assertNotNull(trade1);
        Assert.assertEquals(28d, trade1.getPrice(), 0d);
        Assert.assertEquals(100, trade1.getQty());

        //TODO fix it
//        Assert.assertEquals(100, lob.getVolumeAtPrice(OrderSide.BID.getCoreName(), 29d));
//        Assert.assertEquals(50, lob.getVolumeAtPrice(OrderSide.ASK.getCoreName(), 28d));
//        Assert.assertEquals(100, lob.getVolumeAtPrice(OrderSide.ASK.getCoreName(), 30d));
    }

    /*
    *     b1
    * s1 100
    * s2 150
    *       BID
    * s1 15 200
    * s2 14 200
    * b1 market order == 100
    * b1 market order == 200
    * */
    @Test
    @Transactional
    @Rollback(true)
    public void marketOrdersOneByOne() {
        Counterparty buyer = counterPartyService.addCounterParty(BigInteger.valueOf(123), "buyer");

        Counterparty supplier1 = counterPartyService.addCounterParty(BigInteger.valueOf(124), "supplier1");
        OrderRequest bidOrderRequest1 = new OrderRequestBuilder(BigInteger.valueOf(1235), supplier1)
                .quantity(BigDecimal.valueOf(200))
                .price(BigDecimal.valueOf(15))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestService.addOrderRequest(bidOrderRequest1);

        Counterparty supplier2 = counterPartyService.addCounterParty(BigInteger.valueOf(125), "supplier2");
        OrderRequest bidOrderRequest2 = new OrderRequestBuilder(BigInteger.valueOf(1236), supplier2)
                .quantity(BigDecimal.valueOf(200))
                .price(BigDecimal.valueOf(14))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestService.addOrderRequest(bidOrderRequest2);

        BigInteger invoice1Id = BigInteger.valueOf(11);
        invoiceService.addInvoice(invoice1Id, supplier1, buyer, BigDecimal.valueOf(100), BigDecimal.ZERO);
        BigInteger invoice2Id = BigInteger.valueOf(12);
        invoiceService.addInvoice(invoice2Id, supplier2, buyer, BigDecimal.valueOf(150), BigDecimal.ZERO);

        OrderRequest marketOrderRequest1 = new OrderRequestBuilder(BigInteger.valueOf(1234), buyer)
                .quantity(BigDecimal.valueOf(100))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.MARKET)
                .build();
        OrderResult result = orderRequestService.processOrderRequest(marketOrderRequest1);

        Trade trade1 = findTradeByBuyerAndSeller(result.getTape(), supplier1.getId().intValue(), buyer.getId().intValue());
        Assert.assertNotNull(trade1);
        Assert.assertEquals(100, trade1.getQty());
        Assert.assertEquals(15d, trade1.getPrice(), 0d);

        Assert.assertEquals(0, BigDecimal.valueOf(100).compareTo(invoiceService.getById(invoice1Id).getPrepaidValue()));
        Assert.assertEquals(0, BigDecimal.valueOf(100).compareTo(invoiceService.getById(invoice1Id).getPrepaidValue()));

//        OrderRequest marketOrderRequest2 = new OrderRequestBuilder(BigInteger.valueOf(1233), buyer)
//                .quantity(BigDecimal.valueOf(200))
//                .orderSide(OrderSide.ASK)
//                .orderType(OrderType.MARKET)
//                .build();
//        orderRequestService.processOrderRequest(marketOrderRequest2);



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
