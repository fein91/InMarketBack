package com.fein91.service;

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
    public OrderRequest addOrderRequest(OrderRequest orderRequest) {
        return null;
    }

    @Override
    public OrderResult processOrderRequest(OrderRequest orderRequest) {
        return null;
    }

    @Override
    public OrderResult calculateOrderRequest(OrderRequest orderRequest) {
        return null;
    }

    @Override
    public Set<OrderRequest> findLimitOrderRequestsToTrade(OrderRequest orderRequest) {
        return null;
    }

    @Override
    public void removeOrderRequest(Long orderId) {

    }

    @Override
    public OrderRequest updateOrderRequest(Long orderId, BigDecimal qty) {
        return null;
    }
}
