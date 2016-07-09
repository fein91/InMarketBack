package com.fein91.rest;

import com.fein91.InMarketApplication;
import com.fein91.dao.InvoiceRepository;
import com.fein91.service.CounterPartyService;
import com.fein91.service.HistoryOrderRequestService;
import com.fein91.service.InvoiceService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;

import static org.easymock.EasyMock.*;

/**
 * Unit test for counterparty controller
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = InMarketApplication.class)
public class CounterpartyControllerTest {

    private InvoiceService invoiceService;
    private CounterpartyController controller;
    private HistoryOrderRequestService historyOrderRequestService;
    private CounterPartyService counterPartyService;

    @Before
    public void setUp() {
        invoiceService = createMock(InvoiceService.class);
        historyOrderRequestService = createMock(HistoryOrderRequestService.class);
        counterPartyService = createMock(CounterPartyService.class);
        controller = new CounterpartyController(invoiceService, historyOrderRequestService, counterPartyService);
    }

    @After
    public void tearDown() {
        verify(invoiceService);
    }

//    @Test
//    public void getBySourceId() {
//        expect(invoiceService.findInvoicesBySourceId(1L)).andReturn(new ArrayList<>());
//
//        replay(repoMock);
//
//        controller.getBySourceId(1L);
//    }
//
//    @Test
//    public void getByTargetId() {
//        expect(repoMock.findInvoicesByTargetId(1L)).andReturn(new ArrayList<>());
//
//        replay(repoMock);
//
//        controller.getByTargetId(1L);
//    }
}
