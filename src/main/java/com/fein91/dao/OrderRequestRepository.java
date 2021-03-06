package com.fein91.dao;

import com.fein91.model.Counterparty;
import com.fein91.model.OrderRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;

/**
 * Order request repository
 */
public interface OrderRequestRepository extends CrudRepository<OrderRequest, Long> {

    List<OrderRequest> findByCounterpartyAndSide(Counterparty counterparty, int orderSide);

    @Query("SELECT r FROM OrderRequest r where r.counterparty.id = :counterPartyId")
    List<OrderRequest> findByCounterpartyId(@Param("counterPartyId") Long counterPartyId);
}