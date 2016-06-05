package com.fein91.core.service;

import com.fein91.core.model.OrderBook;
import com.fein91.service.OrderRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
public class OrderBookBuilder {

    private final static double DEFAULT_TICK_SIZE = 0.1;

    @Autowired
    OrderRequestService orderRequestService;

    @Bean
    @Scope("prototype")
    public OrderBook getInstance() {
        OrderBook orderBook = new OrderBook(DEFAULT_TICK_SIZE);
        orderBook.setOrderRequestService(orderRequestService);
        return orderBook;
    }
}
