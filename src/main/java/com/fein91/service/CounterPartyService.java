package com.fein91.service;

import com.fein91.core.model.OrderSide;
import com.fein91.dao.CounterpartyRepository;
import com.fein91.model.Counterparty;
import com.fein91.model.ProposalInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;

@Service
public class CounterPartyService {
    @Autowired
    CounterpartyRepository counterpartyRepository;
    @Autowired
    OrderRequestService orderRequestService;

    @Transactional
    public Counterparty addCounterParty(BigInteger id, String name) {
        Counterparty counterparty = new Counterparty();
        counterparty.setId(id);
        counterparty.setName(name);

        return counterpartyRepository.save(counterparty);
    }

    @Deprecated
    public ProposalInfo calculateProposalInfo(BigInteger counterpartyId) {
        BigDecimal asksSum = orderRequestService.findLimitOrderRequestsToTradeSum(counterpartyId, OrderSide.ASK);
        BigDecimal bidsSum = orderRequestService.findLimitOrderRequestsToTradeSum(counterpartyId, OrderSide.BID);

        return new ProposalInfo(asksSum, bidsSum);
    }
}
