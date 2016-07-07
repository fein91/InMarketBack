package com.fein91.rest;

import com.fein91.InMarketApplication;
import com.fein91.dao.InvoiceRepository;
import com.fein91.service.HistoryOrderRequestService;
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

    private InvoiceRepository repoMock;
    private CounterpartyController controller;
    private HistoryOrderRequestService historyOrderRequestService;

    @Before
    public void setUp() {
        repoMock = createMock(InvoiceRepository.class);
        historyOrderRequestService = createMock(HistoryOrderRequestService.class);
        controller = new CounterpartyController(repoMock, historyOrderRequestService);
    }

    @After
    public void tearDown() {
        verify(repoMock);
    }

    @Test
    public void getBySourceId() {
        expect(repoMock.findInvoicesBySourceId(1L)).andReturn(new ArrayList<>());

        replay(repoMock);

        controller.getBySourceId(1L);
    }

    @Test
    public void getByTargetId() {
        expect(repoMock.findInvoicesByTargetId(1L)).andReturn(new ArrayList<>());

        replay(repoMock);

        controller.getByTargetId(1L);
    }
}
