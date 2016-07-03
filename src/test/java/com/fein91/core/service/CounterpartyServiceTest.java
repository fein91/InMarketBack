package com.fein91.core.service;

import com.fein91.InMarketApplication;
import com.fein91.builders.OrderRequestBuilder;
import com.fein91.core.model.OrderSide;
import com.fein91.model.Counterparty;
import com.fein91.model.Invoice;
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

import static java.math.BigDecimal.ZERO;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = InMarketApplication.class)
public class CounterpartyServiceTest {

    @Autowired
    CounterPartyService counterPartyService;
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
        Date testDate = new Date();
        invoiceServiceImpl.addInvoice(new Invoice(supplier1, buyer, BigDecimal.valueOf(100), ZERO, testDate));
        invoiceServiceImpl.addInvoice(new Invoice(supplier2, buyer, BigDecimal.valueOf(200), ZERO, testDate));
        invoiceServiceImpl.addInvoice(new Invoice(supplier3, buyer, BigDecimal.valueOf(50), ZERO, testDate));
        invoiceServiceImpl.addInvoice(new Invoice(supplier4, buyer, BigDecimal.valueOf(50), ZERO, testDate));

        orderRequestService.save(
                new OrderRequestBuilder(supplier2)
                        .orderSide(OrderSide.BID)
                        .orderType(OrderType.LIMIT)
                        .date(testDate)
                        .price(BigDecimal.valueOf(19))
                        .quantity(BigDecimal.valueOf(25))
                        .build());

        orderRequestService.save(
                new OrderRequestBuilder(supplier1)
                        .orderSide(OrderSide.BID)
                        .orderType(OrderType.LIMIT)
                        .date(testDate)
                        .price(BigDecimal.valueOf(20))
                        .quantity(BigDecimal.valueOf(50))
                        .build());

    }
}
