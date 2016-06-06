package com.fein91;

import com.fein91.core.model.OrderBook;
import com.fein91.core.service.OrderBookBuilder;
import com.fein91.model.Counterparty;
import com.fein91.model.Invoice;
import com.fein91.service.CounterPartyService;
import com.fein91.service.InvoiceService;
import com.fein91.service.OrderRequestService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.BigInteger;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = InMarketApplication.class)
@WebAppConfiguration
public class InMarketApplicationTests {

    public static final BigInteger SUPPLIER_ID = BigInteger.valueOf(2);
    public static final BigInteger BUYER_ID = BigInteger.valueOf(1);
    @Autowired
    CounterPartyService counterPartyService;
    @Autowired
    InvoiceService invoiceService;
    @Autowired
    OrderRequestService orderRequestService;
    @Autowired
    OrderBookBuilder orderBookBuilder;


    @Test
    @Transactional
    public void test() {
        OrderBook orderBook = orderBookBuilder.getInstance();

        OrderBook orderBook1 = orderBookBuilder.getInstance();
        Assert.assertNotSame(orderBook, orderBook1);

    }

}
