package com.fein91.service;


import com.fein91.dao.InvoiceRepository;
import com.fein91.model.Invoice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

import static com.fein91.Constants.ROUNDING_MODE;

@Service("InvoicesServiceStub")
public class InvoicesServiceStub implements InvoiceService {

    private final static Logger LOGGER = Logger.getLogger(InvoicesServiceStub.class.getName());

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Override
    public Invoice addInvoice(Invoice invoice) {
        return null;
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
        return invoice;
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
    public Invoice getByExternalId(String externalId) {
        return null;
    }
}
