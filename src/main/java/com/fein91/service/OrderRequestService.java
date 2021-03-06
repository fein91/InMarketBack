package com.fein91.service;

import com.fein91.core.model.Order;
import com.fein91.core.model.OrderSide;
import com.fein91.model.OrderRequest;
import com.fein91.model.OrderResult;
import com.fein91.rest.exception.OrderRequestException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface OrderRequestService {
    List<OrderRequest> getByCounterpartyId(Long counterpartyId);

    OrderRequest getById(Long id);

    @Transactional
    OrderRequest update(OrderRequest orderRequest);

    OrderRequest save(OrderRequest orderRequest);

    @Transactional
    OrderResult process(OrderRequest orderRequest) throws OrderRequestException;

    @Transactional
    void calculate(OrderRequest orderRequest) throws OrderRequestException;

    @Transactional
    void removeById(Long orderId);

    @Transactional
    OrderRequest update(Long orderId, BigDecimal qty);
}
