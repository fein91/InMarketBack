package com.fein91.service;

import com.fein91.dao.HistoryOrderRequestRepository;
import com.fein91.model.Counterparty;
import com.fein91.model.HistoryOrderRequest;
import com.fein91.model.OrderRequest;
import com.fein91.model.OrderType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("HistoryOrderRequestServiceStub")
public class HistoryOrderRequestServiceStub implements HistoryOrderRequestService {

    private final HistoryOrderRequestRepository historyOrderRequestRepository;

    @Autowired
    public HistoryOrderRequestServiceStub(HistoryOrderRequestRepository historyOrderRequestRepository) {
        this.historyOrderRequestRepository = historyOrderRequestRepository;
    }

    @Override
    public HistoryOrderRequest getById(Long id) {
        return historyOrderRequestRepository.findOne(id);
    }

    @Override
    public HistoryOrderRequest save(HistoryOrderRequest historyOrderRequest) {
        return null;
    }

    @Override
    public HistoryOrderRequest convertFrom(OrderRequest orderRequest) {
        return null;
    }

    @Override
    public List<HistoryOrderRequest> getByCounterparty(Counterparty counterparty) {
        return historyOrderRequestRepository.findByCounterparty(counterparty);
    }

    @Override
    public List<HistoryOrderRequest> getByCounterpartyIdAndOrderType(Long counterpartyId, OrderType orderType) {
        return historyOrderRequestRepository.findByCounterpartyIdAndOrderType(counterpartyId, orderType.getId());
    }

    @Override
    public HistoryOrderRequest getByOriginOrderRequestId(Long originOrderRequestId) {
        return historyOrderRequestRepository.findByOriginOrderRequestId(originOrderRequestId);
    }
}
