package com.fein91.service;

import com.fein91.model.Invoice;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface InvoiceService {

    Invoice addInvoice(Invoice invoice);

    Invoice updateInvoice(Invoice invoice, BigDecimal prepaidValue);

    List<Invoice> findBySourceAndTarget(Long sourceId, Long targetId);

    List<Invoice> findBySourceOrTargetId(Long counterpartyId);

    Invoice getById(Long invoiceId);

    List<Invoice> getBySourceId(Long sourceId);

    List<Invoice> getByTargetId(Long targetId);

    Invoice getByExternalId(String externalId);

    void updateCheckedInvoices(Long counterpartyId, Map<Long, Boolean> checkedInvoices);
}
