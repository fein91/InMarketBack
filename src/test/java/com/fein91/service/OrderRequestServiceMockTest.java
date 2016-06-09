package com.fein91.service;

import com.fein91.InMarketApplication;
import com.fein91.OrderRequestBuilder;
import com.fein91.core.model.OrderBook;
import com.fein91.core.service.LimitOrderBookService;
import com.fein91.core.service.OrderBookBuilder;
import com.fein91.dao.InvoiceRepository;
import com.fein91.dao.OrderRequestRepository;
import com.fein91.model.Counterparty;
import com.fein91.model.Invoice;
import com.fein91.model.OrderRequest;
import com.fein91.model.OrderType;
import com.google.common.collect.ImmutableList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;

import static com.fein91.core.model.OrderSide.ASK;
import static com.fein91.core.model.OrderSide.BID;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static org.easymock.EasyMock.*;

/**
 * Unit test for order request service
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = InMarketApplication.class)
public class OrderRequestServiceMockTest {

    private OrderRequestRepository orderRequestRepository;
    private InvoiceRepository invoiceRepository;
    private LimitOrderBookService lobService;
    private OrderBookBuilder orderBookBuilder;
    private OrderRequestService orderRequestService;

    @Before
    public void setUp() {
        orderRequestRepository = createMock(OrderRequestRepository.class);
        invoiceRepository = createMock(InvoiceRepository.class);
        lobService = createMock(LimitOrderBookService.class);
        orderBookBuilder = createMock(OrderBookBuilder.class);
        orderRequestService = new OrderRequestServiceImpl(
                orderRequestRepository,
                invoiceRepository,
                lobService,
                orderBookBuilder);
    }

    @After
    public void tearDown() {
//        verify(repoMock);
    }

    @Test
    public void getByCounterpartyId() {
        expect(orderRequestRepository.findByCounterpartyId(1L)).andReturn(new ArrayList<>());

        replay(orderRequestRepository);

        orderRequestService.getByCounterpartyId(1L);

        verify(orderRequestRepository);
    }

    @Test
    public void addOrderRequest() {
        OrderRequest request = new OrderRequest();
        expect(orderRequestRepository.save(anyObject(OrderRequest.class))).andReturn(request);

        replay(orderRequestRepository);

        orderRequestService.addOrderRequest(request);

        verify(orderRequestRepository);
    }

    @Test
    public void processOrderRequest() {
        Counterparty supplier = Counterparty.of("supplier");
        supplier.setId(1L);
        OrderRequest request = new OrderRequestBuilder(supplier)
                .quantity(ONE)
                .price(TEN)
                .orderSide(BID)
                .orderType(OrderType.LIMIT)
                .build();
        expect(orderRequestRepository.save(anyObject(OrderRequest.class))).andReturn(request);
        expect(orderBookBuilder.getInstance()).andReturn(new OrderBook());
        expect(invoiceRepository.findInvoicesBySourceId(1L)).andReturn(new ArrayList<>());

        replay(orderRequestRepository, orderBookBuilder, invoiceRepository);

        orderRequestService.processOrderRequest(request);

        verify(orderRequestRepository, orderBookBuilder, invoiceRepository);
    }

    @Test
    public void calculateOrderRequest() {
        Counterparty supplier = Counterparty.of("supplier");
        supplier.setId(1L);
        OrderRequest request = new OrderRequestBuilder(supplier)
                .quantity(ONE)
                .price(TEN)
                .orderSide(BID)
                .orderType(OrderType.LIMIT)
                .build();
        expect(orderBookBuilder.getStubInstance()).andReturn(new OrderBook());
        expect(invoiceRepository.findInvoicesBySourceId(1L)).andReturn(new ArrayList<>());

        replay(orderRequestRepository, orderBookBuilder, invoiceRepository);

        orderRequestService.calculateOrderRequest(request);

        verify(orderBookBuilder, invoiceRepository);
    }

    @Test
    public void findLimitOrderRequestsToTrade() {
        // ASK + empty invoices
        expect(invoiceRepository.findInvoicesByTargetId(1L)).andReturn(new ArrayList<>());
        replay(invoiceRepository);
        orderRequestService.findLimitOrderRequestsToTrade(1L, ASK);

        verify(invoiceRepository);

        // BID + empty invoices
        reset(invoiceRepository);
        expect(invoiceRepository.findInvoicesBySourceId(1L)).andReturn(new ArrayList<>());
        replay(invoiceRepository);
        orderRequestService.findLimitOrderRequestsToTrade(1L, BID);

        verify(invoiceRepository);

        //ASK + some invoices
        reset(invoiceRepository);
        Counterparty source = Counterparty.of("src");
        Counterparty target = Counterparty.of("target");
        Invoice invoice = new Invoice(11L, source, target, ONE, ONE);

        expect(invoiceRepository.findInvoicesByTargetId(1L)).andReturn(ImmutableList.of(invoice));
        expect(orderRequestRepository.findByCounterpartyAndOrderSide(source, BID.getId())).andReturn(new ArrayList<>());

        replay(invoiceRepository, orderRequestRepository);
        orderRequestService.findLimitOrderRequestsToTrade(1L, ASK);

        verify(invoiceRepository, orderRequestRepository);

        //BID + some invoices
        reset(invoiceRepository, orderRequestRepository);

        expect(invoiceRepository.findInvoicesBySourceId(1L)).andReturn(ImmutableList.of(invoice));
        expect(orderRequestRepository.findByCounterpartyAndOrderSide(target, ASK.getId())).andReturn(new ArrayList<>());

        replay(invoiceRepository, orderRequestRepository);
        orderRequestService.findLimitOrderRequestsToTrade(1L, BID);

        verify(invoiceRepository, orderRequestRepository);
    }

    @Test
    public void removeOrderRequest() {
        orderRequestRepository.delete(1L);
        expectLastCall();

        replay(orderRequestRepository);
        orderRequestService.removeOrderRequest(1L);

        verify(orderRequestRepository);
    }

    @Test
    public void updateOrderRequest() {
        OrderRequest request = createMock(OrderRequest.class);
        request.setQuantity(ONE);
        expectLastCall();
        expect(orderRequestRepository.save(request)).andReturn(request);
        expect(orderRequestRepository.findOne(1L)).andReturn(request);

        replay(orderRequestRepository);
        orderRequestService.updateOrderRequest(1L, ONE);

        verify(orderRequestRepository);
    }

}