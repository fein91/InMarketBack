package com.fein91.service;

import com.fein91.dao.CounterpartyRepository;
import com.fein91.model.Counterparty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CounterPartyServiceImpl implements CounterPartyService {

    private final CounterpartyRepository counterpartyRepository;

    @Autowired
    public CounterPartyServiceImpl(CounterpartyRepository counterpartyRepository) {
        this.counterpartyRepository = counterpartyRepository;
    }

    @Override
    @Transactional
    public Counterparty addCounterParty(String name) {
        Counterparty counterparty = Counterparty.of(name);
        return counterpartyRepository.save(counterparty);
    }

    @Override
    public Counterparty getById(long id) {
        return counterpartyRepository.findOne(id);
    }

    @Override
    public Counterparty getByName(String name) {
        return counterpartyRepository.findByName(name);
    }

    @Override
    public Counterparty getByNameOrAdd(String name) {
        Counterparty counterparty = getByName(name);
        if (counterparty != null) {
            return counterparty;
        }
        return addCounterParty(name);
    }

}
