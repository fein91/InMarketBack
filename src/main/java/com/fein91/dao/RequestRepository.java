package com.fein91.dao;

import com.fein91.model.Counterparty;
import com.fein91.model.Request;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by olta1014 on 23.05.2016.
 */
public interface RequestRepository extends CrudRepository<Request, BigInteger> {

    List<Request> findByCounterparty(Counterparty counterparty);
}
