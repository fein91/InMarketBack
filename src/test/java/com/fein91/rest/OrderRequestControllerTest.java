package com.fein91.rest;

import com.fein91.InMarketApplication;
import com.fein91.builders.OrderRequestBuilder;
import com.fein91.core.model.OrderSide;
import com.fein91.model.Counterparty;
import com.fein91.model.OrderRequest;
import com.fein91.model.OrderType;
import com.fein91.rest.exception.OrderRequestException;
import com.fein91.service.OrderRequestService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
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

    @Test
    public void process() throws OrderRequestException {
        expect(serviceMock.process(anyObject(OrderRequest.class))).andReturn(null);

        replay(serviceMock);

        controller.process(new OrderRequestBuilder(new Counterparty())
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.LIMIT)
                .price(BigDecimal.ONE)
                .quantity(BigDecimal.TEN)
                .build());

        verify(serviceMock);
    }


    @Test
    public void calculate() throws OrderRequestException {
        expect(serviceMock.calculate(anyObject(OrderRequest.class))).andReturn(null);

        replay(serviceMock);

        controller.calculate(new OrderRequestBuilder(new Counterparty())
                .orderSide(OrderSide.ASK)
                .orderType(OrderType.LIMIT)
                .price(BigDecimal.ONE)
                .quantity(BigDecimal.TEN)
                .build());

        verify(serviceMock);
    }

    @Test(expected = OrderRequestException.class)
    public void processWithException() throws OrderRequestException {
        expect(serviceMock.calculate(anyObject(OrderRequest.class))).andReturn(null);

        replay(serviceMock);

        controller.process(new OrderRequest());
    }

    @Test(expected = OrderRequestException.class)
    public void calculateWithException() throws OrderRequestException {
        expect(serviceMock.calculate(anyObject(OrderRequest.class))).andReturn(null);

        replay(serviceMock);

        controller.calculate(new OrderRequest());
    }
}
