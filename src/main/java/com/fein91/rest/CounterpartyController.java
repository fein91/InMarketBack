package com.fein91.rest;

import com.fein91.dao.InvoiceRepository;
import com.fein91.model.Invoice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/counterparties")
public class CounterpartyController {

    @Autowired
    InvoiceRepository invoiceRepository;

    @RequestMapping(method = RequestMethod.GET, value = "/{sourceId}/invoicesBySource")
    public List<Invoice> getBySourceId(@PathVariable BigInteger sourceId) {
        return invoiceRepository.findInvoicesBySourceId(sourceId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{targetId}/invoicesByTarget")
    public List<Invoice> getByTargetId(@PathVariable BigInteger targetId) {
        return invoiceRepository.findInvoicesByTargetId(targetId);
    }
}
