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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

import static java.math.BigDecimal.ZERO;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = InMarketApplication.class)
public class UpdateOrderRequestTest {

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
    public void testSuccessUpdate() throws Exception {
        Counterparty buyer = counterPartyService.addCounterParty("buyer");
        Counterparty supplier1 = counterPartyService.addCounterParty("supplier1");

        Invoice invoiceS1B = invoiceService.addInvoice(new Invoice(supplier1, buyer, BigDecimal.valueOf(100), ZERO, new Date()));

        BigDecimal bidPrice = BigDecimal.valueOf(27d);
        OrderRequest bidOrderRequest1 = new OrderRequestBuilder(supplier1)
                .quantity(BigDecimal.valueOf(50))
                .price(bidPrice)
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true))
                .build();
        orderRequestService.process(bidOrderRequest1);

        bidOrderRequest1.setQuantity(BigDecimal.valueOf(60));
        orderRequestService.update(bidOrderRequest1);

        OrderRequest askOrderRequest1 = new OrderRequestBuilder(buyer)
                .quantity(BigDecimal.valueOf(60))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.MARKET)
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true))
                .build();
        OrderResult result = orderRequestService.process(askOrderRequest1);

        Trade trade = testUtils.findTradeByBuyerAndSeller(result.getTape(), supplier1.getId(), buyer.getId());
        Assert.assertNotNull(trade);
        Assert.assertEquals(bidPrice.doubleValue(), trade.getPrice(), 0d);
        Assert.assertEquals(0, BigDecimal.valueOf(60).compareTo(trade.getQty()));
    }


    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    @Transactional
    @Rollback
    public void testUpdateFailed() throws Exception {
        Counterparty buyer = counterPartyService.addCounterParty("buyer");
        Counterparty supplier1 = counterPartyService.addCounterParty("supplier1");

        Invoice invoiceS1B = invoiceService.addInvoice(new Invoice(supplier1, buyer, BigDecimal.valueOf(100), ZERO, new Date()));

        OrderRequest bidOrderRequest1 = new OrderRequestBuilder(supplier1)
                .quantity(BigDecimal.valueOf(50))
                .price(BigDecimal.valueOf(27d))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true))
                .build();
        orderRequestService.process(bidOrderRequest1);

        bidOrderRequest1.setQuantity(BigDecimal.valueOf(120));

        thrown.expect(OrderRequestProcessingException.class);
        thrown.expectMessage("Requested order quantity: 120.00 is greater than available quantity = invoices - discounts: 100.00");

        orderRequestService.update(bidOrderRequest1);
    }
}
