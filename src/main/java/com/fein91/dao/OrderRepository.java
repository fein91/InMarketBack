package com.fein91.dao;

import com.fein91.model.Counterparty;
import com.fein91.model.Request;
import org.springframework.data.repository.Repository;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by olta1014 on 23.05.2016.
 */
public interface OrderRepository extends Repository<Request, BigInteger> {

    List<Request> findAll();

    Request findById(BigInteger id);

    List<Request> findByCounterparty(Counterparty counterparty);
}
