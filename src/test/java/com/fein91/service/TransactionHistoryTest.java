package com.fein91.service;

import com.fein91.Constants;
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
import java.util.List;

import static java.math.BigDecimal.ZERO;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = InMarketApplication.class)
public class TransactionHistoryTest {

    @Autowired
    private OrderRequestService orderRequestService;
    @Autowired
    private CounterPartyService counterPartyService;
    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private HistoryOrderRequestService historyOrderRequestService;

    private final TestUtils testUtils = new TestUtils();


    @Test
    @Transactional
    @Rollback
    public void testMarketAsk() throws Exception {
        Counterparty buyer = counterPartyService.addCounterParty("buyer");
        Counterparty supplier1 = counterPartyService.addCounterParty("supplier1");
        Counterparty supplier2 = counterPartyService.addCounterParty("supplier2");

        Invoice invoiceS1B = invoiceService.addInvoice(new Invoice(supplier1, buyer, BigDecimal.valueOf(100), ZERO, testUtils.getCurrentDayPlusDays(20)));
        Invoice invoice1S2B = invoiceService.addInvoice(new Invoice(supplier2, buyer, BigDecimal.valueOf(120), ZERO, testUtils.getCurrentDayPlusDays(30)));
        Invoice invoice2S2B = invoiceService.addInvoice(new Invoice(supplier2, buyer, BigDecimal.valueOf(50), ZERO, testUtils.getCurrentDayPlusDays(40)));

        OrderRequest bidOrderRequest1 = new OrderRequestBuilder(supplier1)
                .quantity(BigDecimal.valueOf(50))
                .price(BigDecimal.valueOf(27d))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true, invoice1S2B.getId(), true, invoice2S2B.getId(), true))
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
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true, invoice1S2B.getId(), true, invoice2S2B.getId(), true))
                .build();
        orderRequestService.process(bidOrderRequest2);

        OrderRequest askOrderRequest = new OrderRequestBuilder(buyer)
                .quantity(BigDecimal.valueOf(200))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.MARKET)
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true, invoice1S2B.getId(), true, invoice2S2B.getId(), true))
                .build();
        orderRequestService.process(askOrderRequest);

        //check buyer transaction history
        List<HistoryOrderRequest> buyerTransHistory = historyOrderRequestService.getByCounterparty(buyer);

        Assert.assertEquals(1, buyerTransHistory.size());
        HistoryOrderRequest executedOrder = testUtils.findHistoryOrderRequestByOrderSide(buyerTransHistory, HistoryOrderType.MARKET);
        Assert.assertEquals(3, executedOrder.getHistoryTrades().size());
        HistoryTrade supplier1Trade = testUtils.findHistoryTradeByTarget(executedOrder.getHistoryTrades(), supplier1);
        Assert.assertNotNull(supplier1Trade);
        Assert.assertEquals(0, BigDecimal.valueOf(50).compareTo(supplier1Trade.getQuantity()));
        Assert.assertEquals(supplier1HistoryOrderRequest, supplier1Trade.getAffectedOrderRequest());

        HistoryTrade supplier2Trade = testUtils.findHistoryTradeByTarget(executedOrder.getHistoryTrades(), supplier2);
        Assert.assertNotNull(supplier2Trade);
