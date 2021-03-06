package com.fein91.service;

import com.fein91.dao.HistoryOrderRequestRepository;
import com.fein91.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
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
        historyOrderRequest.setSide(orderRequest.getSide());
        historyOrderRequest.setType(HistoryOrderType.valueOf(orderRequest.getType().name()));
        historyOrderRequest.setOriginOrderRequestId(orderRequest.getId());
        return historyOrderRequest;
    }

    @Override
    public List<HistoryOrderRequest> getByCounterparty(Counterparty counterparty) {
        return historyOrderRequestRepository.findByCounterparty(counterparty);
    }

    @Override
    public List<HistoryOrderRequest> getByCounterpartyIdAndHistoryOrderType(Long counterpartyId, List<HistoryOrderType> historyOrderTypes) {
        return historyOrderRequestRepository.findByCounterpartyIdAndHistoryOrderType(counterpartyId,
                historyOrderTypes.stream()
                        .map(HistoryOrderType :: name)
                        .collect(Collectors.toList()));

    }

    @Override
    public HistoryOrderRequest getByOriginOrderRequestId(Long originOrderRequestId) {
        return historyOrderRequestRepository.findByOriginOrderRequestId(originOrderRequestId);
    }

}
