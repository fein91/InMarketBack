package com.fein91.service;

import com.fein91.InMarketApplication;
import com.fein91.builders.OrderRequestBuilder;
import com.fein91.core.model.OrderSide;
import com.fein91.core.model.Trade;
import com.fein91.model.*;
import com.fein91.rest.exception.OrderRequestProcessingException;
import com.fein91.utils.TestUtils;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static java.math.BigDecimal.ROUND_HALF_UP;
import static java.math.BigDecimal.ZERO;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = InMarketApplication.class)
public class LimitOrderBookServiceTest {

    @Autowired
    CounterPartyService counterPartyService;
    @Autowired
    InvoiceService invoiceServiceImpl;
    @Autowired
    OrderRequestService orderRequestServiceImpl;
    
    private final TestUtils testUtils = new TestUtils();

    /*
    *     b1
    * s1 100
    * s2 200
    * s3 50
    * s4 50
    *       BID
    * s4 31 100
    * s3 29 100
    * s2 28 150
    * s1 27 200
    * b1 market order == 350
    * */
    @Test
    @Transactional
    @Rollback
    public void marketAskOrderTest() throws Exception {
        Counterparty buyer = counterPartyService.addCounterParty("buyer");
        Counterparty supplier1 = counterPartyService.addCounterParty("supplier1");
        Counterparty supplier2 = counterPartyService.addCounterParty("supplier2");
        Counterparty supplier3 = counterPartyService.addCounterParty("supplier3");
        Counterparty supplier4 = counterPartyService.addCounterParty("supplier4");

        Invoice invoiceS1B = invoiceServiceImpl.addInvoice(new Invoice(supplier1, buyer, BigDecimal.valueOf(100), ZERO, testUtils.getCurrentDayPlusDays(5)));
        Invoice invoiceS2B = invoiceServiceImpl.addInvoice(new Invoice(supplier2, buyer, BigDecimal.valueOf(200), ZERO, testUtils.getCurrentDayPlusDays(25)));
        Invoice invoiceS3B = invoiceServiceImpl.addInvoice(new Invoice(supplier3, buyer, BigDecimal.valueOf(50), ZERO, testUtils.getCurrentDayPlusDays(15)));
        Invoice invoiceS4B = invoiceServiceImpl.addInvoice(new Invoice(supplier4, buyer, BigDecimal.valueOf(50), ZERO, testUtils.getCurrentDayPlusDays(45)));

        double supplier1AskPrice = 27d;
        OrderRequest bidOrderRequest1 = new OrderRequestBuilder(supplier1)
                .quantity(BigDecimal.valueOf(80))
                .price(BigDecimal.valueOf(supplier1AskPrice))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true, invoiceS2B.getId(), true, invoiceS3B.getId(), true, invoiceS4B.getId(), true))
                .build();
        orderRequestServiceImpl.process(bidOrderRequest1);

        double supplier2AskPrice = 28d;
        OrderRequest bidOrderRequest2 = new OrderRequestBuilder(supplier2)
                .quantity(BigDecimal.valueOf(150))
                .price(BigDecimal.valueOf(supplier2AskPrice))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true, invoiceS2B.getId(), true, invoiceS3B.getId(), true, invoiceS4B.getId(), true))
                .build();
        orderRequestServiceImpl.process(bidOrderRequest2);

        double supplier3AskPrice = 29d;
        OrderRequest bidOrderRequest3 = new OrderRequestBuilder(supplier3)
                .quantity(BigDecimal.valueOf(40))
                .price(BigDecimal.valueOf(supplier3AskPrice))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true, invoiceS2B.getId(), true, invoiceS3B.getId(), true, invoiceS4B.getId(), true))
                .build();
        orderRequestServiceImpl.process(bidOrderRequest3);

        double supplier4AskPrice = 31d;
        OrderRequest bidOrderRequest4 = new OrderRequestBuilder(supplier4)
                .quantity(BigDecimal.valueOf(10))
                .price(BigDecimal.valueOf(supplier4AskPrice))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true, invoiceS2B.getId(), true, invoiceS3B.getId(), true, invoiceS4B.getId(), true))
                .build();
        orderRequestServiceImpl.process(bidOrderRequest4);

        OrderRequest askMarketOrderRequest = new OrderRequestBuilder(buyer)
                .quantity(BigDecimal.valueOf(280))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.MARKET)
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true, invoiceS2B.getId(), true, invoiceS3B.getId(), true, invoiceS4B.getId(), true))
                .build();
        OrderResult result = orderRequestServiceImpl.process(askMarketOrderRequest);

        Trade trade1 = testUtils.findTradeByBuyerAndSeller(result.getTape(), supplier1.getId(), buyer.getId());
        Assert.assertNotNull(trade1);
        Assert.assertEquals(trade1.getPrice(), supplier1AskPrice, 0d);
        Assert.assertEquals(0, BigDecimal.valueOf(80).compareTo(trade1.getQty()));

        Trade trade2 = testUtils.findTradeByBuyerAndSeller(result.getTape(), supplier2.getId(), buyer.getId());
        Assert.assertNotNull(trade2);
        Assert.assertEquals(trade2.getPrice(), supplier2AskPrice, 0d);
        Assert.assertEquals(0, BigDecimal.valueOf(150).compareTo(trade2.getQty()));

        Trade trade3 = testUtils.findTradeByBuyerAndSeller(result.getTape(), supplier3.getId(), buyer.getId());
        Assert.assertNotNull(trade3);
        Assert.assertEquals(trade3.getPrice(), supplier3AskPrice, 0d);
        Assert.assertEquals(0, BigDecimal.valueOf(40).compareTo(trade3.getQty()));

        Trade trade4 = testUtils.findTradeByBuyerAndSeller(result.getTape(), supplier4.getId(), buyer.getId());
        Assert.assertNotNull(trade4);
        Assert.assertEquals(trade4.getPrice(), supplier4AskPrice, 0d);
        Assert.assertEquals(0, BigDecimal.valueOf(10).compareTo(trade4.getQty()));

        Assert.assertEquals(BigDecimal.valueOf(280).compareTo(result.getSatisfiedDemand()), 0);
    }

    /*
    *     b1
    * s1 50000
    * s1 50000
    * s2 200000
    *          BID
    * s1 20000 22
    * s2 10000 20
    * b1 market order == 30000
    * test case when order was removed but there are some invoices to process left and qty remaining
    **/
    @Test
    @Transactional
    @Rollback
    public void testTc1() {
        Counterparty buyer = counterPartyService.addCounterParty("buyer");
        Counterparty supplier1 = counterPartyService.addCounterParty("supplier1");
        Counterparty supplier2 = counterPartyService.addCounterParty("supplier2");

        Invoice invoice1S1B = invoiceServiceImpl.addInvoice(new Invoice(supplier1, buyer, BigDecimal.valueOf(50000), ZERO, testUtils.getCurrentDayPlusDays(5)));
        Invoice invoice2S1B = invoiceServiceImpl.addInvoice(new Invoice(supplier1, buyer, BigDecimal.valueOf(50000), ZERO, testUtils.getCurrentDayPlusDays(25)));
        Invoice invoiceS2B = invoiceServiceImpl.addInvoice(new Invoice(supplier2, buyer, BigDecimal.valueOf(200000), ZERO, testUtils.getCurrentDayPlusDays(15)));

        double supplier1AskPrice = 22d;
        OrderRequest bidOrderRequest1 = new OrderRequestBuilder(supplier1)
                .quantity(BigDecimal.valueOf(20000))
                .price(BigDecimal.valueOf(supplier1AskPrice))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .invoicesChecked(ImmutableMap.of(invoice1S1B.getId(), true, invoice2S1B.getId(), true, invoiceS2B.getId(), true))
                .build();
        orderRequestServiceImpl.process(bidOrderRequest1);

        double supplier2AskPrice = 20d;
        OrderRequest bidOrderRequest2 = new OrderRequestBuilder(supplier2)
                .quantity(BigDecimal.valueOf(10000))
                .price(BigDecimal.valueOf(supplier2AskPrice))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .invoicesChecked(ImmutableMap.of(invoice1S1B.getId(), true, invoice2S1B.getId(), true, invoiceS2B.getId(), true))
                .build();
        orderRequestServiceImpl.process(bidOrderRequest2);

        OrderRequest askMarketOrderRequest = new OrderRequestBuilder(buyer)
                .quantity(BigDecimal.valueOf(30000))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.MARKET)
                .invoicesChecked(ImmutableMap.of(invoice1S1B.getId(), true, invoice2S1B.getId(), true, invoiceS2B.getId(), true))
                .build();
        OrderResult result = orderRequestServiceImpl.process(askMarketOrderRequest);

        Assert.assertEquals("Actual discounts sum: " + result.getDiscountSum(),
                0, BigDecimal.valueOf(142.47).compareTo(result.getDiscountSum()));
        Assert.assertEquals("Actual avg days to payment: " + result.getAvgDaysToPayment(),
                0, BigDecimal.valueOf(8.33).compareTo(result.getAvgDaysToPayment()));
        Assert.assertEquals("Actual avg discount percent: " + result.getAvgDiscountPerc(),
                0, BigDecimal.valueOf(0.06).compareTo(result.getAvgDiscountPerc()));
        Assert.assertEquals("Actual apr: " + result.getApr(),
                0, BigDecimal.valueOf(21.33).compareTo(result.getApr()));
        Assert.assertEquals("Actual satisfied Demand: " + result.getSatisfiedDemand(),
                0, BigDecimal.valueOf(30000).compareTo(result.getSatisfiedDemand()));
        Assert.assertEquals("Actual tape size: " + result.getTape().size(),
                2, result.getTape().size());

        Trade tradeSupplier2Buyer = testUtils.findTradeByBuyerAndSeller(result.getTape(), supplier2.getId(), buyer.getId());
        Assert.assertNotNull(tradeSupplier2Buyer);
        Assert.assertEquals(20d, tradeSupplier2Buyer.getPrice(), 0d);
        Assert.assertEquals("Actual trade qty: " + tradeSupplier2Buyer.getQty(),
                BigDecimal.valueOf(10000).compareTo(tradeSupplier2Buyer.getQty()), 0);

        Trade tradeSupplier1Buyer = testUtils.findTradeByBuyerAndSeller(result.getTape(), supplier1.getId(), buyer.getId());
        Assert.assertNotNull(tradeSupplier1Buyer);
        Assert.assertEquals(22d, tradeSupplier1Buyer.getPrice(), 0d);
        Assert.assertEquals("Actual trade qty: " + tradeSupplier1Buyer.getQty(),
                BigDecimal.valueOf(20000).compareTo(tradeSupplier1Buyer.getQty()), 0);

        invoice1S1B = invoiceServiceImpl.getById(invoice1S1B.getId());
        Assert.assertEquals("Actual invoice prepaid value: " + invoice1S1B.getPrepaidValue(),
                0, BigDecimal.valueOf(20060.27).compareTo(invoice1S1B.getPrepaidValue().setScale(2, ROUND_HALF_UP)));
        invoice2S1B = invoiceServiceImpl.getById(invoice2S1B.getId());
        Assert.assertEquals("Actual invoice prepaid value: " + invoice2S1B.getPrepaidValue(),
                BigDecimal.ZERO.compareTo(invoice2S1B.getPrepaidValue()), 0);

        invoiceS2B = invoiceServiceImpl.getById(invoiceS2B.getId());
        Assert.assertEquals("Actual invoice prepaid value: " + invoiceS2B.getPrepaidValue(),
                BigDecimal.valueOf(10082.19).compareTo(invoiceS2B.getPrepaidValue().setScale(2, ROUND_HALF_UP)), 0);

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
    public void marketBidOrderTest() throws Exception {
        Counterparty supplier = counterPartyService.addCounterParty("supplier");
        Counterparty buyer1 = counterPartyService.addCounterParty("buyer1");
        Counterparty buyer2 = counterPartyService.addCounterParty("buyer2");
        Counterparty buyer3 = counterPartyService.addCounterParty("buyer3");

        Invoice invoiceSB1 = invoiceServiceImpl.addInvoice(new Invoice(supplier, buyer1, BigDecimal.valueOf(100), ZERO, testUtils.getCurrentDayPlusDays(55)));
        Invoice invoiceSB2 = invoiceServiceImpl.addInvoice(new Invoice(supplier, buyer2, BigDecimal.valueOf(100), ZERO, testUtils.getCurrentDayPlusDays(75)));
        Invoice invoiceSB3 = invoiceServiceImpl.addInvoice(new Invoice(supplier, buyer3, BigDecimal.valueOf(800), ZERO, testUtils.getCurrentDayPlusDays(95)));

        double buyer1AskPrice = 29d;
        OrderRequest askOrderRequest1 = new OrderRequestBuilder(buyer1)
                .quantity(BigDecimal.valueOf(90))
                .price(BigDecimal.valueOf(buyer1AskPrice))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.LIMIT)
                .invoicesChecked(ImmutableMap.of(invoiceSB1.getId(), true, invoiceSB2.getId(), true, invoiceSB3.getId(), true))
                .build();
        orderRequestServiceImpl.process(askOrderRequest1);

        double buyer2AskPrice = 30d;
        OrderRequest askOrderRequest2 = new OrderRequestBuilder(buyer2)
                .quantity(BigDecimal.valueOf(80))
                .price(BigDecimal.valueOf(buyer2AskPrice))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.LIMIT)
                .invoicesChecked(ImmutableMap.of(invoiceSB1.getId(), true, invoiceSB2.getId(), true, invoiceSB3.getId(), true))
                .build();
        orderRequestServiceImpl.process(askOrderRequest2);

        double buyer3AskPrice = 31d;
        OrderRequest askOrderRequest3 = new OrderRequestBuilder(buyer3)
                .quantity(BigDecimal.valueOf(500))
                .price(BigDecimal.valueOf(buyer3AskPrice))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.LIMIT)
                .invoicesChecked(ImmutableMap.of(invoiceSB1.getId(), true, invoiceSB2.getId(), true, invoiceSB3.getId(), true))
                .build();
        orderRequestServiceImpl.process(askOrderRequest3);

        OrderRequest bidMarketOrderRequest = new OrderRequestBuilder(supplier)
                .quantity(BigDecimal.valueOf(450))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.MARKET)
                .invoicesChecked(ImmutableMap.of(invoiceSB1.getId(), true, invoiceSB2.getId(), true, invoiceSB3.getId(), true))
                .build();
        OrderResult result = orderRequestServiceImpl.process(bidMarketOrderRequest);

        Trade trade1 = testUtils.findTradeByBuyerAndSeller(result.getTape(), supplier.getId(), buyer1.getId());
        Assert.assertNotNull(trade1);
        Assert.assertEquals(trade1.getPrice(), buyer1AskPrice, 0d);
        Assert.assertEquals(0, BigDecimal.valueOf(90).compareTo(trade1.getQty()));

        Trade trade2 = testUtils.findTradeByBuyerAndSeller(result.getTape(), supplier.getId(), buyer2.getId());
        Assert.assertNotNull(trade2);
        Assert.assertEquals(trade2.getPrice(), buyer2AskPrice, 0d);
        Assert.assertEquals(0, BigDecimal.valueOf(80).compareTo(trade2.getQty()));

        Trade trade3 = testUtils.findTradeByBuyerAndSeller(result.getTape(), supplier.getId(), buyer3.getId());
        Assert.assertNotNull(trade3);
        Assert.assertEquals(trade3.getPrice(), buyer3AskPrice, 0d);
        Assert.assertEquals("Actual trade qty: " + trade3.getQty(),
                0, BigDecimal.valueOf(280).compareTo(trade3.getQty()));

        Assert.assertEquals(BigDecimal.valueOf(450).compareTo(result.getSatisfiedDemand()), 0);
        Assert.assertEquals("Actual apr: " + result.getApr(),
                0, BigDecimal.valueOf(30.42).compareTo(result.getApr()));
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
    public void marketOrderTestWithBoundaryOrderQuantity() throws Exception {
        Counterparty buyer = counterPartyService.addCounterParty("buyer");
        Counterparty supplier1 = counterPartyService.addCounterParty("supplier1");
        Counterparty supplier3 = counterPartyService.addCounterParty("supplier3");

        Invoice invoiceS1B = invoiceServiceImpl.addInvoice(new Invoice(supplier1, buyer, BigDecimal.valueOf(550), ZERO, testUtils.getCurrentDayPlusDays(15)));
        Invoice invoiceS3B = invoiceServiceImpl.addInvoice(new Invoice(supplier3, buyer, BigDecimal.valueOf(200), ZERO, testUtils.getCurrentDayPlusDays(55)));

        double supplier1AskPrice = 28d;
        OrderRequest bidOrderRequest1 = new OrderRequestBuilder(supplier1)
                .quantity(BigDecimal.valueOf(543.67))
                .price(BigDecimal.valueOf(supplier1AskPrice))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true, invoiceS3B.getId(), true))
                .build();
        orderRequestServiceImpl.process(bidOrderRequest1);

        double supplier3AskPrice = 26d;
        OrderRequest bidOrderRequest2 = new OrderRequestBuilder(supplier3)
                .quantity(BigDecimal.valueOf(192.16))
                .price(BigDecimal.valueOf(supplier3AskPrice))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true, invoiceS3B.getId(), true))
                .build();
        orderRequestServiceImpl.process(bidOrderRequest2);

        OrderRequest askMarketOrderRequest = new OrderRequestBuilder(buyer)
                .quantity(BigDecimal.valueOf(250))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.MARKET)
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true, invoiceS3B.getId(), true))
                .build();
        OrderResult result = orderRequestServiceImpl.process(askMarketOrderRequest);

        Assert.assertEquals("Actual discounts sum: " + result.getDiscountSum(),
                0, BigDecimal.valueOf(2.88).compareTo(result.getDiscountSum()));
        Assert.assertEquals("Actual avg days to payment: " + result.getAvgDaysToPayment(),
                0, BigDecimal.valueOf(15).compareTo(result.getAvgDaysToPayment()));
        Assert.assertEquals("Actual avg discount percent: " + result.getAvgDiscountPerc(),
                0, BigDecimal.valueOf(0.52).compareTo(result.getAvgDiscountPerc()));
        Assert.assertEquals("Actual apr: " + result.getApr(),
                0, BigDecimal.valueOf(28).compareTo(result.getApr()));
        Assert.assertEquals("Actual satisfied Demand: " + result.getSatisfiedDemand(),
                0, BigDecimal.valueOf(250).compareTo(result.getSatisfiedDemand()));
        Assert.assertEquals("Actual tape size: " + result.getTape().size(),
                1, result.getTape().size());

        Trade trade1 = testUtils.findTradeByBuyerAndSeller(result.getTape(), supplier1.getId(), buyer.getId());
        Assert.assertNotNull(trade1);
        Assert.assertEquals(trade1.getPrice(), supplier1AskPrice, 0d);
        Assert.assertEquals(trade1.getQty(), BigDecimal.valueOf(250));

        Assert.assertEquals(BigDecimal.valueOf(250).compareTo(result.getSatisfiedDemand()), 0);
        Assert.assertEquals(0, BigDecimal.valueOf(28).compareTo(result.getApr()));
    }

    @Rule
    public ExpectedException marketOrderException = ExpectedException.none();

    @Test
    @Transactional
    @Rollback
    public void marketOrderTest() throws Exception {
        Counterparty buyer = counterPartyService.addCounterParty("buyer");
        Counterparty supplier1 = counterPartyService.addCounterParty("supplier1");
        Counterparty supplier3 = counterPartyService.addCounterParty("supplier3");

        Invoice invoiceS1B = invoiceServiceImpl.addInvoice(new Invoice(supplier1, buyer, BigDecimal.valueOf(550), ZERO, testUtils.getCurrentDayPlusDays(60)));
        Invoice invoiceS3B = invoiceServiceImpl.addInvoice(new Invoice(supplier3, buyer, BigDecimal.valueOf(200), ZERO, testUtils.getCurrentDayPlusDays(55)));

        OrderRequest askLimitOrderRequest = new OrderRequestBuilder(buyer)
                .quantity(BigDecimal.valueOf(700))
                .price(BigDecimal.valueOf(16))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.LIMIT)
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true, invoiceS3B.getId(), true))
                .build();
        orderRequestServiceImpl.process(askLimitOrderRequest);

        marketOrderException.expect(OrderRequestProcessingException.class);
        marketOrderException.expectMessage("Requested order quantity: 545.00 is greater than available quantity = invoices - discounts: 535.90");

        OrderRequest bidMarketOrderRequest = new OrderRequestBuilder(supplier1)
                .quantity(BigDecimal.valueOf(545))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.MARKET)
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true, invoiceS3B.getId(), true))
                .build();
        orderRequestServiceImpl.process(bidMarketOrderRequest);

    }

    @Rule
    public ExpectedException limitOrderCanBeExecutedAsMarket = ExpectedException.none();
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
    public void limitOrderCanBeExecutedAsMarketThrownTest() throws Exception {
        Counterparty buyer1 = counterPartyService.addCounterParty("buyer1");
        Counterparty buyer2 = counterPartyService.addCounterParty("buyer2");
        Counterparty supplier = counterPartyService.addCounterParty("supplier");

        Invoice invoiceSB1 = invoiceServiceImpl.addInvoice(new Invoice(supplier, buyer1, BigDecimal.valueOf(150), ZERO, testUtils.getCurrentDayPlusDays(30)));
        Invoice invoiceSB2 = invoiceServiceImpl.addInvoice(new Invoice(supplier, buyer2, BigDecimal.valueOf(200), ZERO, testUtils.getCurrentDayPlusDays(50)));

        OrderRequest askOrderRequest1 = new OrderRequestBuilder(buyer1)
                .quantity(BigDecimal.valueOf(100))
                .price(BigDecimal.valueOf(30d))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.LIMIT)
                .invoicesChecked(ImmutableMap.of(invoiceSB1.getId(), true, invoiceSB2.getId(), true))
                .build();
        orderRequestServiceImpl.process(askOrderRequest1);

        OrderRequest askOrderRequest2 = new OrderRequestBuilder(buyer2)
                .quantity(BigDecimal.valueOf(150))
                .price(BigDecimal.valueOf(28d))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.LIMIT)
                .invoicesChecked(ImmutableMap.of(invoiceSB1.getId(), true, invoiceSB2.getId(), true))
                .build();
        orderRequestServiceImpl.process(askOrderRequest2);

        limitOrderCanBeExecutedAsMarket.expect(OrderRequestProcessingException.class);
        limitOrderCanBeExecutedAsMarket.expectMessage("Requested APR is higher than available on market. You can process market order on sum=150.00 and APR=28.00.");

        BigDecimal supplier1BidPrice = BigDecimal.valueOf(29);
        OrderRequest bidOrderRequest = new OrderRequestBuilder(supplier)
                .quantity(BigDecimal.valueOf(200))
                .price(supplier1BidPrice)
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .invoicesChecked(ImmutableMap.of(invoiceSB1.getId(), true, invoiceSB2.getId(), true))
                .build();
        orderRequestServiceImpl.calculate(bidOrderRequest);
    }

    @Rule
    public ExpectedException limitOrderProcessingException = ExpectedException.none();
    /*
    *   source  target  value paymentDate
    *     s1      b1     100    0
    *       BID
    * s1 27 90
    * b1 ask market order == 150
    * expected OrderRequestProcessingException, because order request quantity can't be greater than invoices sum
    * */
    @Test
    @Transactional
    @Rollback
    public void limitOrderTestExceptionIsThrownIfInvoicesSumLessThanOrders() throws Exception {
        Counterparty buyer = counterPartyService.addCounterParty("buyer");
        Counterparty supplier = counterPartyService.addCounterParty("supplier");

        Invoice invoice1SB = invoiceServiceImpl.addInvoice(new Invoice(supplier, buyer, BigDecimal.valueOf(100), ZERO, testUtils.getCurrentDayPlusDays(15)));
        Invoice invoice2SB = invoiceServiceImpl.addInvoice(new Invoice(supplier, buyer, BigDecimal.valueOf(100), ZERO, testUtils.getCurrentDayPlusDays(5)));
        Invoice invoice3SB = invoiceServiceImpl.addInvoice(new Invoice(supplier, buyer, BigDecimal.valueOf(100), ZERO, testUtils.getCurrentDayPlusDays(5)));

        OrderRequest bidOrderRequest1 = new OrderRequestBuilder(supplier)
                .quantity(BigDecimal.valueOf(90))
                .price(BigDecimal.valueOf(27d))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .invoicesChecked(ImmutableMap.of(invoice1SB.getId(), true, invoice2SB.getId(), true, invoice3SB.getId(), false))
                .build();
        orderRequestServiceImpl.process(bidOrderRequest1);

        limitOrderProcessingException.expect(OrderRequestProcessingException.class);
        limitOrderProcessingException.expectMessage("Requested order quantity: 250.00 is greater than available quantity = invoices - discounts: 198.37");

        OrderRequest askMarketOrderRequest = new OrderRequestBuilder(buyer)
                .quantity(BigDecimal.valueOf(250))
                .price(BigDecimal.valueOf(30d))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.LIMIT)
                .invoicesChecked(ImmutableMap.of(invoice1SB.getId(), true, invoice2SB.getId(), true, invoice3SB.getId(), false))
                .build();
        orderRequestServiceImpl.process(askMarketOrderRequest);
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

        Invoice invoiceS1B = invoiceServiceImpl.addInvoice(new Invoice(supplier1, buyer, BigDecimal.valueOf(200), ZERO, testUtils.getCurrentDayPlusDays(60)));
        Invoice invoiceS2B = invoiceServiceImpl.addInvoice(new Invoice(supplier2, buyer, BigDecimal.valueOf(250), ZERO, testUtils.getCurrentDayPlusDays(90)));

        OrderRequest bidOrderRequest1 = new OrderRequestBuilder(supplier1)
                .quantity(BigDecimal.valueOf(150))
                .price(BigDecimal.valueOf(15))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true, invoiceS2B.getId(), true))
                .build();
        orderRequestServiceImpl.process(bidOrderRequest1);

        OrderRequest bidOrderRequest2 = new OrderRequestBuilder(supplier2)
                .quantity(BigDecimal.valueOf(200))
                .price(BigDecimal.valueOf(14))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true, invoiceS2B.getId(), true))
                .build();
        orderRequestServiceImpl.process(bidOrderRequest2);

        OrderRequest marketOrderRequest1 = new OrderRequestBuilder(buyer)
                .quantity(BigDecimal.valueOf(100))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.MARKET)
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true, invoiceS2B.getId(), true))
                .build();
        OrderResult result = orderRequestServiceImpl.process(marketOrderRequest1);

        Trade trade1 = testUtils.findTradeByBuyerAndSeller(result.getTape(), supplier1.getId(), buyer.getId());
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

        Trade trade2 = testUtils.findTradeByBuyerAndSeller(result.getTape(), supplier1.getId(), buyer.getId());
        Assert.assertNotNull(trade2);
        Assert.assertEquals(0, BigDecimal.valueOf(50).compareTo(trade2.getQty()));
        Assert.assertEquals(15d, trade2.getPrice(), 0d);

        Trade trade3 = testUtils.findTradeByBuyerAndSeller(result.getTape(), supplier2.getId(), buyer.getId());
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
    @Test
    @Transactional
    @Rollback
    public void testOrderWithSomeUncheckedInvoices() throws Exception {
        Counterparty buyer = counterPartyService.addCounterParty("buyer");
        Counterparty supplier1 = counterPartyService.addCounterParty("supplier1");
        Counterparty supplier2 = counterPartyService.addCounterParty("supplier2");

        Invoice invoiceS1B = invoiceServiceImpl.addInvoice(new Invoice(supplier1, buyer, BigDecimal.valueOf(200), ZERO, testUtils.getCurrentDayPlusDays(60)));
        Invoice invoice1S2B = invoiceServiceImpl.addInvoice(new Invoice(supplier2, buyer, BigDecimal.valueOf(300), ZERO, testUtils.getCurrentDayPlusDays(15)));
        Invoice invoice2S2B = invoiceServiceImpl.addInvoice(new Invoice(supplier2, buyer, BigDecimal.valueOf(300), ZERO, testUtils.getCurrentDayPlusDays(15)));

        OrderRequest bidOrderRequest1 = new OrderRequestBuilder(supplier1)
                .quantity(BigDecimal.valueOf(100))
                .price(BigDecimal.valueOf(26))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true, invoice1S2B.getId(), true, invoice2S2B.getId(), false))
                .build();
        orderRequestServiceImpl.process(bidOrderRequest1);

        OrderRequest bidOrderRequest2 = new OrderRequestBuilder(supplier2)
                .quantity(BigDecimal.valueOf(250))
                .price(BigDecimal.valueOf(25))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true, invoice1S2B.getId(), true, invoice2S2B.getId(), false))
                .build();
        orderRequestServiceImpl.process(bidOrderRequest2);

        OrderRequest marketOrderRequest1 = new OrderRequestBuilder(buyer)
                .quantity(BigDecimal.valueOf(350))
                .orderSide(OrderSide.ASK)
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true, invoice1S2B.getId(), true, invoice2S2B.getId(), false))
                .orderType(OrderType.MARKET)
                .build();

        OrderResult result = orderRequestServiceImpl.process(marketOrderRequest1);
        Trade trade1 = testUtils.findTradeByBuyerAndSeller(result.getTape(), supplier1.getId(), buyer.getId());
        Assert.assertNotNull(trade1);
        Assert.assertEquals(26d, trade1.getPrice(), 0d);
        Assert.assertEquals("Actual trade qty: " + trade1.getQty(),
                0, BigDecimal.valueOf(100).compareTo(trade1.getQty()));

        Trade trade2 = testUtils.findTradeByBuyerAndSeller(result.getTape(), supplier2.getId(), buyer.getId());
        Assert.assertNotNull(trade2);
        Assert.assertEquals(25d, trade2.getPrice(), 0d);
        Assert.assertEquals("Actual trade qty: " + trade2.getQty(),
                0, BigDecimal.valueOf(250).compareTo(trade2.getQty()));

        Assert.assertEquals("Actual apr " + result.getApr(), BigDecimal.valueOf(25.29).compareTo(result.getApr()), 0);
    }

    @Rule
    public ExpectedException marketOrderProcessingException = ExpectedException.none();

    @Test
    @Transactional
    @Rollback
    public void testExceptionThrownAfterMarketOrderBiggerThanOrdersSum() throws Exception {
        Counterparty buyer = counterPartyService.addCounterParty("buyer");
        Counterparty supplier = counterPartyService.addCounterParty("supplier");

        Invoice invoice1SB = invoiceServiceImpl.addInvoice(new Invoice(supplier, buyer, BigDecimal.valueOf(100), ZERO, testUtils.getCurrentDayPlusDays(30)));
        Invoice invoice2SB = invoiceServiceImpl.addInvoice(new Invoice(supplier, buyer, BigDecimal.valueOf(100), ZERO, testUtils.getCurrentDayPlusDays(50)));

        OrderRequest bidOrderRequest1 = new OrderRequestBuilder(buyer)
                .quantity(BigDecimal.valueOf(150))
                .price(BigDecimal.valueOf(27d))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.LIMIT)
                .invoicesChecked(ImmutableMap.of(invoice1SB.getId(), true, invoice2SB.getId(), true))
                .build();
        orderRequestServiceImpl.process(bidOrderRequest1);

        marketOrderProcessingException.expect(OrderRequestProcessingException.class);
        marketOrderProcessingException.expectMessage("Requested order quantity: 200.00 cannot be satisfied. Available order quantity: 150.00. Please process unsatisfied quantity: 50.00 as limit order");

        OrderRequest askOrderRequest = new OrderRequestBuilder(supplier)
                .quantity(BigDecimal.valueOf(200))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.MARKET)
                .invoicesChecked(ImmutableMap.of(invoice1SB.getId(), true, invoice2SB.getId(), true))
                .build();
        orderRequestServiceImpl.process(askOrderRequest);

    }

    @Test
    @Transactional
    @Rollback
    public void testWithDiscountsOneInvoicePerBuyer() throws Exception {
        Counterparty buyer = counterPartyService.addCounterParty("buyer");
        Counterparty supplier1 = counterPartyService.addCounterParty("supplier1");
        Counterparty supplier2 = counterPartyService.addCounterParty("supplier2");

        Invoice invoiceS1B = invoiceServiceImpl.addInvoice(new Invoice(supplier1, buyer, BigDecimal.valueOf(400), ZERO, testUtils.getCurrentDayPlusDays(50)));
        Invoice invoiceS2B = invoiceServiceImpl.addInvoice(new Invoice(supplier2, buyer, BigDecimal.valueOf(300), ZERO, testUtils.getCurrentDayPlusDays(60)));

        BigDecimal bid1Price = BigDecimal.valueOf(26);
        OrderRequest bidOrderRequest1 = new OrderRequestBuilder(supplier1)
                .quantity(BigDecimal.valueOf(385))
                .price(bid1Price)
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true, invoiceS2B.getId(), true))
                .build();
        orderRequestServiceImpl.process(bidOrderRequest1);

        OrderRequest bidOrderRequest2 = new OrderRequestBuilder(supplier2)
                .quantity(BigDecimal.valueOf(200))
                .price(BigDecimal.valueOf(25))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true, invoiceS2B.getId(), true))
                .build();
        orderRequestServiceImpl.process(bidOrderRequest2);

        OrderRequest marketOrderRequest1 = new OrderRequestBuilder(buyer)
                .quantity(BigDecimal.valueOf(350))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.MARKET)
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true, invoiceS2B.getId(), true))
                .build();

        OrderResult result = orderRequestServiceImpl.process(marketOrderRequest1);
        Trade trade = testUtils.findTradeByBuyerAndSeller(result.getTape(), supplier1.getId(), buyer.getId());
        Assert.assertNotNull(trade);
        Assert.assertEquals(bid1Price.doubleValue(), trade.getPrice(), 0d);
        Assert.assertEquals(BigDecimal.valueOf(350), trade.getQty());
    }

    /*
    *   source  target  value paymentDate
    *     s1      b1     200    40
    *     s1      b1     200    50
    *     s2      b1     300    40
    *       BID
    * s2 27 295
    * s1 26 380
    * b1 ask market order == 350
    * */
    @Test
    @Transactional
    @Rollback
    public void testWithDiscountsTwoInvoicesPerBuyer() throws Exception {
        Counterparty buyer = counterPartyService.addCounterParty("buyer");
        Counterparty supplier1 = counterPartyService.addCounterParty("supplier1");
        Counterparty supplier2 = counterPartyService.addCounterParty("supplier2");

        Invoice invoice1S1B = invoiceServiceImpl.addInvoice(new Invoice(supplier1, buyer, BigDecimal.valueOf(200), ZERO, testUtils.getCurrentDayPlusDays(40)));
        Invoice invoice2S1B = invoiceServiceImpl.addInvoice(new Invoice(supplier1, buyer, BigDecimal.valueOf(200), ZERO, testUtils.getCurrentDayPlusDays(50)));
        Invoice invoiceS2B = invoiceServiceImpl.addInvoice(new Invoice(supplier2, buyer, BigDecimal.valueOf(300), ZERO, testUtils.getCurrentDayPlusDays(40)));

        ImmutableMap<Long, Boolean> invoicesChecked = ImmutableMap.of(invoice1S1B.getId(), true, invoice2S1B.getId(), true, invoiceS2B.getId(), true);

        BigDecimal bid1Price = BigDecimal.valueOf(26);
        OrderRequest bidOrderRequest1 = new OrderRequestBuilder(supplier1)
                .quantity(BigDecimal.valueOf(380))
                .price(bid1Price)
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .invoicesChecked(invoicesChecked)
                .build();
        orderRequestServiceImpl.process(bidOrderRequest1);

        OrderRequest bidOrderRequest2 = new OrderRequestBuilder(supplier2)
                .quantity(BigDecimal.valueOf(291))
                .price(BigDecimal.valueOf(27))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .invoicesChecked(invoicesChecked)
                .build();
        orderRequestServiceImpl.process(bidOrderRequest2);

        OrderRequest marketOrderRequest1 = new OrderRequestBuilder(buyer)
                .quantity(BigDecimal.valueOf(350))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.MARKET)
                .invoicesChecked(invoicesChecked)
                .build();

        OrderResult result = orderRequestServiceImpl.process(marketOrderRequest1);

        Trade tradeSupplier2Buyer = testUtils.findTradeByBuyerAndSeller(result.getTape(), supplier2.getId(), buyer.getId());
        Assert.assertNotNull(tradeSupplier2Buyer);
        Assert.assertEquals(27d, tradeSupplier2Buyer.getPrice(), 0d);
        Assert.assertEquals("Actual trade qty: " + tradeSupplier2Buyer.getQty(),
                BigDecimal.valueOf(291).compareTo(tradeSupplier2Buyer.getQty()), 0);

        Trade tradeSupplier1Buyer = testUtils.findTradeByBuyerAndSeller(result.getTape(), supplier1.getId(), buyer.getId());
        Assert.assertNotNull(tradeSupplier1Buyer);
        Assert.assertEquals(26d, tradeSupplier1Buyer.getPrice(), 0d);
        Assert.assertEquals("Actual trade qty: " + tradeSupplier1Buyer.getQty(),
                BigDecimal.valueOf(59).compareTo(tradeSupplier1Buyer.getQty()), 0);

        invoice1S1B = invoiceServiceImpl.getById(invoice1S1B.getId());
        Assert.assertEquals("Actual invoice prepaid value: " + invoice1S1B.getPrepaidValue(),
                0, BigDecimal.valueOf(60.68).compareTo(invoice1S1B.getPrepaidValue().setScale(2, ROUND_HALF_UP)));
        invoice2S1B = invoiceServiceImpl.getById(invoice2S1B.getId());
        Assert.assertEquals("Actual invoice prepaid value: " + invoice2S1B.getPrepaidValue(),
                BigDecimal.ZERO.compareTo(invoice2S1B.getPrepaidValue()), 0);

        invoiceS2B = invoiceServiceImpl.getById(invoiceS2B.getId());
        Assert.assertEquals("Actual invoice prepaid value: " + invoiceS2B.getPrepaidValue(),
                BigDecimal.valueOf(299.61).compareTo(invoiceS2B.getPrepaidValue().setScale(2, ROUND_HALF_UP)), 0);

    }
}
