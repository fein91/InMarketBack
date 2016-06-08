package com.fein91.core.service;

import com.fein91.core.model.OrderBook;
import com.fein91.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
public class OrderBookBuilder {

    private final static double DEFAULT_TICK_SIZE = 0.1;

    @Autowired
    @Qualifier("OrderRequestServiceImpl")
    OrderRequestService orderRequestServiceImpl;
    @Autowired
    @Qualifier("InvoiceServiceImpl")
    InvoiceService invoiceServiceImpl;

    @Autowired
    @Qualifier("OrderRequestServiceStub")
    OrderRequestService orderRequestsServiceStub;
    @Autowired
    @Qualifier("InvoicesServiceStub")
    InvoiceService invoicesServiceStub;

    @Bean
    @Scope("prototype")
    public OrderBook getInstance() {
        OrderBook orderBook = new OrderBook(DEFAULT_TICK_SIZE);
        orderBook.setOrderRequestService(orderRequestServiceImpl);
        orderBook.setInvoiceService(invoiceServiceImpl);
        return orderBook;
    }

    @Bean
    @Scope("prototype")
    public OrderBook getStubInstance() {
        OrderBook orderBook = new OrderBook(DEFAULT_TICK_SIZE);
        orderBook.setOrderRequestService(orderRequestsServiceStub);
        orderBook.setInvoiceService(invoicesServiceStub);
        return orderBook;
    }

}
