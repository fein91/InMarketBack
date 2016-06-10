package com.fein91.service;

import com.fein91.model.Counterparty;
import com.fein91.model.Invoice;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface InvoiceService {

    @Deprecated
    Invoice addInvoice(Counterparty source, Counterparty target, BigDecimal value, BigDecimal prepaidValue);

    Invoice addInvoice(Invoice invoice);

    Invoice updateInvoice(Invoice invoice, BigDecimal prepaidValue);

    List<Invoice> findBySourceAndTarget(Long sourceId, Long targetId);

    Invoice getById(Long invoiceId);
}
