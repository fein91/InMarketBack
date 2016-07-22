package com.fein91.service;

import com.fein91.InMarketApplication;
import com.fein91.builders.OrderRequestBuilder;
import com.fein91.core.model.OrderSide;
import com.fein91.model.*;
import com.fein91.rest.exception.OrderRequestException;
import com.fein91.utils.TestUtils;
import com.google.common.collect.ImmutableMap;
import junit.framework.Assert;
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
public class TransactionHistoryTest {

    @Autowired
    @Qualifier("OrderRequestServiceImpl")
    private OrderRequestService orderRequestService;
    @Autowired
    private CounterPartyService counterPartyService;
    @Autowired
    @Qualifier("InvoiceServiceImpl")
    private InvoiceService invoiceService;
    @Autowired
    @Qualifier("HistoryOrderRequestServiceImpl")
    private HistoryOrderRequestService historyOrderRequestService;

    private final TestUtils testUtils = new TestUtils();


    @Test
    @Transactional
    @Rollback
    public void test() throws Exception {
        Counterparty buyer = counterPartyService.addCounterParty("buyer");
        Counterparty supplier1 = counterPartyService.addCounterParty("supplier1");
        Counterparty supplier2 = counterPartyService.addCounterParty("supplier2");
        Counterparty supplier3 = counterPartyService.addCounterParty("supplier3");
        Counterparty supplier4 = counterPartyService.addCounterParty("supplier4");

        Invoice invoiceS1B = invoiceService.addInvoice(new Invoice(supplier1, buyer, BigDecimal.valueOf(100), ZERO, new Date()));
        Invoice invoiceS2B= invoiceService.addInvoice(new Invoice(supplier2, buyer, BigDecimal.valueOf(200), ZERO, new Date()));
        Invoice invoiceS3B = invoiceService.addInvoice(new Invoice(supplier3, buyer, BigDecimal.valueOf(50), ZERO, new Date()));
        Invoice invoiceS4B = invoiceService.addInvoice(new Invoice(supplier4, buyer, BigDecimal.valueOf(50), ZERO, new Date()));

        OrderRequest bidOrderRequest1 = new OrderRequestBuilder(supplier1)
                .quantity(BigDecimal.valueOf(50))
                .price(BigDecimal.valueOf(27d))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestService.process(bidOrderRequest1);

        List<HistoryOrderRequest> supplier1TransHistory = historyOrderRequestService.getByCounterparty(supplier1);

        Assert.assertEquals(1, supplier1TransHistory.size());
        OrderRequest supplier1OriginOrderRequest = orderRequestService.getByCounterpartyId(supplier1.getId()).iterator().next();
        HistoryOrderRequest supplier1HistoryOrderRequest = testUtils.findHistoryOrderRequestByOrderSide(supplier1TransHistory, HistoryOrderType.LIMIT);
        Assert.assertEquals(supplier1OriginOrderRequest.getId(), supplier1HistoryOrderRequest.getOriginOrderRequestId());

        OrderRequest bidOrderRequest2 = new OrderRequestBuilder(supplier2)
                .quantity(BigDecimal.valueOf(150))
                .price(BigDecimal.valueOf(28d))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestService.process(bidOrderRequest2);

        OrderRequest askOrderRequest = new OrderRequestBuilder(buyer)
                .quantity(BigDecimal.valueOf(200))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.MARKET)
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true, invoiceS2B.getId(), true, invoiceS3B.getId(), true, invoiceS4B.getId(), true))
                .build();
        orderRequestService.process(askOrderRequest);

        //check buyer transaction history
        List<HistoryOrderRequest> buyerTransHistory = historyOrderRequestService.getByCounterparty(buyer);

        Assert.assertEquals(1, buyerTransHistory.size());
        HistoryOrderRequest executedOrder = testUtils.findHistoryOrderRequestByOrderSide(buyerTransHistory, HistoryOrderType.MARKET);
        Assert.assertEquals(2, executedOrder.getHistoryTrades().size());
        HistoryTrade supplier1Trade = testUtils.findHistoryTradeByTarget(executedOrder.getHistoryTrades(), supplier1);
        Assert.assertNotNull(supplier1Trade);
        Assert.assertEquals(0, BigDecimal.valueOf(50).compareTo(supplier1Trade.getQuantity()));
        Assert.assertEquals(supplier1HistoryOrderRequest, supplier1Trade.getAffectedOrderRequest());

        HistoryTrade supplier2Trade = testUtils.findHistoryTradeByTarget(executedOrder.getHistoryTrades(), supplier2);
        Assert.assertNotNull(supplier2Trade);
        Assert.assertEquals(0, BigDecimal.valueOf(150).compareTo(supplier2Trade.getQuantity()));

        Assert.assertEquals(0, BigDecimal.valueOf(200).compareTo(executedOrder.getQuantity()));

        //check supplier1 transaction history
        supplier1TransHistory = historyOrderRequestService.getByCounterparty(supplier1);
        Assert.assertEquals(2, supplier1TransHistory.size());
        HistoryOrderRequest supplier1MarketOrder = testUtils.findHistoryOrderRequestByOrderSide(supplier1TransHistory, HistoryOrderType.EXECUTED_LIMIT);
        Assert.assertNotNull(supplier1MarketOrder);
        Assert.assertEquals(OrderSide.BID, supplier1MarketOrder.getOrderSide());
        Assert.assertEquals(0, BigDecimal.valueOf(50).compareTo(supplier1MarketOrder.getQuantity()));

        HistoryTrade supplier1BuyerHistoryTrade = testUtils.findHistoryTradeByTarget(supplier1MarketOrder.getHistoryTrades(), buyer);
        Assert.assertNotNull(supplier1BuyerHistoryTrade);
        Assert.assertEquals(0, BigDecimal.valueOf(50).compareTo(supplier1BuyerHistoryTrade.getQuantity()));

        //check supplier2 transaction history
        List<HistoryOrderRequest> supplier2TransHistory = historyOrderRequestService.getByCounterparty(supplier2);
        Assert.assertEquals(2, supplier2TransHistory.size());
        HistoryOrderRequest supplier2MarketOrder = testUtils.findHistoryOrderRequestByOrderSide(supplier2TransHistory, HistoryOrderType.EXECUTED_LIMIT);
        Assert.assertNotNull(supplier2MarketOrder);
        Assert.assertEquals(OrderSide.BID, supplier2MarketOrder.getOrderSide());
        Assert.assertEquals(0, BigDecimal.valueOf(150).compareTo(supplier2MarketOrder.getQuantity()));

        HistoryTrade supplier2BuyerHistoryTrade = testUtils.findHistoryTradeByTarget(supplier2MarketOrder.getHistoryTrades(), buyer);
        Assert.assertNotNull(supplier2BuyerHistoryTrade);
        Assert.assertEquals(0, BigDecimal.valueOf(150).compareTo(supplier2BuyerHistoryTrade.getQuantity()));

    }

    @Test
    @Transactional
    @Rollback
    public void testMarketAsk() throws OrderRequestException {
        Counterparty buyer = counterPartyService.addCounterParty("buyer");
        Counterparty supplier1 = counterPartyService.addCounterParty("supplier1");
        Counterparty supplier2 = counterPartyService.addCounterParty("supplier2");
        Counterparty supplier3 = counterPartyService.addCounterParty("supplier3");
        Counterparty supplier4 = counterPartyService.addCounterParty("supplier4");

        Invoice invoiceS1B = invoiceService.addInvoice(new Invoice(supplier1, buyer, BigDecimal.valueOf(100), ZERO, new Date()));
        Invoice invoiceS2B= invoiceService.addInvoice(new Invoice(supplier2, buyer, BigDecimal.valueOf(200), ZERO, new Date()));
        Invoice invoiceS3B = invoiceService.addInvoice(new Invoice(supplier3, buyer, BigDecimal.valueOf(50), ZERO, new Date()));
        Invoice invoiceS4B = invoiceService.addInvoice(new Invoice(supplier4, buyer, BigDecimal.valueOf(50), ZERO, new Date()));

        OrderRequest bidOrderRequest1 = new OrderRequestBuilder(supplier1)
                .quantity(BigDecimal.valueOf(50))
                .price(BigDecimal.valueOf(27d))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestService.process(bidOrderRequest1);

        OrderRequest askOrderRequest = new OrderRequestBuilder(buyer)
                .quantity(BigDecimal.valueOf(250))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.MARKET)
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true, invoiceS2B.getId(), true, invoiceS3B.getId(), true, invoiceS4B.getId(), true))
                .build();
        orderRequestService.process(askOrderRequest);

        List<HistoryOrderRequest> supplier1TransHistory = historyOrderRequestService.getByCounterparty(supplier1);
        Assert.assertEquals(2, supplier1TransHistory.size());
        HistoryOrderRequest supplier1LimitOrderRequest = testUtils.findHistoryOrderRequestByOrderSide(supplier1TransHistory, HistoryOrderType.LIMIT);
        Assert.assertEquals(OrderSide.BID, supplier1LimitOrderRequest.getOrderSide());
        Assert.assertEquals(0, BigDecimal.valueOf(50).compareTo(supplier1LimitOrderRequest.getQuantity()));

        HistoryOrderRequest supplier1MarketOrderRequest = testUtils.findHistoryOrderRequestByOrderSide(supplier1TransHistory, HistoryOrderType.EXECUTED_LIMIT);
        Assert.assertEquals(OrderSide.BID, supplier1MarketOrderRequest.getOrderSide());
        Assert.assertEquals(0, BigDecimal.valueOf(50).compareTo(supplier1MarketOrderRequest.getQuantity()));

        List<HistoryOrderRequest> buyerTransHistory = historyOrderRequestService.getByCounterparty(buyer);
        Assert.assertEquals(2, buyerTransHistory.size());
        HistoryOrderRequest buyerMarketOrderRequest = testUtils.findHistoryOrderRequestByOrderSide(buyerTransHistory, HistoryOrderType.MARKET);
        Assert.assertEquals(OrderSide.ASK, buyerMarketOrderRequest.getOrderSide());
        Assert.assertEquals(0, BigDecimal.valueOf(50).compareTo(buyerMarketOrderRequest.getQuantity()));

        HistoryOrderRequest buyerLimitOrderRequest = testUtils.findHistoryOrderRequestByOrderSide(buyerTransHistory, HistoryOrderType.LIMIT);
        Assert.assertEquals(OrderSide.ASK, buyerLimitOrderRequest.getOrderSide());
        Assert.assertEquals(0, BigDecimal.valueOf(200).compareTo(buyerLimitOrderRequest.getQuantity()));
    }

    @Test
    @Transactional
    @Rollback
    public void testMarketBid() throws OrderRequestException {
        Counterparty buyer = counterPartyService.addCounterParty("buyer");
        Counterparty supplier = counterPartyService.addCounterParty("supplier");

        Invoice invoice1SB = invoiceService.addInvoice(new Invoice(supplier, buyer, BigDecimal.valueOf(200), ZERO, testUtils.getCurrentDayPlusDays(30)));

        OrderRequest bidOrderRequest1 = new OrderRequestBuilder(buyer)
                .quantity(BigDecimal.valueOf(100))
                .price(BigDecimal.valueOf(27d))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestService.process(bidOrderRequest1);

        OrderRequest askOrderRequest = new OrderRequestBuilder(supplier)
                .quantity(BigDecimal.valueOf(100))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.MARKET)
                .invoicesChecked(ImmutableMap.of(invoice1SB.getId(), true))
                .build();
        orderRequestService.process(askOrderRequest);

        List<HistoryOrderRequest> supplierTransHistory = historyOrderRequestService.getByCounterparty(supplier);
        Assert.assertEquals(1, supplierTransHistory.size());
        HistoryOrderRequest supplierMarketOrderRequest = testUtils.findHistoryOrderRequestByOrderSide(supplierTransHistory, HistoryOrderType.MARKET);
        Assert.assertEquals(OrderSide.BID, supplierMarketOrderRequest.getOrderSide());
        Assert.assertEquals("Actual qty: " + supplierMarketOrderRequest.getQuantity(),
                0, BigDecimal.valueOf(100).compareTo(supplierMarketOrderRequest.getQuantity()));

        List<HistoryOrderRequest> buyerTransHistory = historyOrderRequestService.getByCounterparty(buyer);
        Assert.assertEquals(2, buyerTransHistory.size());
        HistoryOrderRequest buyerLimitOrderRequest = testUtils.findHistoryOrderRequestByOrderSide(buyerTransHistory, HistoryOrderType.LIMIT);
        Assert.assertEquals(OrderSide.ASK, buyerLimitOrderRequest.getOrderSide());
        Assert.assertEquals("Actual qty: " + buyerLimitOrderRequest.getQuantity(),
                0, BigDecimal.valueOf(100).compareTo(buyerLimitOrderRequest.getQuantity()));

        HistoryOrderRequest buyerMarketOrderRequest = testUtils.findHistoryOrderRequestByOrderSide(buyerTransHistory, HistoryOrderType.EXECUTED_LIMIT);
        Assert.assertEquals(OrderSide.ASK, buyerMarketOrderRequest.getOrderSide());
        Assert.assertEquals("Actual qty: " + buyerMarketOrderRequest.getQuantity(),
                0, BigDecimal.valueOf(100).compareTo(buyerMarketOrderRequest.getQuantity()));

        HistoryTrade buyerSupplierTrade = testUtils.findHistoryTradeByTarget(buyerMarketOrderRequest.getHistoryTrades(), supplier);
        Assert.assertNotNull(buyerSupplierTrade);
        Assert.assertEquals(0, BigDecimal.valueOf(100).compareTo(buyerSupplierTrade.getQuantity()));

    }

}
