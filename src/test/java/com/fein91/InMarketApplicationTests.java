package com.fein91;

import com.fein91.core.model.OrderBook;
import com.fein91.core.service.OrderBookBuilder;
import com.fein91.model.Counterparty;
import com.fein91.model.Invoice;
import com.fein91.service.*;
import com.fein91.utils.TestUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static java.math.BigDecimal.ZERO;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = InMarketApplication.class)
@WebAppConfiguration
public class InMarketApplicationTests {

    public static final BigInteger SUPPLIER_ID = BigInteger.valueOf(2);
    public static final BigInteger BUYER_ID = BigInteger.valueOf(1);
    @Autowired
    OrderBookBuilder orderBookBuilder;

    @Autowired
    InvoiceService invoiceService;
    @Autowired
    CounterPartyService counterPartyService;
    TestUtils testUtils = new TestUtils();
    @Autowired
    OrderRequestService orderRequestServiceImpl;

    @Test
    @Transactional
    public void test() {

        Counterparty buyer = counterPartyService.addCounterParty("buyer");
        Counterparty supplier1 = counterPartyService.addCounterParty("supplier1");
        Counterparty supplier2 = counterPartyService.addCounterParty("supplier2");
        Counterparty supplier3 = counterPartyService.addCounterParty("supplier3");
        Counterparty supplier4 = counterPartyService.addCounterParty("supplier4");

        Invoice invoiceS2B= invoiceService.addInvoice(new Invoice(supplier1, buyer, BigDecimal.valueOf(200), ZERO, testUtils.getCurrentDayPlusDays(30)));
        Invoice invoiceS1B = invoiceService.addInvoice(new Invoice(supplier1, buyer, BigDecimal.valueOf(100), ZERO, testUtils.getCurrentDayPlusDays(20)));
        Invoice invoiceS4B = invoiceService.addInvoice(new Invoice(supplier1, buyer, BigDecimal.valueOf(50), ZERO, testUtils.getCurrentDayPlusDays(50)));
        Invoice invoiceS3B = invoiceService.addInvoice(new Invoice(supplier1, buyer, BigDecimal.valueOf(50), ZERO, testUtils.getCurrentDayPlusDays(40)));


    }

}
