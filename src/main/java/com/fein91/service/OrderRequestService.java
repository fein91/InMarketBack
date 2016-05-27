package com.fein91.service;

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

    public OrderRequest addOrderRequest(BigInteger id) {
        return orderRequestRepository.save(new OrderRequest());
    }

    @Transactional
    public void processOrderRequest(OrderRequest orderRequest) {
        List<Invoice> invoices = invoiceRepository.findInvoicesBySourceId(orderRequest.getCounterparty().getId());
        LimitOrderBookDecorator lobDecorator = new LimitOrderBookDecorator();
        for (Invoice invoice : invoices) {
            Counterparty target = invoice.getTarget();
            addAllTargetOrdersToLOB(lobDecorator, target);
        }

        addOrderRequest(lobDecorator, orderRequest);
    }

    public void addAllTargetOrdersToLOB(LimitOrderBookDecorator lobDecorator, Counterparty counterparty) {
        List<OrderRequest> orderRequests = orderRequestRepository.findByCounterparty(counterparty);

        for (OrderRequest orderRequest : orderRequests) {
            addOrderRequest(lobDecorator, orderRequest);
        }
    }

    private void addOrderRequest(LimitOrderBookDecorator lobDecorator, OrderRequest orderRequest) {
        if (OrderType.MARKET == orderRequest.getOrderType()) {
            if (OrderSide.ASK == orderRequest.getOrderSide()) {
                lobService.addAskMarketOrder(lobDecorator, orderRequest);
            } else if (OrderSide.BID == orderRequest.getOrderSide()) {
                lobService.addBidMarketOrder(lobDecorator, orderRequest);
            }
        } else if (OrderType.LIMIT == orderRequest.getOrderType()) {
            if (OrderSide.ASK == orderRequest.getOrderSide()) {
                lobService.addAskLimitOrder(lobDecorator, orderRequest);
            } else if (OrderSide.BID == orderRequest.getOrderSide()) {
                lobService.addBidLimitOrder(lobDecorator, orderRequest);
            }
        }
    }

}
