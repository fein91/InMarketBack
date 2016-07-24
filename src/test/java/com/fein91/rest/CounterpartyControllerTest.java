package com.fein91.rest;

import com.fein91.InMarketApplication;
import com.fein91.service.*;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
    private OrderRequestService orderRequestService;

    @Before
    public void setUp() {
        invoiceService = createMock(InvoiceService.class);
        historyOrderRequestService = createMock(HistoryOrderRequestService.class);
        counterPartyService = createMock(CounterPartyService.class);
        orderRequestService = createMock(OrderRequestService.class);
        controller = new CounterpartyController(invoiceService, historyOrderRequestService, counterPartyService, orderRequestService);
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
