package com.fein91.service;

import com.fein91.core.model.OrderSide;
import com.fein91.model.OrderRequest;
import com.fein91.model.OrderResult;
import com.fein91.rest.exception.OrderRequestException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface OrderRequestService {
    List<OrderRequest> getByCounterpartyId(Long counterpartyId);

    OrderRequest addOrderRequest(OrderRequest orderRequest);

    @Transactional
    OrderResult processOrderRequest(OrderRequest orderRequest) throws OrderRequestException;

    @Transactional
    OrderResult calculateOrderRequest(OrderRequest orderRequest) throws OrderRequestException;

    @Transactional
    List<OrderRequest> findLimitOrderRequestsToTrade(Long counterpartyId, OrderSide orderSide) throws OrderRequestException;

    @Transactional
    void removeOrderRequest(Long orderId);

    @Transactional
    OrderRequest updateOrderRequest(Long orderId, BigDecimal qty);
}
