package com.fein91.service;

import com.fein91.dao.InvoiceRepository;
import com.fein91.model.Counterparty;
import com.fein91.model.Invoice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

@Service("InvoiceServiceImpl")
public class InvoiceServiceImpl implements InvoiceService {

    private final static Logger LOGGER = Logger.getLogger(InvoiceServiceImpl.class.getName());

    @Autowired
    InvoiceRepository invoiceRepository;

    @Override
    @Transactional
    public Invoice addInvoice(Counterparty source, Counterparty target, BigDecimal value, BigDecimal prepaidValue) {
        Invoice invoice = new Invoice();
        invoice.setSource(source);
        invoice.setTarget(target);
        invoice.setValue(value);
        invoice.setPrepaidValue(prepaidValue);

        return invoiceRepository.save(invoice);
    }

    @Override
    public Invoice updateInvoice(Invoice invoice, BigDecimal prepaidValue) {
        invoice.setPrepaidValue(invoice.getPrepaidValue().add(prepaidValue));

        LOGGER.info(invoice + " prepaid value was updated to: " + prepaidValue);
        return invoiceRepository.save(invoice);
    }

    @Override
    public List<Invoice> findBySourceAndTarget(Long sourceId, Long targetId) {
        return invoiceRepository.findBySourceAndTarget(sourceId, targetId);
    }

    @Override
    public Invoice getById(Long invoiceId) {
        return invoiceRepository.findOne(invoiceId);
    }
}
