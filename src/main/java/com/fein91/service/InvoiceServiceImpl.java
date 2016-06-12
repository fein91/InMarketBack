package com.fein91.service;

import com.fein91.dao.InvoiceRepository;
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

    private final InvoiceRepository invoiceRepository;

    @Autowired
    public InvoiceServiceImpl(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    @Transactional
    public Invoice addInvoice(Invoice invoice) {
        return invoiceRepository.save(invoice);
    }

    @Override
    public Invoice updateInvoice(Invoice invoice, BigDecimal prepaidValue) {
        invoice.setPrepaidValue(invoice.getPrepaidValue().add(prepaidValue));

        LOGGER.info("Invoice with id: " + invoice.getId() + " prepaid value was updated to: " + prepaidValue);
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
