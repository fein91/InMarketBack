package com.fein91.service;

import com.fein91.dao.CounterpartyRepository;
import com.fein91.model.Counterparty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;

@Service
public class CounterPartyService {
    @Autowired
    CounterpartyRepository counterpartyRepository;

    @Transactional
    public Counterparty addCounterParty(BigInteger id, String name) {
        Counterparty counterparty = new Counterparty();
        counterparty.setId(id);
        counterparty.setName(name);

        return counterpartyRepository.save(counterparty);
    }
}
