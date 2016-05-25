package com.fein91.core.service;

import com.fein91.InMarketApplication;
import com.fein91.dao.CounterpartyRepository;
import com.fein91.dao.RequestRepository;
import com.fein91.model.Counterparty;
import com.fein91.model.Invoice;
import com.fein91.model.Request;
import com.fein91.service.CounterPartyService;
import com.fein91.service.InvoiceService;
import com.fein91.service.RequestService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = InMarketApplication.class)
public class LimitOrderBookServiceTest {

    public static final BigInteger BUYER1_ID = BigInteger.valueOf(1);
    public static final BigInteger BUYER2_ID = BigInteger.valueOf(3);
    public static final BigInteger BUYER3_ID = BigInteger.valueOf(4);
    public static final BigInteger SUPPLIER_ID = BigInteger.valueOf(2);

    @Autowired
    LimitOrderBookService limitOrderBookService;
    @Autowired
    CounterPartyService counterPartyService;
    @Autowired
    CounterpartyRepository counterpartyRepository;
    @Autowired
    InvoiceService invoiceService;

    @Test
    @Transactional
    @Rollback(true)
    public void test1() throws Exception {
        LimitOrderBookDecorator lob = new LimitOrderBookDecorator();

        Counterparty supplier = counterPartyService.addCounterParty(BigInteger.valueOf(1), "supplier");
        Counterparty buyer1 = counterPartyService.addCounterParty(BigInteger.valueOf(2), "buyer1");
        Counterparty buyer2 = counterPartyService.addCounterParty(BigInteger.valueOf(3), "buyer2");
        Counterparty buyer3 = counterPartyService.addCounterParty(BigInteger.valueOf(4), "buyer3");

        invoiceService.addInvoice(BigInteger.valueOf(11), supplier, buyer1, BigDecimal.valueOf(100));
        invoiceService.addInvoice(BigInteger.valueOf(12), supplier, buyer2, BigDecimal.valueOf(200));
        invoiceService.addInvoice(BigInteger.valueOf(13), supplier, buyer3, BigDecimal.valueOf(50));


        limitOrderBookService.addAskLimitOrder(lob, buyer1, 200, 27d);
        limitOrderBookService.addAskLimitOrder(lob, buyer2, 150, 28d);
        limitOrderBookService.addAskLimitOrder(lob, buyer3, 100, 29d);

        limitOrderBookService.addBidMarketOrder(lob, supplier, 150);

    }
}
