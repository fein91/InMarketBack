package com.fein91;

import com.fein91.dao.CounterpartyRepository;
import com.fein91.dao.InvoiceRepository;
import com.fein91.dao.OrderBookRepository;
import com.fein91.model.Counterparty;
import com.fein91.model.Invoice;
import com.fein91.model.OrderBook;
import com.fein91.model.OrderType;
import junit.framework.Assert;
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
    CounterpartyRepository counterpartyRepository;
    @Autowired
    InvoiceRepository invoiceRepository;
    @Autowired
    OrderBookRepository orderBookRepository;


	@Test
    @Transactional
	public void contextLoads() {
        Counterparty buyer = counterpartyRepository.findById(BUYER_ID);
        Assert.assertEquals(BUYER_ID, buyer.getId());
        Assert.assertEquals(1, buyer.getInvoicesTo().size());
        Assert.assertEquals(0, buyer.getInvoicesFrom().size());

        Invoice supplyerInvoice = invoiceRepository.findById(BigInteger.valueOf(11));
        Assert.assertNotNull(supplyerInvoice);
        Assert.assertEquals(BigDecimal.valueOf(200).compareTo(supplyerInvoice.getValue()), 0);
        Assert.assertEquals(SUPPLIER_ID, supplyerInvoice.getCounterPartyFrom().getId());
        Assert.assertEquals(BUYER_ID, supplyerInvoice.getCounterPartyTo().getId());

        OrderBook order = orderBookRepository.findById(BigInteger.valueOf(111));
        Assert.assertNotNull(order);
        Assert.assertEquals(0, BigDecimal.valueOf(200).compareTo(order.getAmount()));
        Assert.assertEquals(0, BigDecimal.valueOf(25).compareTo(order.getPrice()));
        Assert.assertEquals(OrderType.ASK, order.getOrderType());
    }

}
