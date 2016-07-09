package com.fein91.dao;

import com.fein91.model.Counterparty;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

import java.math.BigInteger;
import java.util.List;

/**
 * Counter party repository
 */
public interface CounterpartyRepository extends CrudRepository<Counterparty, Long> {

    Counterparty findByLogin(String loginName);

    Counterparty findByName(String name);
}
