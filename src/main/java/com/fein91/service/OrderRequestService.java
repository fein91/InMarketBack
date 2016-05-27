package com.fein91.service;

import com.fein91.core.service.LimitOrderBookDecorator;
import com.fein91.dao.InvoiceRepository;
import com.fein91.dao.OrderRequestRepository;
import com.fein91.model.Counterparty;
import com.fein91.model.Invoice;
import com.fein91.model.OrderRequest;
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

    public OrderRequest addOrderRequest(BigInteger id) {
        return orderRequestRepository.save(new OrderRequest());
    }

    public void processLimitOrderRequest(BigInteger counterpartyId, BigDecimal price, int quantity) {

    }

    @Transactional
    public void processBidMarketOrderRequest(BigInteger counterpartyId) {
        List<Invoice> invoices = invoiceRepository.findInvoicesBySourceId(counterpartyId);
        LimitOrderBookDecorator lobDecorator = new LimitOrderBookDecorator();
        for (Invoice invoice : invoices) {
            Counterparty target = invoice.getTarget();
            addAllOrdersToLOB(lobDecorator, target);
        }
    }

    public void addAllOrdersToLOB(LimitOrderBookDecorator lobDecorator, Counterparty counterparty) {
        List<OrderRequest> orderRequests = orderRequestRepository.findByCounterparty(counterparty);

        for (OrderRequest orderRequest : orderRequests) {
            lobDecorator.addOrder(orderRequest);
        }
    }

}
