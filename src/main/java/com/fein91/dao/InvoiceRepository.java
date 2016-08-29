package com.fein91.dao;

import com.fein91.model.Counterparty;
import com.fein91.model.Invoice;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Invoice repository
 */
public interface InvoiceRepository extends CrudRepository<Invoice, Long> {

    List<Invoice> findBySource(Counterparty counterparty);

    List<Invoice> findByTarget(Counterparty counterparty);

    @Query("SELECT i FROM Invoice i where (i.source.id = :counterPartyId or i.target.id = :counterPartyId) and i.paymentDate > CURRENT_DATE")
    List<Invoice> findInvoicesBySourceOrTargetId(@Param("counterPartyId") Long counterPartyId);

    @Query("SELECT i FROM Invoice i where i.source.id = :counterPartyId and i.paymentDate > CURRENT_DATE")
    List<Invoice> findInvoicesBySourceId(@Param("counterPartyId") Long counterPartyId);

    @Query("SELECT i FROM Invoice i where i.target.id = :counterPartyId and i.paymentDate > CURRENT_DATE")
    List<Invoice> findInvoicesByTargetId(@Param("counterPartyId") Long counterPartyId);

    List<Invoice> findBySourceAndTarget(Counterparty source, Counterparty target);

    @Query("SELECT i FROM Invoice i where i.target.id = :targetId and i.source.id = :sourceId and i.paymentDate > CURRENT_DATE ORDER BY i.paymentDate")
    List<Invoice> findBySourceAndTarget(@Param("sourceId") Long sourceId, @Param("targetId") Long targetId);

    Invoice findByExternalId(String externalId);

}
