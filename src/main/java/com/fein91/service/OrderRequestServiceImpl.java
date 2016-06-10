package com.fein91.service;

import com.fein91.core.model.OrderBook;
import com.fein91.core.model.OrderSide;
import com.fein91.core.service.LimitOrderBookService;
import com.fein91.core.service.OrderBookBuilder;
import com.fein91.dao.InvoiceRepository;
import com.fein91.dao.OrderRequestRepository;
import com.fein91.model.*;
import com.fein91.rest.exception.OrderRequestException;
import com.fein91.rest.exception.OrderRequestProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@Service("OrderRequestServiceImpl")
public class OrderRequestServiceImpl implements OrderRequestService {

    private final static Logger LOGGER = Logger.getLogger(OrderRequestServiceImpl.class.getName());

    private final OrderRequestRepository orderRequestRepository;
    private final InvoiceRepository invoiceRepository;
    private final LimitOrderBookService lobService;
    private final OrderBookBuilder orderBookBuilder;

    @Autowired
    public OrderRequestServiceImpl(OrderRequestRepository orderRequestRepository,
                                   InvoiceRepository invoiceRepository,
                                   LimitOrderBookService lobService,
                                   OrderBookBuilder orderBookBuilder) {
        this.orderRequestRepository = orderRequestRepository;
        this.invoiceRepository = invoiceRepository;
        this.lobService = lobService;
        this.orderBookBuilder = orderBookBuilder;
    }

    @Override
    public List<OrderRequest> getByCounterpartyId(Long counterpartyId) {
        return orderRequestRepository.findByCounterpartyId(counterpartyId);
    }

    @Override
    public OrderRequest addOrderRequest(OrderRequest orderRequest) {
        return orderRequestRepository.save(orderRequest);
    }

    @Override
    @Transactional
    public OrderResult processOrderRequest(OrderRequest orderRequest) throws OrderRequestException {
        orderRequestRepository.save(orderRequest);

        OrderBook lob = orderBookBuilder.getInstance();
        for (OrderRequest limitOrderRequest : findLimitOrderRequestsToTrade(orderRequest.getCounterparty().getId(), orderRequest.getOrderSide())) {
            lobService.addOrder(lob, limitOrderRequest);
        }

        return lobService.addOrder(lob, orderRequest);
    }

    @Override
    @Transactional
    public OrderResult calculateOrderRequest(OrderRequest orderRequest) {
        OrderBook lob = orderBookBuilder.getStubInstance();
        for (OrderRequest limitOrderRequest : findLimitOrderRequestsToTrade(orderRequest.getCounterparty().getId(), orderRequest.getOrderSide())) {
            lobService.addOrder(lob, limitOrderRequest);
        }

        return lobService.addOrder(lob, orderRequest);
    }

    @Override
    @Transactional
    public Set<OrderRequest> findLimitOrderRequestsToTrade(Long counterpartyId, OrderSide orderSide) {
        List<Invoice> invoices = OrderSide.BID == orderSide
                ? invoiceRepository.findInvoicesBySourceId(counterpartyId)
                : invoiceRepository.findInvoicesByTargetId(counterpartyId);

        if (CollectionUtils.isEmpty(invoices)) {
            throw new OrderRequestProcessingException("No invoices were found while processing order request");
        }

        Set<Counterparty> counterparties = new HashSet<>();
        Set<OrderRequest> orderRequests = new HashSet<>();
        for (Invoice invoice : invoices) {
            Counterparty giver = OrderSide.BID == orderSide
                    ? invoice.getTarget()
                    : invoice.getSource();

            if (counterparties.add(giver)) {
                orderRequests.addAll(orderRequestRepository.findByCounterpartyAndOrderSide(giver, orderSide.oppositeSide().getId()));
            }
        }

        if (CollectionUtils.isEmpty(orderRequests)) {
            throw new OrderRequestProcessingException("No suitable order requests were found");
        }

        return orderRequests;
    }

    @Override
    @Transactional
    public void removeOrderRequest(Long orderId) {
        orderRequestRepository.delete(orderId);
        LOGGER.info("Order request was removed: " + orderId);
    }

    @Override
    @Transactional
    public OrderRequest updateOrderRequest(Long orderId, BigDecimal qty) {
        OrderRequest orderRequest = orderRequestRepository.findOne(orderId);
        orderRequest.setQuantity(qty);

        LOGGER.info(orderRequest + " quantity was updated to: " + qty);
        return orderRequestRepository.save(orderRequest);
    }
}
