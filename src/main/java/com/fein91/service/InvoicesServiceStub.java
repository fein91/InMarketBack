package com.fein91.service;


import com.fein91.dao.InvoiceRepository;
import com.fein91.model.Invoice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service("InvoicesServiceStub")
public class InvoicesServiceStub implements InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

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

    @Override
    public List<Invoice> getBySourceId(Long sourceId) {
        return null;
    }

    @Override
    public List<Invoice> getByTargetId(Long targetId) {
        return null;
    }

    @Override
    public Invoice getByExternalId(Long externalId) {
        return null;
    }
}
