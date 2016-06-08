package com.fein91.service;

import com.fein91.model.Counterparty;
import com.fein91.model.Invoice;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public interface InvoiceService {
    @Transactional
    Invoice addInvoice(BigInteger id, Counterparty source, Counterparty target, BigDecimal value, BigDecimal prepaidValue);

    Invoice updateInvoice(Invoice invoice, BigDecimal prepaidValue);

    List<Invoice> findBySourceAndTarget(BigInteger sourceId, BigInteger targetId);

    Invoice getById(BigInteger invoiceId);
}
