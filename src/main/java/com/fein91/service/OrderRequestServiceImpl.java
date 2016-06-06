package com.fein91.service;

import com.fein91.core.model.OrderBook;
import com.fein91.core.service.OrderBookBuilder;
import com.fein91.model.OrderResult;
import com.fein91.core.model.OrderSide;
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
import java.util.logging.Logger;

@Service("OrderRequestServiceImpl")
public class OrderRequestServiceImpl implements OrderRequestService {

    private final static Logger LOGGER = Logger.getLogger(OrderRequestServiceImpl.class.getName());

    @Autowired
    OrderRequestRepository orderRequestRepository;
    @Autowired
    InvoiceRepository invoiceRepository;
    @Autowired
    LimitOrderBookService lobService;
    @Autowired
    OrderBookBuilder orderBookBuilder;

    @Override
    public List<OrderRequest> getByCounterpartyId(BigInteger counterpartyId) {
        return orderRequestRepository.findByCounterpartyId(counterpartyId);
    }

    @Override
    public OrderRequest addOrderRequest(OrderRequest orderRequest) {
        return orderRequestRepository.save(orderRequest);
    }

    @Override
    @Transactional
    public OrderResult processOrderRequest(OrderRequest orderRequest) {
        orderRequestRepository.save(orderRequest);

        OrderBook lob = orderBookBuilder.getInstance();
        for (OrderRequest limitOrderRequest : findLimitOrderRequestsToTrade(orderRequest.getCounterparty().getId(), orderRequest.getOrderSide())) {
            addOrderRequest(lob, limitOrderRequest);
        }

        return addOrderRequest(lob, orderRequest);
    }

    @Override
    @Transactional
    public OrderResult calculateOrderRequest(OrderRequest orderRequest) {
        OrderBook lob = orderBookBuilder.getStubInstance();
        for (OrderRequest limitOrderRequest : findLimitOrderRequestsToTrade(orderRequest.getCounterparty().getId(), orderRequest.getOrderSide())) {
            addOrderRequest(lob, limitOrderRequest);
        }

        return addOrderRequest(lob, orderRequest);
    }

    @Override
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
             orderRequests.addAll(orderRequestRepository.findByCounterpartyAndOrderSide(giver, orderSide.oppositeSide().getId()));
        }
        return orderRequests;
    }

    @Transactional
    @Deprecated
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

    @Override
    @Transactional
    public void removeOrderRequest(BigInteger orderId) {
        orderRequestRepository.delete(orderId);
        LOGGER.info("Order request was removed: " + orderId);
    }

    @Override
    @Transactional
    public OrderRequest updateOrderRequest(BigInteger orderId, BigDecimal qty) {
        OrderRequest orderRequest = orderRequestRepository.findOne(orderId);
        orderRequest.setQuantity(qty);

        return orderRequestRepository.save(orderRequest);
    }

    private OrderResult addOrderRequest(OrderBook lob, OrderRequest orderRequest) {
        if (OrderType.MARKET == orderRequest.getOrderType()) {
            if (OrderSide.ASK == orderRequest.getOrderSide()) {
                return lobService.addAskMarketOrder(lob, orderRequest);
            } else if (OrderSide.BID == orderRequest.getOrderSide()) {
                return lobService.addBidMarketOrder(lob, orderRequest);
            }
        } else if (OrderType.LIMIT == orderRequest.getOrderType()) {
            if (OrderSide.ASK == orderRequest.getOrderSide()) {
                return lobService.addAskLimitOrder(lob, orderRequest);
            } else if (OrderSide.BID == orderRequest.getOrderSide()) {
                return lobService.addBidLimitOrder(lob, orderRequest);
            }
        }
        throw new IllegalStateException("Couldn't reach here");
    }

}
