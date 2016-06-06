package com.fein91.service;

import com.fein91.model.Counterparty;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;

public interface CounterPartyService {
    @Transactional
    Counterparty addCounterParty(BigInteger id, String name);
}
