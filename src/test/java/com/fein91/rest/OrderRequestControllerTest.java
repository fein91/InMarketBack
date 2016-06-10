package com.fein91.rest;

import com.fein91.InMarketApplication;
import com.fein91.dao.InvoiceRepository;
import com.fein91.model.OrderRequest;
import com.fein91.model.OrderResult;
import com.fein91.service.OrderRequestService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;

import static org.easymock.EasyMock.*;

/**
 * Unit test for order request controller
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = InMarketApplication.class)
public class OrderRequestControllerTest {

    private OrderRequestService serviceMock;
    private OrderRequestController controller;

    @Before
    public void setUp() {
        serviceMock = createMock(OrderRequestService.class);
        controller = new OrderRequestController(serviceMock);
    }

    @After
    public void tearDown() {
        verify(serviceMock);
    }

    @Test
    public void process() {
        expect(serviceMock.processOrderRequest(anyObject(OrderRequest.class))).andReturn(null);

        replay(serviceMock);

        controller.process(new OrderRequest());
    }

    @Test
    public void calculate() {
        expect(serviceMock.calculateOrderRequest(anyObject(OrderRequest.class))).andReturn(null);

        replay(serviceMock);

        controller.calculate(new OrderRequest());
    }

    @Test
    public void getByCounterpartyId() {
        expect(serviceMock.getByCounterpartyId(1L)).andReturn(new ArrayList<>());

        replay(serviceMock);

        controller.getByCounterpartyId(1L);
    }
}
