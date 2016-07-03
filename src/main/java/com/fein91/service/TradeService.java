package com.fein91.service;

import com.fein91.model.Trade;
import com.fein91.dao.TradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TradeService {

    private final TradeRepository tradeRepository;

    @Autowired
    public TradeService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    public Trade getById(Long id) {
        return tradeRepository.findOne(id);
    }

    @Transactional
    public Trade save(Trade trade) {
        return tradeRepository.save(trade);
    }
}
