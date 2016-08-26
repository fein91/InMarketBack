package com.fein91.service;

import com.fein91.InMarketApplication;
import com.fein91.builders.OrderRequestBuilder;
import com.fein91.core.model.OrderSide;
import com.fein91.model.*;
import com.fein91.rest.exception.RollbackOnCalculateException;
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

import static java.math.BigDecimal.ZERO;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = InMarketApplication.class)
public class RollbackOnCalcTest {

    @Autowired
    CounterPartyService counterPartyService;
    @Autowired
    InvoiceService invoiceServiceImpl;
    @Autowired
    OrderRequestService orderRequestServiceImpl;

    private final TestUtils testUtils = new TestUtils();

    @Test
    @Transactional
    @Rollback
    public void test() throws Exception {
        Counterparty buyer = counterPartyService.addCounterParty("buyer");
        Counterparty supplier1 = counterPartyService.addCounterParty("supplier1");

        Invoice invoice1S1B = invoiceServiceImpl.addInvoice(new Invoice(supplier1, buyer, BigDecimal.valueOf(50000), ZERO, testUtils.getCurrentDayPlusDays(5)));

        double supplier1AskPrice = 22d;
        OrderRequest bidOrderRequest1 = new OrderRequestBuilder(supplier1)
                .quantity(BigDecimal.valueOf(20000))
                .price(BigDecimal.valueOf(supplier1AskPrice))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .invoicesChecked(ImmutableMap.of(invoice1S1B.getId(), true))
                .build();
        orderRequestServiceImpl.process(bidOrderRequest1);

        OrderRequest askMarketOrderRequest = new OrderRequestBuilder(buyer)
                .quantity(BigDecimal.valueOf(20000))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.MARKET)
                .invoicesChecked(ImmutableMap.of(invoice1S1B.getId(), true))
                .build();
        runCalc(askMarketOrderRequest);

        Invoice invoice1S1BFromDb = invoiceServiceImpl.getById(invoice1S1B.getId());
        Assert.assertEquals(invoice1S1B, invoice1S1BFromDb);

    }

    private void runCalc(OrderRequest orderRequest) {
        try {
            orderRequestServiceImpl.calculate(orderRequest);
        } catch (RollbackOnCalculateException ex) {
            //do nothing, expected behavior
        }
    }
}
