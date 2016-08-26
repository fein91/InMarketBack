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
    CalculationService calculationService;

    @Autowired
    OrderRequestService orderRequestServiceImpl;
    @Autowired
    InvoiceService invoiceServiceImpl;

    @Bean
    @Scope("prototype")
    public OrderBook getInstance() {
        OrderBook orderBook = new OrderBook(DEFAULT_TICK_SIZE);
        orderBook.setOrderRequestService(orderRequestServiceImpl);
        orderBook.setInvoiceService(invoiceServiceImpl);
        orderBook.setCalculationService(calculationService);
        return orderBook;
    }
}
