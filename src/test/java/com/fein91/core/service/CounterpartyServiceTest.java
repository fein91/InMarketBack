package com.fein91.core.service;

import com.fein91.InMarketApplication;
import com.fein91.OrderRequestBuilder;
import com.fein91.core.model.OrderSide;
import com.fein91.dao.CounterpartyRepository;
import com.fein91.model.Counterparty;
import com.fein91.model.OrderType;
import com.fein91.service.CounterPartyServiceImpl;
import com.fein91.service.InvoiceServiceImpl;
import com.fein91.service.OrderRequestServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = InMarketApplication.class)
public class CounterpartyServiceTest {

    @Autowired
    CounterPartyServiceImpl counterPartyServiceImpl;
    @Autowired
    CounterpartyRepository counterpartyRepository;
    @Autowired
    InvoiceServiceImpl invoiceServiceImpl;
    @Autowired
    OrderRequestServiceImpl orderRequestServiceImpl;

    @Test
    @Transactional
    @Rollback(true)
    public void test() throws Exception {
        Counterparty buyer = counterPartyServiceImpl.addCounterParty(BigInteger.valueOf(1), "buyer");
        Counterparty supplier1 = counterPartyServiceImpl.addCounterParty(BigInteger.valueOf(2), "supplier1");
        Counterparty supplier2 = counterPartyServiceImpl.addCounterParty(BigInteger.valueOf(3), "supplier2");
        Counterparty supplier3 = counterPartyServiceImpl.addCounterParty(BigInteger.valueOf(4), "supplier3");
        Counterparty supplier4 = counterPartyServiceImpl.addCounterParty(BigInteger.valueOf(6), "supplier4");

        invoiceServiceImpl.addInvoice(BigInteger.valueOf(11), supplier1, buyer, BigDecimal.valueOf(100), BigDecimal.ZERO);
        invoiceServiceImpl.addInvoice(BigInteger.valueOf(12), supplier2, buyer, BigDecimal.valueOf(200), BigDecimal.ZERO);
        invoiceServiceImpl.addInvoice(BigInteger.valueOf(13), supplier3, buyer, BigDecimal.valueOf(50), BigDecimal.ZERO);
        invoiceServiceImpl.addInvoice(BigInteger.valueOf(14), supplier4, buyer, BigDecimal.valueOf(50), BigDecimal.ZERO);

        orderRequestServiceImpl.addOrderRequest(
                new OrderRequestBuilder(BigInteger.valueOf(111), supplier2)
                    .orderSide(OrderSide.BID)
                    .orderType(OrderType.LIMIT)
                    .date(new Date())
                    .price(BigDecimal.valueOf(19))
                    .quantity(BigDecimal.valueOf(25))
                            .build());

        orderRequestServiceImpl.addOrderRequest(
                new OrderRequestBuilder(BigInteger.valueOf(112), supplier1)
                        .orderSide(OrderSide.BID)
                        .orderType(OrderType.LIMIT)
                        .date(new Date())
                        .price(BigDecimal.valueOf(20))
                        .quantity(BigDecimal.valueOf(50))
                        .build());

    }
}
