package com.fein91.service;

import com.fein91.InMarketApplication;
import com.fein91.builders.OrderRequestBuilder;
import com.fein91.core.model.OrderSide;
import com.fein91.core.model.Trade;
import com.fein91.model.*;
import com.fein91.rest.exception.OrderRequestProcessingException;
import com.fein91.service.CounterPartyService;
import com.fein91.service.InvoiceService;
import com.fein91.service.OrderRequestService;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static java.math.BigDecimal.ZERO;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = InMarketApplication.class)
public class LimitOrderBookServiceTest {

    public static final Date NEW_DATE = new Date();
    @Autowired
    CounterPartyService counterPartyService;
    @Autowired
    @Qualifier("InvoiceServiceImpl")
    InvoiceService invoiceServiceImpl;
    @Autowired
    @Qualifier("OrderRequestServiceImpl")
    OrderRequestService orderRequestServiceImpl;

    /*
    *     b1  b2  b3
    * s1 100 200  50
    *    ASK
    * b3 100 29
    * b2 150 28
    * b1 50 27
    * s1 BID market order == 350
    * */
    @Test
    @Transactional
    @Rollback
    public void marketOrderTest1() throws Exception {
        Counterparty supplier = counterPartyService.addCounterParty("supplier");
        Counterparty buyer1 = counterPartyService.addCounterParty("buyer1");
        Counterparty buyer2 = counterPartyService.addCounterParty("buyer2");
        Counterparty buyer3 = counterPartyService.addCounterParty("buyer3");

        Invoice invoiceSB1 = invoiceServiceImpl.addInvoice(new Invoice(supplier, buyer1, BigDecimal.valueOf(100), ZERO, getCurrentDayPlusDays(40)));
        Invoice invoiceSB2 = invoiceServiceImpl.addInvoice(new Invoice(supplier, buyer2, BigDecimal.valueOf(200), ZERO, getCurrentDayPlusDays(60)));
        Invoice invoiceSB3 = invoiceServiceImpl.addInvoice(new Invoice(supplier, buyer3, BigDecimal.valueOf(50), ZERO, getCurrentDayPlusDays(60)));

        double buyer1AskPrice = 27d;
        OrderRequest askOrderRequest1 = new OrderRequestBuilder(buyer1)
                .quantity(BigDecimal.valueOf(50))
                .price(BigDecimal.valueOf(buyer1AskPrice))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestServiceImpl.process(askOrderRequest1);

        double buyer2AskPrice = 28d;
        OrderRequest askOrderRequest2 = new OrderRequestBuilder(buyer2)
                .quantity(BigDecimal.valueOf(150))
                .price(BigDecimal.valueOf(buyer2AskPrice))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestServiceImpl.process(askOrderRequest2);

        double buyer3AskPrice = 29d;
        OrderRequest askOrderRequest3 = new OrderRequestBuilder(buyer3)
                .quantity(BigDecimal.valueOf(45))
                .price(BigDecimal.valueOf(buyer3AskPrice))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestServiceImpl.process(askOrderRequest3);

        OrderRequest marketOrderRequest1 = new OrderRequestBuilder(supplier)
                .quantity(BigDecimal.valueOf(350))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.MARKET)
                .invoicesChecked(ImmutableMap.of(invoiceSB1.getId(), true, invoiceSB2.getId(), true, invoiceSB3.getId(), true))
                .build();
        OrderResult result = orderRequestServiceImpl.process(marketOrderRequest1);

        Trade trade1 = findTradeByBuyerAndSeller(result.getTape(), supplier.getId(), buyer1.getId());
        Assert.assertNotNull(trade1);
        Assert.assertEquals(trade1.getPrice(), buyer1AskPrice, 0d);
        Assert.assertEquals(BigDecimal.valueOf(50).compareTo(trade1.getQty()), 0);
        Trade trade2 = findTradeByBuyerAndSeller(result.getTape(), supplier.getId(), buyer2.getId());
        Assert.assertNotNull(trade2);
        Assert.assertEquals(trade2.getPrice(), buyer2AskPrice, 0d);
        Assert.assertEquals(BigDecimal.valueOf(150).compareTo(trade2.getQty()), 0);
        Trade trade3 = findTradeByBuyerAndSeller(result.getTape(), supplier.getId(), buyer3.getId());
        Assert.assertNotNull(trade3);
        Assert.assertEquals(trade3.getPrice(), buyer3AskPrice, 0d);
        Assert.assertEquals("Actual qty: " + trade3.getQty(), BigDecimal.valueOf(45).compareTo(trade3.getQty()), 0);

        Assert.assertEquals("Actual satisfied demand: " + result.getSatisfiedDemand(), BigDecimal.valueOf(245).compareTo(result.getSatisfiedDemand()), 0);
        Assert.assertEquals("Actual APR: " + result.getApr(), 0, BigDecimal.valueOf(27.98).compareTo(result.getApr()));

