package com.fein91.service;

import com.fein91.dao.InvoiceRepository;
import com.fein91.model.Invoice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

import static com.fein91.Constants.ROUNDING_MODE;

@Service
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
        BigDecimal unpaidInvoiceValue = invoice.getValue().subtract(prepaidValue).setScale(2, ROUNDING_MODE);
        if (unpaidInvoiceValue.signum() < 0) {
            throw new IllegalStateException("Invoice prepaid value can't be greater than invoice value");
        } else if (unpaidInvoiceValue.signum() == 0) {
            invoice.setProcessed(true);
        }
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

    @Override
     public List<Invoice> getBySourceId(Long sourceId) {
        return invoiceRepository.findInvoicesBySourceId(sourceId);
    }

    @Override
    public List<Invoice> getByTargetId(Long targetId) {
        return invoiceRepository.findInvoicesByTargetId(targetId);
    }

    @Override
    public Invoice getByExternalId(String externalId) {
        return invoiceRepository.findByExternalId(externalId);
    }
}
