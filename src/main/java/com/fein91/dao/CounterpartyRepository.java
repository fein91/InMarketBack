package com.fein91.dao;

import com.fein91.model.Counterparty;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by olta1014 on 23.05.2016.
 */
public interface CounterpartyRepository extends CrudRepository<Counterparty, BigInteger> {

}
