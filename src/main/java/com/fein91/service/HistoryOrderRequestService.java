package com.fein91.service;

import com.fein91.model.HistoryOrderRequest;
import com.fein91.model.OrderRequest;
import org.springframework.transaction.annotation.Transactional;

public interface HistoryOrderRequestService {
    HistoryOrderRequest getById(Long id);

    @Transactional
    HistoryOrderRequest save(HistoryOrderRequest historyOrderRequest);

    HistoryOrderRequest convertFrom(OrderRequest orderRequest);
}
