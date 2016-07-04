package com.fein91.service;

import com.fein91.InMarketApplication;
import com.fein91.builders.OrderRequestBuilder;
import com.fein91.core.model.OrderSide;
import com.fein91.model.*;
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
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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

        OrderRequest bidOrderRequest2 = new OrderRequestBuilder(supplier2)
                .quantity(BigDecimal.valueOf(150))
                .price(BigDecimal.valueOf(28d))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestService.process(bidOrderRequest2);

        OrderRequest askOrderRequest = new OrderRequestBuilder(buyer)
                .quantity(BigDecimal.valueOf(250))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.MARKET)
                .invoicesChecked(ImmutableMap.of(invoiceS1B.getId(), true, invoiceS2B.getId(), true, invoiceS3B.getId(), true, invoiceS4B.getId(), true))
                .build();
        orderRequestService.process(askOrderRequest);


        List<HistoryOrderRequest> transHistory = historyOrderRequestService.getByCounterparty(buyer);

        Assert.assertEquals(2, transHistory.size());
        HistoryOrderRequest executedOrder = findHistoryOrderRequestByOrderSide(transHistory, OrderType.MARKET);
        Assert.assertEquals(2, executedOrder.getHistoryTrades().size());
        Assert.assertEquals(0, BigDecimal.valueOf(200).compareTo(executedOrder.getQuantity()));

        HistoryOrderRequest limitOrder = findHistoryOrderRequestByOrderSide(transHistory, OrderType.LIMIT);
        Assert.assertNotNull(limitOrder);
        Assert.assertEquals(0, BigDecimal.valueOf(50).compareTo(limitOrder.getQuantity()));
    }

    private HistoryOrderRequest findHistoryOrderRequestByOrderSide(List<HistoryOrderRequest> transHistory, OrderType orderType) {
        return transHistory.stream()
                .filter(historyOrderRequest -> orderType == historyOrderRequest.getOrderType())
                .findFirst()
                .get();
    }


}
