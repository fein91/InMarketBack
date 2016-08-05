package com.fein91.service;

import com.fein91.core.model.Order;
import com.fein91.core.model.OrderSide;
import com.fein91.model.OrderRequest;
import com.fein91.model.OrderResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;

@Service("OrderRequestServiceStub")
public class OrderRequestServiceStub implements OrderRequestService {
    @Override
    public List<OrderRequest> getByCounterpartyId(Long counterpartyId) {
        return null;
    }

    @Override
    public OrderRequest getById(Long id) {
        return null;
    }

    @Override
    public OrderRequest update(OrderRequest orderRequest) {
        return null;
    }

    @Override
    public OrderRequest save(OrderRequest orderRequest) {
        return null;
    }

    @Override
    public OrderRequest saveOrder(Order order) {
        return null;
    }

    @Override
    public OrderResult process(OrderRequest orderRequest) {
        return null;
    }

    @Override
    public OrderResult calculate(OrderRequest orderRequest) {
        return null;
    }

    @Override
    public void removeById(Long orderId) {

    }

    @Override
    public OrderRequest update(Long orderId, BigDecimal qty) {
        return null;
    }
}