        invoiceSB1 = invoiceServiceImpl.getById(invoiceSB1.getId());
        Assert.assertEquals("Actual invoice prepaid value: " + invoiceSB1.getPrepaidValue(),
                0, BigDecimal.valueOf(51.479452055000).compareTo(invoiceSB1.getPrepaidValue()));
        invoiceSB2 = invoiceServiceImpl.getById(invoiceSB2.getId());
        Assert.assertEquals("Actual invoice prepaid value: " + invoiceSB2.getPrepaidValue(),
                0, BigDecimal.valueOf(156.904109595).compareTo(invoiceSB2.getPrepaidValue()));
        invoiceSB3 = invoiceServiceImpl.getById(invoiceSB3.getId());
        Assert.assertEquals("Actual invoice prepaid value: " + invoiceSB3.getPrepaidValue(),
                0, BigDecimal.valueOf(47.1452054805).compareTo(invoiceSB3.getPrepaidValue()));
    }

    @Test
    @Transactional
    @Rollback
    public void marketOrderTest2() throws Exception {
        Counterparty buyer = counterPartyService.addCounterParty("buyer");
        Counterparty supplier1 = counterPartyService.addCounterParty("supplier1");
        Counterparty supplier2 = counterPartyService.addCounterParty("supplier2");
        Counterparty supplier3 = counterPartyService.addCounterParty("supplier3");
        Counterparty supplier4 = counterPartyService.addCounterParty("supplier4");

        Invoice invoiceS1B = invoiceServiceImpl.addInvoice(new Invoice(supplier1, buyer, BigDecimal.valueOf(100), ZERO, NEW_DATE));
        Invoice invoiceS2B = invoiceServiceImpl.addInvoice(new Invoice(supplier2, buyer, BigDecimal.valueOf(200), ZERO, NEW_DATE));
        Invoice invoiceS3B = invoiceServiceImpl.addInvoice(new Invoice(supplier3, buyer, BigDecimal.valueOf(50), ZERO, NEW_DATE));
        Invoice invoiceS4B = invoiceServiceImpl.addInvoice(new Invoice(supplier4, buyer, BigDecimal.valueOf(50), ZERO, NEW_DATE));


        double supplier1AskPrice = 27d;
        OrderRequest bidOrderRequest1 = new OrderRequestBuilder(supplier1)
                .quantity(BigDecimal.valueOf(200))
                .price(BigDecimal.valueOf(supplier1AskPrice))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestServiceImpl.save(bidOrderRequest1);

        double supplier2AskPrice = 28d;
        OrderRequest bidOrderRequest2 = new OrderRequestBuilder(supplier2)
                .quantity(BigDecimal.valueOf(150))
                .price(BigDecimal.valueOf(supplier2AskPrice))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestServiceImpl.save(bidOrderRequest2);

        double supplier3AskPrice = 29d;
        OrderRequest bidOrderRequest3 = new OrderRequestBuilder(supplier3)
                .quantity(BigDecimal.valueOf(100))
                .price(BigDecimal.valueOf(supplier3AskPrice))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestServiceImpl.save(bidOrderRequest3);

        double supplier4AskPrice = 31d;
        OrderRequest bidOrderRequest4 = new OrderRequestBuilder(supplier4)
                .quantity(BigDecimal.valueOf(100))
                .price(BigDecimal.valueOf(supplier4AskPrice))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestServiceImpl.save(bidOrderRequest4);

        OrderRequest askMarketOrderRequest = new OrderRequestBuilder(buyer)
                .quantity(BigDecimal.valueOf(350))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.MARKET)
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true, invoiceS2B.getId(), true, invoiceS3B.getId(), true, invoiceS4B.getId(), true))
                .build();
        OrderResult result = orderRequestServiceImpl.process(askMarketOrderRequest);

        Trade trade1 = findTradeByBuyerAndSeller(result.getTape(), supplier1.getId(), buyer.getId());
        Assert.assertNotNull(trade1);
        Assert.assertEquals(trade1.getPrice(), supplier1AskPrice, 0d);
        Assert.assertEquals(trade1.getQty(), BigDecimal.valueOf(100));

        Trade trade2 = findTradeByBuyerAndSeller(result.getTape(), supplier2.getId(), buyer.getId());
        Assert.assertNotNull(trade2);
        Assert.assertEquals(trade2.getPrice(), supplier2AskPrice, 0d);
        Assert.assertEquals(trade2.getQty(), BigDecimal.valueOf(150));

        Trade trade3 = findTradeByBuyerAndSeller(result.getTape(), supplier3.getId(), buyer.getId());
        Assert.assertNotNull(trade3);
        Assert.assertEquals(trade3.getPrice(), supplier3AskPrice, 0d);
        Assert.assertEquals(trade3.getQty(), BigDecimal.valueOf(50));

        Trade trade4 = findTradeByBuyerAndSeller(result.getTape(), supplier4.getId(), buyer.getId());
        Assert.assertNotNull(trade4);
        Assert.assertEquals(trade4.getPrice(), supplier4AskPrice, 0d);
        Assert.assertEquals(trade4.getQty(), BigDecimal.valueOf(50));

        Assert.assertEquals(BigDecimal.valueOf(350).compareTo(result.getSatisfiedDemand()), 0);
    }

    /*
    *   source  target  value paymentDate
    *     s1      b1     100    0
    *       BID
    * s1 27 90
    * b1 ask market order == 150
    * expected OrderRequestProcessingException, because order request quantity can't be greater than invoices sum
    * */
    @Test(expected = OrderRequestProcessingException.class)
    @Transactional
    @Rollback
    public void marketOrderTest3() throws Exception {
        Counterparty buyer = counterPartyService.addCounterParty("buyer");
        Counterparty supplier = counterPartyService.addCounterParty("supplier");

        Invoice invoice1SB = invoiceServiceImpl.addInvoice(new Invoice(supplier, buyer, BigDecimal.valueOf(100), ZERO, NEW_DATE));
        Invoice invoice2SB = invoiceServiceImpl.addInvoice(new Invoice(supplier, buyer, BigDecimal.valueOf(100), ZERO, NEW_DATE));

        OrderRequest bidOrderRequest1 = new OrderRequestBuilder(supplier)
                .quantity(BigDecimal.valueOf(90))
                .price(BigDecimal.valueOf(27d))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestServiceImpl.save(bidOrderRequest1);

        OrderRequest askMarketOrderRequest = new OrderRequestBuilder(buyer)
                .quantity(BigDecimal.valueOf(250))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.MARKET)
                .invoicesChecked(ImmutableMap.of(invoice1SB.getId(), true, invoice2SB.getId(), true))
                .build();
        orderRequestServiceImpl.process(askMarketOrderRequest);
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
    @Rollback
    public void marketOrderTest4() throws Exception {
        Counterparty supplier = counterPartyService.addCounterParty("supplier");
        Counterparty buyer1 = counterPartyService.addCounterParty("buyer1");
        Counterparty buyer2 = counterPartyService.addCounterParty("buyer2");
        Counterparty buyer3 = counterPartyService.addCounterParty("buyer3");

        Invoice invoiceSB1 = invoiceServiceImpl.addInvoice(new Invoice(supplier, buyer1, BigDecimal.valueOf(100), ZERO, NEW_DATE));
        Invoice invoiceSB2 = invoiceServiceImpl.addInvoice(new Invoice(supplier, buyer2, BigDecimal.valueOf(100), ZERO, NEW_DATE));
        Invoice invoiceSB3 = invoiceServiceImpl.addInvoice(new Invoice(supplier, buyer3, BigDecimal.valueOf(800), ZERO, NEW_DATE));

        double buyer1AskPrice = 29d;
        OrderRequest askOrderRequest1 = new OrderRequestBuilder(buyer1)
                .quantity(BigDecimal.valueOf(600))
                .price(BigDecimal.valueOf(buyer1AskPrice))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestServiceImpl.save(askOrderRequest1);

        double buyer2AskPrice = 30d;
        OrderRequest askOrderRequest2 = new OrderRequestBuilder(buyer2)
                .quantity(BigDecimal.valueOf(100))
                .price(BigDecimal.valueOf(buyer2AskPrice))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestServiceImpl.save(askOrderRequest2);

        double buyer3AskPrice = 31d;
        OrderRequest askOrderRequest3 = new OrderRequestBuilder(buyer3)
                .quantity(BigDecimal.valueOf(500))
                .price(BigDecimal.valueOf(buyer3AskPrice))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestServiceImpl.save(askOrderRequest3);

        OrderRequest bidMarketOrderRequest = new OrderRequestBuilder(supplier)
                .quantity(BigDecimal.valueOf(450))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.MARKET)
                .invoicesChecked(ImmutableMap.of(invoiceSB1.getId(), true, invoiceSB2.getId(), true, invoiceSB3.getId(), true))
                .build();
        OrderResult result = orderRequestServiceImpl.process(bidMarketOrderRequest);

        Trade trade1 = findTradeByBuyerAndSeller(result.getTape(), supplier.getId(), buyer1.getId());
        Assert.assertNotNull(trade1);
        Assert.assertEquals(trade1.getPrice(), buyer1AskPrice, 0d);
        Assert.assertEquals(trade1.getQty(), BigDecimal.valueOf(100));
        Trade trade2 = findTradeByBuyerAndSeller(result.getTape(), supplier.getId(), buyer2.getId());
        Assert.assertNotNull(trade2);
        Assert.assertEquals(trade2.getPrice(), buyer2AskPrice, 0d);
        Assert.assertEquals(trade2.getQty(), BigDecimal.valueOf(100));
        Trade trade3 = findTradeByBuyerAndSeller(result.getTape(), supplier.getId(), buyer3.getId());
        Assert.assertNotNull(trade3);
        Assert.assertEquals(trade3.getPrice(), buyer3AskPrice, 0d);
        Assert.assertEquals(trade3.getQty(), BigDecimal.valueOf(250));

        Assert.assertEquals(BigDecimal.valueOf(450).compareTo(result.getSatisfiedDemand()), 0);
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
    @Rollback
    public void marketOrderTest5() throws Exception {
        Counterparty buyer = counterPartyService.addCounterParty("buyer");
        Counterparty supplier1 = counterPartyService.addCounterParty("supplier1");
        Counterparty supplier3 = counterPartyService.addCounterParty("supplier3");

        Invoice invoiceS1B = invoiceServiceImpl.addInvoice(new Invoice(supplier1, buyer, BigDecimal.valueOf(550), ZERO, NEW_DATE));
        Invoice invoiceS3B = invoiceServiceImpl.addInvoice(new Invoice(supplier3, buyer, BigDecimal.valueOf(200), ZERO, NEW_DATE));

        double supplier1AskPrice = 28d;
        OrderRequest bidOrderRequest1 = new OrderRequestBuilder(supplier1)
                .quantity(BigDecimal.valueOf(550))
                .price(BigDecimal.valueOf(supplier1AskPrice))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestServiceImpl.save(bidOrderRequest1);

        double supplier3AskPrice = 26d;
        OrderRequest bidOrderRequest2 = new OrderRequestBuilder(supplier3)
                .quantity(BigDecimal.valueOf(200))
                .price(BigDecimal.valueOf(supplier3AskPrice))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestServiceImpl.save(bidOrderRequest2);

        OrderRequest askMarketOrderRequest = new OrderRequestBuilder(buyer)
                .quantity(BigDecimal.valueOf(250))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.MARKET)
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true, invoiceS3B.getId(), true))
                .build();
        OrderResult result = orderRequestServiceImpl.process(askMarketOrderRequest);

        Trade trade1 = findTradeByBuyerAndSeller(result.getTape(), supplier1.getId(), buyer.getId());
        Assert.assertNotNull(trade1);
        Assert.assertEquals(trade1.getPrice(), supplier1AskPrice, 0d);
        Assert.assertEquals(trade1.getQty(), BigDecimal.valueOf(250));

        Assert.assertEquals(BigDecimal.valueOf(250).compareTo(result.getSatisfiedDemand()), 0);
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
    @Rollback
    public void limitOrderTest6() throws Exception {
        Counterparty buyer1 = counterPartyService.addCounterParty("buyer1");
        Counterparty buyer2 = counterPartyService.addCounterParty("buyer2");
        Counterparty supplier = counterPartyService.addCounterParty("supplier");

        Invoice invoiceSB1 = invoiceServiceImpl.addInvoice(new Invoice(supplier, buyer1, BigDecimal.valueOf(150), ZERO, getCurrentDayPlusDays(30)));
        Invoice invoiceSB2 = invoiceServiceImpl.addInvoice(new Invoice(supplier, buyer2, BigDecimal.valueOf(200), ZERO, getCurrentDayPlusDays(50)));

        OrderRequest askOrderRequest1 = new OrderRequestBuilder(buyer1)
                .quantity(BigDecimal.valueOf(100))
                .price(BigDecimal.valueOf(30d))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestServiceImpl.process(askOrderRequest1);

        OrderRequest askOrderRequest2 = new OrderRequestBuilder(buyer2)
                .quantity(BigDecimal.valueOf(150))
                .price(BigDecimal.valueOf(28d))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestServiceImpl.process(askOrderRequest2);

        BigDecimal supplier1BidPrice = BigDecimal.valueOf(29);
        OrderRequest bidOrderRequest = new OrderRequestBuilder(supplier)
                .quantity(BigDecimal.valueOf(200))
                .price(supplier1BidPrice)
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .invoicesChecked(ImmutableMap.of(invoiceSB1.getId(), true, invoiceSB2.getId(), true))
                .build();
        OrderResult result = orderRequestServiceImpl.process(bidOrderRequest);

        Trade trade1 = findTradeByBuyerAndSeller(result.getTape(), supplier.getId(), buyer2.getId());
        Assert.assertNotNull(trade1);
        Assert.assertEquals(28d, trade1.getPrice(), 0d);
        Assert.assertEquals(BigDecimal.valueOf(150).compareTo(trade1.getQty()), 0);

        List<OrderRequest> orderRequests = orderRequestServiceImpl.getByCounterpartyId(supplier.getId());
        Assert.assertEquals(1, orderRequests.size());
        OrderRequest limitOrderRequest = orderRequests.iterator().next();
        Assert.assertEquals(0, BigDecimal.valueOf(50).compareTo(limitOrderRequest.getQuantity()));
        Assert.assertEquals(0, supplier1BidPrice.compareTo(limitOrderRequest.getPrice()));
    }

    /*
   *   source  target  value paymentDate
   *     s1      b1     150    40
   *     s1      b2     100    40
   *       ASK
   * b1 28 100
   * b2 28 100
   * s1 bid limit order qty == 200 price == 29
   * TODO holy random here
   * */
    @Test
    @Transactional
    @Rollback
    @Ignore
    public void testLimitOrderRequestWithTwoOrdersPerPrice() throws Exception {
        Counterparty buyer1 = counterPartyService.addCounterParty("buyer1");
        Counterparty buyer2 = counterPartyService.addCounterParty("buyer2");
        Counterparty supplier = counterPartyService.addCounterParty("supplier");

        Invoice invoiceSB1 = invoiceServiceImpl.addInvoice(new Invoice(supplier, buyer1, BigDecimal.valueOf(150), ZERO, getCurrentDayPlusDays(40)));
        Invoice invoiceSB2 = invoiceServiceImpl.addInvoice(new Invoice(supplier, buyer2, BigDecimal.valueOf(100), ZERO, getCurrentDayPlusDays(40)));

        OrderRequest askOrderRequest1 = new OrderRequestBuilder(buyer1)
                .quantity(BigDecimal.valueOf(100))
                .price(BigDecimal.valueOf(28d))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestServiceImpl.process(askOrderRequest1);

        OrderRequest askOrderRequest2 = new OrderRequestBuilder(buyer2)
                .quantity(BigDecimal.valueOf(99))
                .price(BigDecimal.valueOf(28d))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestServiceImpl.process(askOrderRequest2);

        double supplier1BidPrice = 28d;
        OrderRequest bidOrderRequest = new OrderRequestBuilder(supplier)
                .quantity(BigDecimal.valueOf(150))
                .price(BigDecimal.valueOf(supplier1BidPrice))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .invoicesChecked(ImmutableMap.of(invoiceSB1.getId(), true, invoiceSB2.getId(), true))
                .build();
        OrderResult result = orderRequestServiceImpl.process(bidOrderRequest);

        Trade trade1 = findTradeByBuyerAndSeller(result.getTape(), supplier.getId(), buyer2.getId());
        Assert.assertNotNull(trade1);
        Assert.assertEquals(28d, trade1.getPrice(), 0d);
        Assert.assertEquals("Actual qty: " + trade1.getQty(), 0, BigDecimal.valueOf(50).compareTo(trade1.getQty()));

        Trade trade2 = findTradeByBuyerAndSeller(result.getTape(), supplier.getId(), buyer1.getId());
        Assert.assertNotNull(trade2);
        Assert.assertEquals(28d, trade2.getPrice(), 0d);
        Assert.assertEquals("Actual qty: " + trade2.getQty(), 0, BigDecimal.valueOf(50).compareTo(trade2.getQty()));

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
    @Rollback
    public void marketOrdersOneByOne() throws Exception {
        Counterparty buyer = counterPartyService.addCounterParty("buyer");

        Counterparty supplier1 = counterPartyService.addCounterParty("supplier1");
        Counterparty supplier2 = counterPartyService.addCounterParty("supplier2");

        Invoice invoiceS1B = invoiceServiceImpl.addInvoice(new Invoice(supplier1, buyer, BigDecimal.valueOf(200), ZERO, getCurrentDayPlusDays(60)));
        Invoice invoiceS2B = invoiceServiceImpl.addInvoice(new Invoice(supplier2, buyer, BigDecimal.valueOf(250), ZERO, getCurrentDayPlusDays(90)));

        OrderRequest bidOrderRequest1 = new OrderRequestBuilder(supplier1)
                .quantity(BigDecimal.valueOf(150))
                .price(BigDecimal.valueOf(15))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestServiceImpl.process(bidOrderRequest1);

        OrderRequest bidOrderRequest2 = new OrderRequestBuilder(supplier2)
                .quantity(BigDecimal.valueOf(200))
                .price(BigDecimal.valueOf(14))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestServiceImpl.process(bidOrderRequest2);

        OrderRequest marketOrderRequest1 = new OrderRequestBuilder(buyer)
                .quantity(BigDecimal.valueOf(100))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.MARKET)
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true, invoiceS2B.getId(), true))
                .build();
        OrderResult result = orderRequestServiceImpl.process(marketOrderRequest1);

        Trade trade1 = findTradeByBuyerAndSeller(result.getTape(), supplier1.getId(), buyer.getId());
        Assert.assertNotNull(trade1);
        Assert.assertEquals(BigDecimal.valueOf(100), trade1.getQty());
        Assert.assertEquals(15d, trade1.getPrice(), 0d);

        OrderRequest marketOrderRequest2 = new OrderRequestBuilder(buyer)
                .quantity(BigDecimal.valueOf(200))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.MARKET)
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true, invoiceS2B.getId(), true))
                .build();
        result = orderRequestServiceImpl.process(marketOrderRequest2);

        Trade trade2 = findTradeByBuyerAndSeller(result.getTape(), supplier1.getId(), buyer.getId());
        Assert.assertNotNull(trade2);
        Assert.assertEquals(0, BigDecimal.valueOf(50).compareTo(trade2.getQty()));
        Assert.assertEquals(15d, trade2.getPrice(), 0d);

        Trade trade3 = findTradeByBuyerAndSeller(result.getTape(), supplier2.getId(), buyer.getId());
        Assert.assertNotNull(trade3);
        Assert.assertEquals(0, BigDecimal.valueOf(150).compareTo(trade3.getQty()));
        Assert.assertEquals(14d, trade3.getPrice(), 0d);

        invoiceS1B = invoiceServiceImpl.getById(invoiceS1B.getId());
        Assert.assertEquals(0, BigDecimal.valueOf(153.69863013).compareTo(invoiceS1B.getPrepaidValue()));

        invoiceS2B = invoiceServiceImpl.getById(invoiceS2B.getId());
        Assert.assertEquals("Actual invoice prepaid value: " + invoiceS2B.getPrepaidValue(),
                0, BigDecimal.valueOf(155.178082185).compareTo(invoiceS2B.getPrepaidValue()));

