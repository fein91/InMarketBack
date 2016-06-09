package com.fein91.service;

import com.fein91.dao.CounterpartyRepository;
import com.fein91.model.Counterparty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("CounterPartyServiceImpl")
public class CounterPartyServiceImpl implements CounterPartyService {

    @Autowired(required = false)
    CounterpartyRepository counterpartyRepository;

    @Override
    @Transactional
    public Counterparty addCounterParty(String name) {
        Counterparty counterparty = Counterparty.of(name);
        return counterpartyRepository.save(counterparty);
    }

}
