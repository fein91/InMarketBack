package com.fein91.service;

import com.fein91.model.OrderResult;
import com.fein91.core.model.OrderSide;
import com.fein91.core.service.LimitOrderBookDecorator;
import com.fein91.core.service.LimitOrderBookService;
import com.fein91.dao.InvoiceRepository;
import com.fein91.dao.OrderRequestRepository;
import com.fein91.model.Counterparty;
import com.fein91.model.Invoice;
import com.fein91.model.OrderRequest;
import com.fein91.model.OrderType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by olta1014 on 23.05.2016.
 */
@Service
public class OrderRequestService {

    @Autowired
    OrderRequestRepository orderRequestRepository;
    @Autowired
    InvoiceRepository invoiceRepository;
    @Autowired
    LimitOrderBookService lobService;

    public OrderRequest addOrderRequest(OrderRequest orderRequest) {
        return orderRequestRepository.save(orderRequest);
    }

    @Transactional
    public OrderResult processOrderRequest(OrderRequest orderRequest) {
        LimitOrderBookDecorator lobDecorator = new LimitOrderBookDecorator();
        for (OrderRequest limitOrderRequest : findLimitOrderRequestsToTrade(orderRequest.getCounterparty().getId(), orderRequest.getOrderSide())) {
            addOrderRequest(lobDecorator, limitOrderRequest);
        }

        return addOrderRequest(lobDecorator, orderRequest);
    }

    @Transactional
    public List<OrderRequest> findLimitOrderRequestsToTrade(BigInteger counterpartyId, OrderSide orderSide) {
        List<Invoice> invoices = OrderSide.BID == orderSide
                ? invoiceRepository.findInvoicesBySourceId(counterpartyId)
                : invoiceRepository.findInvoicesByTargetId(counterpartyId);

        List<OrderRequest> orderRequests = new ArrayList<>();
        for (Invoice invoice : invoices) {
            Counterparty giver = OrderSide.BID == orderSide
                    ? invoice.getTarget()
                    : invoice.getSource();
             orderRequests.addAll(orderRequestRepository.findByCounterpartyAndOrderSide(giver, orderSide.getId()));
        }
        return orderRequests;
    }

    @Transactional
    public BigDecimal findLimitOrderRequestsToTradeSum(BigInteger counterpartyId, OrderSide orderSide) {
        List<Invoice> invoices = OrderSide.BID == orderSide
                ? invoiceRepository.findInvoicesBySourceId(counterpartyId)
                : invoiceRepository.findInvoicesByTargetId(counterpartyId);

        BigDecimal result = BigDecimal.ZERO;
        for (Invoice invoice : invoices) {
            Counterparty giver = OrderSide.BID == orderSide
                    ? invoice.getTarget()
                    : invoice.getSource();
            result = result.add(orderRequestRepository.findByCounterpartyAndOrderSide(giver, orderSide.getId()).stream()
                    .map(OrderRequest :: getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        return result;
    }

    private OrderResult addOrderRequest(LimitOrderBookDecorator lobDecorator, OrderRequest orderRequest) {
        if (OrderType.MARKET == orderRequest.getOrderType()) {
            if (OrderSide.ASK == orderRequest.getOrderSide()) {
                return lobService.addAskMarketOrder(lobDecorator, orderRequest);
            } else if (OrderSide.BID == orderRequest.getOrderSide()) {
                return lobService.addBidMarketOrder(lobDecorator, orderRequest);
            }
        } else if (OrderType.LIMIT == orderRequest.getOrderType()) {
            if (OrderSide.ASK == orderRequest.getOrderSide()) {
                return lobService.addAskLimitOrder(lobDecorator, orderRequest);
            } else if (OrderSide.BID == orderRequest.getOrderSide()) {
                return lobService.addBidLimitOrder(lobDecorator, orderRequest);
            }
        }
        throw new IllegalStateException("Couldn't reach here");
    }

}