//        Assert.assertEquals(0, BigDecimal.valueOf(150).compareTo(supplier2Trade.getQuantity()));

        Assert.assertEquals(0, BigDecimal.valueOf(200).compareTo(executedOrder.getQuantity()));
        Assert.assertEquals("Actual price: " + executedOrder.getPrice(),
                0, BigDecimal.valueOf(27.75).compareTo(executedOrder.getPrice()));
        Assert.assertEquals("Actual avg days to payment: " + executedOrder.getAvgDaysToPayment(),
                0, BigDecimal.valueOf(29.13).compareTo(executedOrder.getAvgDaysToPayment()));

        //check supplier1 transaction history
        supplier1TransHistory = historyOrderRequestService.getByCounterparty(supplier1);
        Assert.assertEquals(2, supplier1TransHistory.size());
        HistoryOrderRequest supplier1MarketOrder = testUtils.findHistoryOrderRequestByOrderSide(supplier1TransHistory, HistoryOrderType.EXECUTED_LIMIT);
        Assert.assertNotNull(supplier1MarketOrder);
        Assert.assertEquals(OrderSide.BID, supplier1MarketOrder.getSide());
        Assert.assertEquals(0, BigDecimal.valueOf(50).compareTo(supplier1MarketOrder.getQuantity()));
        Assert.assertEquals(0, BigDecimal.valueOf(27).compareTo(supplier1MarketOrder.getPrice()));
        Assert.assertEquals("Actual avg days to payment: " + supplier1MarketOrder.getAvgDaysToPayment(),
                0, BigDecimal.valueOf(20).compareTo(supplier1MarketOrder.getAvgDaysToPayment()));

        HistoryTrade supplier1BuyerHistoryTrade = testUtils.findHistoryTradeByTarget(supplier1MarketOrder.getHistoryTrades(), buyer);
        Assert.assertNotNull(supplier1BuyerHistoryTrade);
        Assert.assertEquals(0, BigDecimal.valueOf(50).compareTo(supplier1BuyerHistoryTrade.getQuantity()));
        Assert.assertEquals(0, BigDecimal.valueOf(27).compareTo(supplier1BuyerHistoryTrade.getPrice()));

        //check supplier2 transaction history
        List<HistoryOrderRequest> supplier2TransHistory = historyOrderRequestService.getByCounterparty(supplier2);
        Assert.assertEquals(2, supplier2TransHistory.size());
        HistoryOrderRequest supplier2MarketOrder = testUtils.findHistoryOrderRequestByOrderSide(supplier2TransHistory, HistoryOrderType.EXECUTED_LIMIT);
        Assert.assertNotNull(supplier2MarketOrder);
        Assert.assertEquals(OrderSide.BID, supplier2MarketOrder.getSide());
        Assert.assertEquals(0, BigDecimal.valueOf(150).compareTo(supplier2MarketOrder.getQuantity()));
        Assert.assertEquals("Actual price: " + supplier2MarketOrder.getPrice(),
                0, BigDecimal.valueOf(28).compareTo(supplier2MarketOrder.getPrice()));
        Assert.assertEquals("Actual avg day to payment: " + supplier2MarketOrder.getAvgDaysToPayment(),
                0, BigDecimal.valueOf(32.18).compareTo(supplier2MarketOrder.getAvgDaysToPayment().setScale(Constants.UI_SCALE, Constants.ROUNDING_MODE)));

        HistoryTrade supplier2BuyerHistoryTrade = testUtils.findHistoryTradeByTarget(supplier2MarketOrder.getHistoryTrades(), buyer);
        Assert.assertEquals(2, supplier2MarketOrder.getHistoryTrades().size());
        Assert.assertNotNull(supplier2BuyerHistoryTrade);
