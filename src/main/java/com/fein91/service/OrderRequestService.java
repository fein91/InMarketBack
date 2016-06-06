package com.fein91.service;

import com.fein91.core.model.OrderSide;
import com.fein91.model.OrderRequest;
import com.fein91.model.OrderResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public interface OrderRequestService {
    List<OrderRequest> getByCounterpartyId(BigInteger counterpartyId);

    OrderRequest addOrderRequest(OrderRequest orderRequest);

    @Transactional
    OrderResult processOrderRequest(OrderRequest orderRequest);

    @Transactional
    OrderResult calculateOrderRequest(OrderRequest orderRequest);

    @Transactional
    List<OrderRequest> findLimitOrderRequestsToTrade(BigInteger counterpartyId, OrderSide orderSide);

    @Transactional
    void removeOrderRequest(BigInteger orderId);

    @Transactional
    OrderRequest updateOrderRequest(BigInteger orderId, BigDecimal qty);
}
