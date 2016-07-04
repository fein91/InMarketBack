package com.fein91.rest;

import com.fein91.dao.InvoiceRepository;
import com.fein91.model.HistoryOrderRequest;
import com.fein91.model.Invoice;
import com.fein91.service.HistoryOrderRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/counterparties")
public class CounterpartyController {

    private final InvoiceRepository invoiceRepository;
    private final HistoryOrderRequestService historyOrderRequestService;

    @Autowired
    public CounterpartyController(InvoiceRepository invoiceRepository,
                                  @Qualifier("HistoryOrderRequestServiceImpl") HistoryOrderRequestService historyOrderRequestService) {
        this.invoiceRepository = invoiceRepository;
        this.historyOrderRequestService = historyOrderRequestService;
    }


    @RequestMapping(method = RequestMethod.GET, value = "/{sourceId}/invoicesBySource")
    public List<Invoice> getBySourceId(@PathVariable Long sourceId) {
        return invoiceRepository.findInvoicesBySourceId(sourceId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{targetId}/invoicesByTarget")
    public List<Invoice> getByTargetId(@PathVariable Long targetId) {
        return invoiceRepository.findInvoicesByTargetId(targetId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{counterpartyId}/transactionHistory")
    public List<HistoryOrderRequest> getTransactionHistory(@PathVariable Long counterpartyId) {
        return historyOrderRequestService.getByCounterpartyId(counterpartyId);
    }
}
