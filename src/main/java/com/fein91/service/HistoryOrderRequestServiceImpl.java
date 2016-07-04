package com.fein91.service;

import com.fein91.dao.HistoryOrderRequestRepository;
import com.fein91.model.Counterparty;
import com.fein91.model.HistoryOrderRequest;
import com.fein91.model.OrderRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("HistoryOrderRequestServiceImpl")
public class HistoryOrderRequestServiceImpl implements HistoryOrderRequestService {

    private final HistoryOrderRequestRepository historyOrderRequestRepository;

    @Autowired
    public HistoryOrderRequestServiceImpl(HistoryOrderRequestRepository historyOrderRequestRepository) {
        this.historyOrderRequestRepository = historyOrderRequestRepository;
    }

    @Override
    public HistoryOrderRequest getById(Long id) {
        return historyOrderRequestRepository.findOne(id);
    }

    @Override
    @Transactional
    public HistoryOrderRequest save(HistoryOrderRequest historyOrderRequest) {
        return historyOrderRequestRepository.save(historyOrderRequest);
    }

    @Override
    public HistoryOrderRequest convertFrom(OrderRequest orderRequest) {
        HistoryOrderRequest historyOrderRequest = new HistoryOrderRequest();
        historyOrderRequest.setCounterparty(orderRequest.getCounterparty());
        historyOrderRequest.setPrice(orderRequest.getPrice());
        historyOrderRequest.setQuantity(orderRequest.getQuantity());
        historyOrderRequest.setDate(orderRequest.getDate());
        historyOrderRequest.setOrderSide(orderRequest.getOrderSide());
        historyOrderRequest.setOrderType(orderRequest.getOrderType());
        return historyOrderRequest;
    }

    @Override
    public List<HistoryOrderRequest> getByCounterparty(Counterparty counterparty) {
        return historyOrderRequestRepository.findByCounterparty(counterparty);
    }

    @Override
    public List<HistoryOrderRequest> getByCounterpartyId(Long counterpartyId) {
        return historyOrderRequestRepository.findByCounterpartyId(counterpartyId);
    }

}
