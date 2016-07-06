package com.fein91.service;

import com.fein91.dao.HistoryOrderRequestRepository;
import com.fein91.model.Counterparty;
import com.fein91.model.HistoryOrderRequest;
import com.fein91.model.OrderRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.logging.Logger;

@Service("HistoryOrderRequestServiceImpl")
public class HistoryOrderRequestServiceImpl implements HistoryOrderRequestService {

    private final static Logger LOGGER = Logger.getLogger(HistoryOrderRequestServiceImpl.class.getName());

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
        LOGGER.info("Saving history order request: " + historyOrderRequest);
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
        historyOrderRequest.setOriginOrderRequestId(orderRequest.getId());
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

    @Override
    public HistoryOrderRequest getByOriginOrderRequestId(Long originOrderRequestId) {
        return historyOrderRequestRepository.findByOriginOrderRequestId(originOrderRequestId);
    }

}
