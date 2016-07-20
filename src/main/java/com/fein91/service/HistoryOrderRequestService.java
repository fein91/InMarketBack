package com.fein91.service;

import com.fein91.model.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface HistoryOrderRequestService {
    HistoryOrderRequest getById(Long id);

    @Transactional
    HistoryOrderRequest save(HistoryOrderRequest historyOrderRequest);

    HistoryOrderRequest convertFrom(OrderRequest orderRequest);

    List<HistoryOrderRequest> getByCounterparty(Counterparty counterparty);

    List<HistoryOrderRequest> getByCounterpartyIdAndHistoryOrderType(Long counterpartyId, List<HistoryOrderType> historyOrderTypes);

    HistoryOrderRequest getByOriginOrderRequestId(Long originOrderRequestId);
}
