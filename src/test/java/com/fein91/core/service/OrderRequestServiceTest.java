package com.fein91.core.service;

import com.fein91.InMarketApplication;
import com.fein91.OrderRequestBuilder;
import com.fein91.core.model.OrderSide;
import com.fein91.dao.CounterpartyRepository;
import com.fein91.model.Counterparty;
import com.fein91.model.OrderRequest;
import com.fein91.model.OrderType;
import com.fein91.service.CounterPartyService;
import com.fein91.service.InvoiceService;
import com.fein91.service.OrderRequestService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.math.BigInteger;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = InMarketApplication.class)
public class OrderRequestServiceTest {

    @Autowired
    OrderRequestService orderRequestService;
    @Autowired
    CounterPartyService counterPartyService;
    @Autowired
    CounterpartyRepository counterpartyRepository;
    @Autowired
    InvoiceService invoiceService;

    @Test
    public void test() {
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
                .price(BigDecimal.valueOf(15))
                .orderSide(OrderSide.BID)
                .orderType(OrderType.LIMIT)
                .build();
        orderRequestService.addOrderRequest(bidOrderRequest2);

        invoiceService.addInvoice(BigInteger.valueOf(11), supplier1, buyer, BigDecimal.valueOf(100));
        invoiceService.addInvoice(BigInteger.valueOf(12), supplier2, buyer, BigDecimal.valueOf(150));

        OrderRequest marketOrderRequest1 = new OrderRequestBuilder(BigInteger.valueOf(1234), buyer)
                .quantity(BigDecimal.valueOf(100))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.MARKET)
                .build();
        orderRequestService.processOrderRequest(marketOrderRequest1);


        OrderRequest marketOrderRequest2 = new OrderRequestBuilder(BigInteger.valueOf(1233), buyer)
                .quantity(BigDecimal.valueOf(200))
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.MARKET)
                .build();
        orderRequestService.processOrderRequest(marketOrderRequest2);



    }
}
