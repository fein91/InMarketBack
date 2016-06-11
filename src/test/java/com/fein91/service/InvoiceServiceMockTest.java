package com.fein91.service;

import com.fein91.InMarketApplication;
import com.fein91.dao.InvoiceRepository;
import com.fein91.model.Counterparty;
import com.fein91.model.Invoice;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

import static org.easymock.EasyMock.*;

/**
 * Unit test for invoice service
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = InMarketApplication.class)
public class InvoiceServiceMockTest {

    private InvoiceService invoiceService;
    private InvoiceRepository repoMock;

    @Before
    public void setUp() {
        repoMock = createMock(InvoiceRepository.class);
        invoiceService = new InvoiceServiceImpl(repoMock);
    }

    @After
    public void tearDown() {
        verify(repoMock);
    }

    @Test
    public void addInvoice() {
        Counterparty cpSource = Counterparty.of("sourceCP");
        Counterparty cpTarget = Counterparty.of("targetCP");
        BigDecimal value = BigDecimal.ONE;
        BigDecimal prepaidValue = BigDecimal.TEN;

        Invoice invoice = new Invoice(cpSource, cpTarget, value, prepaidValue, new Date());
        invoice.setId(1L);

        expect(repoMock.save(anyObject(Invoice.class))).andReturn(invoice);

        replay(repoMock);

        Invoice addedInvoice = invoiceService.addInvoice(new Invoice(cpSource, cpTarget, value, prepaidValue, new Date()));

        Assert.assertNotNull(addedInvoice.getId());
        Assert.assertEquals(invoice, addedInvoice);
    }

    @Test
    public void updateInvoice() {
        Invoice invoice = createMock(Invoice.class);

        expect(invoice.getPrepaidValue()).andReturn(BigDecimal.ONE);
        invoice.setPrepaidValue(BigDecimal.valueOf(4));
        expectLastCall().once();

        expect(repoMock.save(anyObject(Invoice.class))).andReturn(invoice);

        replay(repoMock, invoice);

        invoiceService.updateInvoice(invoice, BigDecimal.valueOf(3));

        verify(invoice);
    }

    @Test
    public void findBySourceAndTarget() {
        expect(repoMock.findBySourceAndTarget(1L, 2L)).andReturn(new ArrayList<>());

        replay(repoMock);

        invoiceService.findBySourceAndTarget(1L, 2L);
    }

    @Test
    public void getById() {
        expect(repoMock.findOne(1L)).andReturn(new Invoice());

        replay(repoMock);

        invoiceService.getById(1L);
    }
}
