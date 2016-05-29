package com.fein91.dao;

import com.fein91.model.Counterparty;
import com.fein91.model.Invoice;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by olta1014 on 23.05.2016.
 */
public interface InvoiceRepository extends CrudRepository<Invoice, BigInteger> {

    List<Invoice> findBySource(Counterparty counterparty);

    List<Invoice> findByTarget(Counterparty counterparty);

    @Query("SELECT i FROM Invoice i where i.source.id = :counterPartyId")
    List<Invoice> findInvoicesBySourceId(@Param("counterPartyId") BigInteger counterPartyId);

    @Query("SELECT i FROM Invoice i where i.target.id = :counterPartyId")
    List<Invoice> findInvoicesByTargetId(@Param("counterPartyId") BigInteger counterPartyId);

    Invoice findBySourceAndTarget(Counterparty source, Counterparty target);
}
