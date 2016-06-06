package com.fein91.service;

import com.fein91.dao.InvoiceRepository;
import com.fein91.model.Counterparty;
import com.fein91.model.Invoice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Service
public class InvoiceService {

    @Autowired
    InvoiceRepository invoiceRepository;

    @Transactional
    public Invoice addInvoice(BigInteger id, Counterparty source, Counterparty target, BigDecimal value, BigDecimal prepaidValue) {
        Invoice invoice = new Invoice();
        invoice.setId(id);
        invoice.setSource(source);
        invoice.setTarget(target);
        invoice.setValue(value);
        invoice.setPrepaidValue(prepaidValue);

        return invoiceRepository.save(invoice);
    }

    public Invoice updateInvoice(Invoice invoice, BigDecimal prepaidValue) {
        invoice.setPrepaidValue(invoice.getPrepaidValue().add(prepaidValue));
        return invoiceRepository.save(invoice);
    }

    public List<Invoice> findBySourceAndTarget(BigInteger sourceId, BigInteger targetId) {
        return invoiceRepository.findBySourceAndTarget(sourceId, targetId);
    }

    public Invoice getById(BigInteger invoiceId) {
        return invoiceRepository.findOne(invoiceId);
    }
}