//        Assert.assertEquals("Actual qty: " + supplier2BuyerHistoryTrade.getQuantity(),
//                0, BigDecimal.valueOf(150).compareTo(supplier2BuyerHistoryTrade.getQuantity()));

    }

    @Test
    @Transactional
    @Rollback
    public void testMarketBid() throws OrderRequestException {
        Counterparty buyer = counterPartyService.addCounterParty("buyer");
        Counterparty supplier = counterPartyService.addCounterParty("supplier");

        Invoice invoice1SB = invoiceService.addInvoice(new Invoice(supplier, buyer, BigDecimal.valueOf(200), ZERO, testUtils.getCurrentDayPlusDays(30)));
        Invoice invoice2SB = invoiceService.addInvoice(new Invoice(supplier, buyer, BigDecimal.valueOf(100), ZERO, testUtils.getCurrentDayPlusDays(20)));

        BigDecimal bidOrder1Price = BigDecimal.valueOf(27d);
        OrderRequest bidOrderRequest1 = new OrderRequestBuilder(buyer)
                .quantity(BigDecimal.valueOf(100))
                .price(bidOrder1Price)
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.LIMIT)
                .invoicesChecked(ImmutableMap.of(invoice1SB.getId(), true, invoice2SB.getId(), true))
                .build();
        orderRequestService.process(bidOrderRequest1);

        OrderRequest bidOrderRequest2 = new OrderRequestBuilder(buyer)
                .quantity(BigDecimal.valueOf(75))
                .price(BigDecimal.valueOf(26d))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.LIMIT)
                .invoicesChecked(ImmutableMap.of(invoice1SB.getId(), true, invoice2SB.getId(), true))
                .build();
        orderRequestService.process(bidOrderRequest2);

        OrderRequest askOrderRequest = new OrderRequestBuilder(supplier)
                .quantity(BigDecimal.valueOf(150))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.MARKET)
                .invoicesChecked(ImmutableMap.of(invoice1SB.getId(), true, invoice2SB.getId(), true))
                .build();
        orderRequestService.process(askOrderRequest);

        List<HistoryOrderRequest> supplierTransHistory = historyOrderRequestService.getByCounterparty(supplier);
        Assert.assertEquals(1, supplierTransHistory.size());
        HistoryOrderRequest supplierMarketOrderRequest = testUtils.findHistoryOrderRequestByOrderSide(supplierTransHistory, HistoryOrderType.MARKET);
        Assert.assertEquals(OrderSide.BID, supplierMarketOrderRequest.getSide());
        Assert.assertEquals("Actual qty: " + supplierMarketOrderRequest.getQuantity(),
                0, BigDecimal.valueOf(150).compareTo(supplierMarketOrderRequest.getQuantity()));
        Assert.assertEquals("Actual avg days to payment: " + supplierMarketOrderRequest.getAvgDaysToPayment(),
                0, BigDecimal.valueOf(23.43).compareTo(supplierMarketOrderRequest.getAvgDaysToPayment()));
        Assert.assertEquals(3, supplierMarketOrderRequest.getHistoryTrades().size());

        List<HistoryOrderRequest> buyerTransHistory = historyOrderRequestService.getByCounterparty(buyer);
        Assert.assertEquals(4, buyerTransHistory.size());
        HistoryOrderRequest buyerLimitOrderRequest = testUtils.findHistoryOrderRequestByOrderSideAndPrice(buyerTransHistory, HistoryOrderType.LIMIT, bidOrder1Price);
        Assert.assertEquals(OrderSide.ASK, buyerLimitOrderRequest.getSide());
        Assert.assertEquals("Actual qty: " + buyerLimitOrderRequest.getQuantity(),
                0, BigDecimal.valueOf(100).compareTo(buyerLimitOrderRequest.getQuantity()));

        HistoryOrderRequest buyerMarketOrderRequest = testUtils.findHistoryOrderRequestByOrderSideAndPrice(buyerTransHistory, HistoryOrderType.EXECUTED_LIMIT, bidOrder1Price);
        Assert.assertEquals(OrderSide.ASK, buyerMarketOrderRequest.getSide());
        Assert.assertEquals(2, buyerMarketOrderRequest.getHistoryTrades().size());
        Assert.assertEquals("Actual qty: " + buyerMarketOrderRequest.getQuantity(),
                0, BigDecimal.valueOf(75).compareTo(buyerMarketOrderRequest.getQuantity()));

        HistoryTrade buyerSupplierTrade = testUtils.findHistoryTradeByTargetAndInvoiceId(buyerMarketOrderRequest.getHistoryTrades(), supplier, invoice1SB.getId());
        Assert.assertNotNull(buyerSupplierTrade);
        Assert.assertEquals("Actual qty: " + buyerSupplierTrade.getQuantity(),
                0, BigDecimal.valueOf(51.42).compareTo(buyerSupplierTrade.getQuantity().setScale(Constants.UI_SCALE, Constants.ROUNDING_MODE)));

    }
}