//        Assert.assertEquals(0, BigDecimal.valueOf(100).compareTo(invoiceServiceImpl.getById(invoice1Id).getPrepaidValue()));
//        Assert.assertEquals(0, BigDecimal.valueOf(100).compareTo(invoiceServiceImpl.getById(invoice1Id).getPrepaidValue()));
    }

    /*
    *   source  target  value paymentDate
    *     s1      b1     200    60
    *     s2      b1     300    15
    *       BID
    * s1 26 200
    * s2 25 300
    * b1 ask market order == 350
    * */
    //TODO fix this NPE
    @Test
    @Transactional
    @Rollback
    public void testOrderWithSomeUncheckedInvoices() throws Exception {
        Counterparty buyer = counterPartyService.addCounterParty("buyer");
        Counterparty supplier1 = counterPartyService.addCounterParty("supplier1");
        Counterparty supplier2 = counterPartyService.addCounterParty("supplier2");

        Invoice invoiceS1B = invoiceServiceImpl.addInvoice(new Invoice(supplier1, buyer, BigDecimal.valueOf(200), ZERO, getCurrentDayPlusDays(60)));
        Invoice invoice1S2B = invoiceServiceImpl.addInvoice(new Invoice(supplier2, buyer, BigDecimal.valueOf(300), ZERO, getCurrentDayPlusDays(15)));
        Invoice invoice2S2B = invoiceServiceImpl.addInvoice(new Invoice(supplier2, buyer, BigDecimal.valueOf(300), ZERO, getCurrentDayPlusDays(15)));

        OrderRequest bidOrderRequest1 = new OrderRequestBuilder(supplier1)
                .quantity(BigDecimal.valueOf(100))
                .price(BigDecimal.valueOf(26))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestServiceImpl.process(bidOrderRequest1);

        OrderRequest bidOrderRequest2 = new OrderRequestBuilder(supplier2)
                .quantity(BigDecimal.valueOf(250))
                .price(BigDecimal.valueOf(25))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestServiceImpl.process(bidOrderRequest2);

        OrderRequest marketOrderRequest1 = new OrderRequestBuilder(buyer)
                .quantity(BigDecimal.valueOf(350))
                .orderSide(OrderSide.ASK)
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true, invoice1S2B.getId(), true, invoice2S2B.getId(), false))
                .orderType(OrderType.MARKET)
                .build();

        OrderResult result = orderRequestServiceImpl.process(marketOrderRequest1);
        Trade trade1 = findTradeByBuyerAndSeller(result.getTape(), supplier1.getId(), buyer.getId());
        Assert.assertNotNull(trade1);
        Assert.assertEquals(26d, trade1.getPrice(), 0d);
        Assert.assertEquals(0, BigDecimal.valueOf(191.8).compareTo(trade1.getQty()));

        Trade trade2 = findTradeByBuyerAndSeller(result.getTape(), supplier2.getId(), buyer.getId());
        Assert.assertNotNull(trade2);
        Assert.assertEquals(25d, trade2.getPrice(), 0d);
        Assert.assertEquals(0, BigDecimal.valueOf(158.2).compareTo(trade2.getQty()));

        Assert.assertEquals("Actual apr " + result.getApr(), BigDecimal.valueOf(25.55).compareTo(result.getApr()), 0);
    }

    //TODO to fix this case
    @Test
    @Transactional
    @Rollback
    public void testToFix() throws Exception {
        Counterparty buyer = counterPartyService.addCounterParty("buyer");
        Counterparty supplier = counterPartyService.addCounterParty("supplier");

        Invoice invoice1SB = invoiceServiceImpl.addInvoice(new Invoice(supplier, buyer, BigDecimal.valueOf(100), ZERO, getCurrentDayPlusDays(30)));
        Invoice invoice2SB = invoiceServiceImpl.addInvoice(new Invoice(supplier, buyer, BigDecimal.valueOf(100), ZERO, getCurrentDayPlusDays(50)));

        OrderRequest bidOrderRequest1 = new OrderRequestBuilder(buyer)
                .quantity(BigDecimal.valueOf(150))
                .price(BigDecimal.valueOf(27d))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestServiceImpl.process(bidOrderRequest1);

        OrderRequest askOrderRequest = new OrderRequestBuilder(supplier)
                .quantity(BigDecimal.valueOf(200))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.MARKET)
                .invoicesChecked(ImmutableMap.of(invoice1SB.getId(), true, invoice2SB.getId(), true))
                .build();
        orderRequestServiceImpl.process(askOrderRequest);

    }

    /*
        *   source  target  value paymentDate
        *     s1      b1     200    70
        *     s3      b1     100    70
        *       BID
        * s1 19 50
        * s2 17 100
        * b1 ask market order == 200
        * */
    @Test
    @Transactional
    @Rollback
    public void testLimitOrderCreatedAfterUnsatisfiedMarketOrder() throws Exception {
        Counterparty buyer = counterPartyService.addCounterParty("buyer");
        Counterparty supplier1 = counterPartyService.addCounterParty("supplier1");
        Counterparty supplier2 = counterPartyService.addCounterParty("supplier2");

        Invoice invoiceS1B = invoiceServiceImpl.addInvoice(new Invoice(supplier1, buyer, BigDecimal.valueOf(200), ZERO, getCurrentDayPlusDays(70)));
        Invoice invoiceS2B = invoiceServiceImpl.addInvoice(new Invoice(supplier2, buyer, BigDecimal.valueOf(100), ZERO, getCurrentDayPlusDays(70)));

        OrderRequest bidOrderRequest1 = new OrderRequestBuilder(supplier1)
                .quantity(BigDecimal.valueOf(50))
                .price(BigDecimal.valueOf(19))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestServiceImpl.process(bidOrderRequest1);

        OrderRequest bidOrderRequest3 = new OrderRequestBuilder(supplier2)
                .quantity(BigDecimal.valueOf(95))
                .price(BigDecimal.valueOf(17))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestServiceImpl.process(bidOrderRequest3);

        OrderRequest marketOrderRequest1 = new OrderRequestBuilder(buyer)
                .quantity(BigDecimal.valueOf(200))
                .orderSide(OrderSide.ASK)
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true, invoiceS2B.getId(), true))
                .orderType(OrderType.MARKET)
                .build();

        OrderResult result = orderRequestServiceImpl.process(marketOrderRequest1);
        Trade trade1 = findTradeByBuyerAndSeller(result.getTape(), supplier1.getId(), buyer.getId());
        Assert.assertNotNull(trade1);
        Assert.assertEquals(19d, trade1.getPrice(), 0d);
        Assert.assertEquals(0, BigDecimal.valueOf(50).compareTo(trade1.getQty()));

        Trade trade2 = findTradeByBuyerAndSeller(result.getTape(), supplier2.getId(), buyer.getId());
        Assert.assertNotNull(trade2);
        Assert.assertEquals(17d, trade2.getPrice(), 0d);
        Assert.assertEquals("Actual qty: " + trade2.getQty(), 0, BigDecimal.valueOf(95).compareTo(trade2.getQty()));

        invoiceS1B = invoiceServiceImpl.getById(invoiceS1B.getId());
        Assert.assertEquals(BigDecimal.valueOf(51.82), invoiceS1B.getPrepaidValue());
        invoiceS2B = invoiceServiceImpl.getById(invoiceS2B.getId());
        Assert.assertEquals("Actual invoice prepaid value: " + invoiceS2B.getPrepaidValue(),
                BigDecimal.valueOf(98.10).compareTo(invoiceS2B.getPrepaidValue()), 0);

        Assert.assertEquals(70, result.getAvgDaysToPayment().intValue());
        BigDecimal expectedApr = BigDecimal.valueOf(17.69);
        Assert.assertEquals("Actual APR " + result.getApr(), expectedApr.compareTo(result.getApr()), 0);
        Assert.assertEquals("Actual avg discount perc: " + result.getAvgDiscountPerc(), BigDecimal.valueOf(1.64).compareTo(result.getAvgDiscountPerc()), 0);

        List<OrderRequest> supplier1Orders = orderRequestServiceImpl.getByCounterpartyId(supplier1.getId());
        Assert.assertEquals(0, supplier1Orders.size());

        List<OrderRequest> supplier2Orders = orderRequestServiceImpl.getByCounterpartyId(supplier2.getId());
        Assert.assertEquals(0, supplier2Orders.size());

        List<OrderRequest> buyerOrders = orderRequestServiceImpl.getByCounterpartyId(buyer.getId());
        Assert.assertEquals(1, buyerOrders.size());
        OrderRequest buyerLimitOrder = buyerOrders.iterator().next();
        Assert.assertEquals(OrderSide.ASK, buyerLimitOrder.getOrderSide());
        Assert.assertEquals(OrderType.LIMIT, buyerLimitOrder.getOrderType());
        Assert.assertEquals(0, expectedApr.compareTo(buyerLimitOrder.getPrice()));
        Assert.assertEquals(0, BigDecimal.valueOf(55).compareTo(buyerLimitOrder.getQuantity()));

    }

    @Test
    @Transactional
    @Rollback
    public void testWithDiscountsOneInvoicePerBuyer() throws Exception {
        Counterparty buyer = counterPartyService.addCounterParty("buyer");
        Counterparty supplier1 = counterPartyService.addCounterParty("supplier1");
        Counterparty supplier2 = counterPartyService.addCounterParty("supplier2");

        Invoice invoiceS1B = invoiceServiceImpl.addInvoice(new Invoice(supplier1, buyer, BigDecimal.valueOf(400), ZERO, getDate(2016, 7, 12)));
        Invoice invoiceS2B = invoiceServiceImpl.addInvoice(new Invoice(supplier2, buyer, BigDecimal.valueOf(300), ZERO, getDate(2016, 7, 2)));

        BigDecimal bid1Price = BigDecimal.valueOf(26);
        OrderRequest bidOrderRequest1 = new OrderRequestBuilder(supplier1)
                .quantity(BigDecimal.valueOf(395))
                .price(bid1Price)
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestServiceImpl.process(bidOrderRequest1);

        OrderRequest bidOrderRequest2 = new OrderRequestBuilder(supplier2)
                .quantity(BigDecimal.valueOf(200))
                .price(BigDecimal.valueOf(25))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestServiceImpl.process(bidOrderRequest2);

        OrderRequest marketOrderRequest1 = new OrderRequestBuilder(buyer)
                .quantity(BigDecimal.valueOf(350))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.MARKET)
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true, invoiceS2B.getId(), true))
                .build();

        OrderResult result = orderRequestServiceImpl.process(marketOrderRequest1);
        Trade trade = findTradeByBuyerAndSeller(result.getTape(), supplier1.getId(), buyer.getId());
        Assert.assertNotNull(trade);
        Assert.assertEquals(bid1Price.doubleValue(), trade.getPrice(), 0d);
        Assert.assertEquals(BigDecimal.valueOf(350), trade.getQty());
    }

    @Test
    @Transactional
    @Rollback
    public void testWithDiscountsTwoInvoicesPerBuyer() throws Exception {
        Counterparty buyer = counterPartyService.addCounterParty("buyer");
        Counterparty supplier1 = counterPartyService.addCounterParty("supplier1");
        Counterparty supplier2 = counterPartyService.addCounterParty("supplier2");

        Invoice invoice1S1B = invoiceServiceImpl.addInvoice(new Invoice(supplier1, buyer, BigDecimal.valueOf(200), ZERO, getCurrentDayPlusDays(40)));
        Invoice invoice2S1B = invoiceServiceImpl.addInvoice(new Invoice(supplier1, buyer, BigDecimal.valueOf(200), ZERO, getCurrentDayPlusDays(50)));
        Invoice invoiceS2B = invoiceServiceImpl.addInvoice(new Invoice(supplier2, buyer, BigDecimal.valueOf(300), ZERO, getCurrentDayPlusDays(40)));

        BigDecimal bid1Price = BigDecimal.valueOf(26);
        OrderRequest bidOrderRequest1 = new OrderRequestBuilder(supplier1)
                .quantity(BigDecimal.valueOf(380))
                .price(bid1Price)
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestServiceImpl.process(bidOrderRequest1);

        OrderRequest bidOrderRequest2 = new OrderRequestBuilder(supplier2)
                .quantity(BigDecimal.valueOf(295))
                .price(BigDecimal.valueOf(25))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestServiceImpl.process(bidOrderRequest2);

        OrderRequest marketOrderRequest1 = new OrderRequestBuilder(buyer)
                .quantity(BigDecimal.valueOf(350))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.MARKET)
                .invoicesChecked(ImmutableMap.of(invoice1S1B.getId(), true, invoice2S1B.getId(), true, invoiceS2B.getId(), true))
                .build();

        OrderResult result = orderRequestServiceImpl.process(marketOrderRequest1);
        Trade trade = findTradeByBuyerAndSeller(result.getTape(), supplier1.getId(), buyer.getId());
        Assert.assertNotNull(trade);
        Assert.assertEquals(bid1Price.doubleValue(), trade.getPrice(), 0d);
        Assert.assertEquals("Actual trade qty: " + trade.getQty(),
                BigDecimal.valueOf(350).compareTo(trade.getQty()), 0);

        invoice1S1B = invoiceServiceImpl.getById(invoice1S1B.getId());
        Assert.assertEquals(0, BigDecimal.valueOf(200).compareTo(invoice1S1B.getPrepaidValue()));
        invoice2S1B = invoiceServiceImpl.getById(invoice2S1B.getId());
        Assert.assertEquals("Actual invoice prepaid value: " + invoice2S1B.getPrepaidValue(),
                BigDecimal.valueOf(168.01).compareTo(invoice2S1B.getPrepaidValue()), 0);


    }

    private Date getCurrentDayPlusDays(int days) {
        Instant instant = LocalDate.now().plusDays(days).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    @Deprecated
    private Date getDate(int year, int month, int day) {
        Instant instant = LocalDate.of(year, month, day).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    private Trade findTradeByBuyerAndSeller(List<Trade> trades, Long buyerID, Long sellerId) {
        for (Trade trade : trades) {
            if (trade.getBuyer() == buyerID && trade.getSeller() == sellerId) {
                return trade;
            }
        }

        return null;
    }
}
