package com.fein91.service;

import com.fein91.dao.InvoiceRepository;
import com.fein91.model.Counterparty;
import com.fein91.model.Invoice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;

@Service
public class InvoiceService {

    @Autowired
    InvoiceRepository invoiceRepository;

    @Transactional
    public Invoice addInvoice(BigInteger id, Counterparty from, Counterparty to, BigDecimal value) {
        Invoice invoice = new Invoice();
        invoice.setId(id);
        invoice.setCounterPartyFrom(from);
        invoice.setCounterPartyTo(to);
        invoice.setValue(value);

        return invoiceRepository.save(invoice);
    }
}
