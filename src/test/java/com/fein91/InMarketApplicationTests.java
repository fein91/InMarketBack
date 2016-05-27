package com.fein91;

import com.fein91.model.Counterparty;
import com.fein91.model.Invoice;
import com.fein91.service.CounterPartyService;
import com.fein91.service.InvoiceService;
import com.fein91.service.OrderRequestService;
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


    @Test
    @Transactional
    public void test() {
        Counterparty supplier = counterPartyService.addCounterParty(SUPPLIER_ID, "supplier");
        Counterparty buyer = counterPartyService.addCounterParty(BUYER_ID, "buyer");

        Invoice invoice = invoiceService.addInvoice(BigInteger.valueOf(111), supplier, buyer, BigDecimal.valueOf(1000));

        //orderRequestService.processBidMarketOrderRequest(supplier.getId());
    }

}
