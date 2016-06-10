package com.fein91.service;


import com.fein91.dao.InvoiceRepository;
import com.fein91.model.Counterparty;
import com.fein91.model.Invoice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service("InvoicesServiceStub")
public class InvoicesServiceStub implements InvoiceService {

    @Autowired
    InvoiceRepository invoiceRepository;

    @Override
    @Deprecated
    public Invoice addInvoice(Counterparty source, Counterparty target, BigDecimal value, BigDecimal prepaidValue) {
        return null;
    }

    @Override
    public Invoice addInvoice(Invoice invoice) {
        return null;
    }

    @Override
    public Invoice updateInvoice(Invoice invoice, BigDecimal prepaidValue) {
        return null;
    }

    @Override
    public List<Invoice> findBySourceAndTarget(Long sourceId, Long targetId) {
        return invoiceRepository.findBySourceAndTarget(sourceId, targetId);
    }

    @Override
    public Invoice getById(Long invoiceId) {
        return null;
    }
}
