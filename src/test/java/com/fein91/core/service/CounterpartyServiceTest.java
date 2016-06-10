package com.fein91.core.service;

import com.fein91.InMarketApplication;
import com.fein91.builders.OrderRequestBuilder;
import com.fein91.core.model.OrderSide;
import com.fein91.dao.CounterpartyRepository;
import com.fein91.model.Counterparty;
import com.fein91.model.OrderType;
import com.fein91.service.CounterPartyService;
import com.fein91.service.InvoiceService;
import com.fein91.service.OrderRequestService;
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

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = InMarketApplication.class)
public class CounterpartyServiceTest {

    @Autowired
    CounterPartyService counterPartyService;
    @Autowired
    CounterpartyRepository counterpartyRepository;
    @Autowired
    @Qualifier("InvoiceServiceImpl")
    InvoiceService invoiceServiceImpl;
    @Autowired
    @Qualifier("OrderRequestServiceImpl")
    OrderRequestService orderRequestService;

    @Test
    @Transactional
    @Rollback
    public void test() throws Exception {
        Counterparty buyer = counterPartyService.addCounterParty("buyer");
        Counterparty supplier1 = counterPartyService.addCounterParty("supplier1");
        Counterparty supplier2 = counterPartyService.addCounterParty("supplier2");
        Counterparty supplier3 = counterPartyService.addCounterParty("supplier3");
        Counterparty supplier4 = counterPartyService.addCounterParty("supplier4");

        invoiceServiceImpl.addInvoice(supplier1, buyer, BigDecimal.valueOf(100), BigDecimal.ZERO);
        invoiceServiceImpl.addInvoice(supplier2, buyer, BigDecimal.valueOf(200), BigDecimal.ZERO);
        invoiceServiceImpl.addInvoice(supplier3, buyer, BigDecimal.valueOf(50), BigDecimal.ZERO);
        invoiceServiceImpl.addInvoice(supplier4, buyer, BigDecimal.valueOf(50), BigDecimal.ZERO);

        orderRequestService.addOrderRequest(
                new OrderRequestBuilder(supplier2)
                        .orderSide(OrderSide.BID)
                        .orderType(OrderType.LIMIT)
                        .date(new Date())
                        .price(BigDecimal.valueOf(19))
                        .quantity(BigDecimal.valueOf(25))
                        .build());

        orderRequestService.addOrderRequest(
                new OrderRequestBuilder(supplier1)
                        .orderSide(OrderSide.BID)
                        .orderType(OrderType.LIMIT)
                        .date(new Date())
                        .price(BigDecimal.valueOf(20))
                        .quantity(BigDecimal.valueOf(50))
                        .build());

    }
}
