package com.fein91.dao;

import com.fein91.model.Counterparty;
import com.fein91.model.OrderRequest;
import org.springframework.data.repository.CrudRepository;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by olta1014 on 23.05.2016.
 */
public interface OrderRequestRepository extends CrudRepository<OrderRequest, BigInteger> {

    List<OrderRequest> findByCounterpartyAndOrderSide(Counterparty counterparty, int orderSide);
}
