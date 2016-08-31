package com.fein91.rest;

import com.fein91.model.*;
import com.fein91.rest.exception.ImportExportException;
import com.fein91.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/counterparties")
public class CounterpartyController {

    private final static Logger LOGGER = Logger.getLogger(CounterpartyController.class.getName());

    private final InvoiceService invoiceService;
    private final HistoryOrderRequestService historyOrderRequestService;
    private final CounterPartyService counterPartyService;
    private final OrderRequestService orderRequestService;
    private final ImportExportService importExportService;

    @Autowired
    public CounterpartyController(InvoiceService invoiceService,
                                  HistoryOrderRequestService historyOrderRequestService,
                                  CounterPartyService counterPartyService,
                                  OrderRequestService orderRequestService,
                                  ImportExportService importExportService) {
        this.invoiceService = invoiceService;
        this.historyOrderRequestService = historyOrderRequestService;
        this.counterPartyService = counterPartyService;
        this.orderRequestService = orderRequestService;
        this.importExportService = importExportService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{counterpartyId}/invoices")
    public List<Invoice> getInvoicesBySourceOrTargetId(@PathVariable Long counterpartyId) {
        LOGGER.info("Looking for all invoices by counterpartyID: " + counterpartyId);
        return invoiceService.findBySourceOrTargetId(counterpartyId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{sourceId}/invoicesBySource")
    public List<Invoice> getInvoicesBySourceId(@PathVariable Long sourceId) {
        LOGGER.info("Looking for invoices by sourceId: " + sourceId);
        return invoiceService.getBySourceId(sourceId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{targetId}/invoicesByTarget")
    public List<Invoice> getInvoicesByTargetId(@PathVariable Long targetId) {
        LOGGER.info("Looking for invoices by target: " + targetId);
        return invoiceService.getByTargetId(targetId);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{counterpartyId}/updateCheckedInvoices")
    public ResponseEntity updateCheckedInvoices(@RequestBody Map<Long, Boolean> checkedInvoices,
                                                @PathVariable Long counterpartyId) {
        LOGGER.info("Updating checked invoices: " + checkedInvoices);
        invoiceService.updateCheckedInvoices(counterpartyId, checkedInvoices);
        return new ResponseEntity<>("{}", HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{counterpartyId}/transactionHistory")
    public List<HistoryOrderRequest> getTransactionHistory(@PathVariable Long counterpartyId) {
        return historyOrderRequestService.getByCounterpartyIdAndHistoryOrderType(counterpartyId,
                Arrays.asList(HistoryOrderType.MARKET, HistoryOrderType.EXECUTED_LIMIT));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{counterpartyId}/orderRequests")
    public List<OrderRequest> getOrderRequestsByCounterpartyId(@PathVariable Long counterpartyId) {
        return orderRequestService.getByCounterpartyId(counterpartyId);
    }

    @RequestMapping(value = "/{counterpartyId}/importInvoices", method = RequestMethod.POST)
    public ResponseEntity uploadFile(@PathVariable Long counterpartyId,
                                     @RequestParam("file") MultipartFile file) throws ImportExportException {
        importExportService.importExcel(counterpartyId, file);
        return new ResponseEntity<>("{}", HttpStatus.OK);
    }

    @RequestMapping(value = "/{counterpartyId}/exportInvoicesBySource", method = RequestMethod.POST, produces = "text/csv")
    @ResponseBody
    public ResponseEntity<String> exportInvoicesBySource(@PathVariable Long counterpartyId,
                                                 final HttpServletResponse response) {
        response.setHeader("Content-Disposition", "attachment");
        response.setContentType("text/csv");

        return new ResponseEntity<>(importExportService.exportCsvBySource(counterpartyId), HttpStatus.OK);
    }

    @RequestMapping(value = "/{counterpartyId}/exportInvoicesByTarget", method = RequestMethod.POST, produces = "text/csv")
    @ResponseBody
    public ResponseEntity<String> exportInvoicesByTarget(@PathVariable Long counterpartyId,
                                                         final HttpServletResponse response) {
        response.setHeader("Content-Disposition", "attachment");
        response.setContentType("text/csv");

        return new ResponseEntity<>(importExportService.exportCsvByTarget(counterpartyId), HttpStatus.OK);
    }

    @ExceptionHandler(ImportExportException.class)
    public ResponseEntity<ErrorResponse> importExportExceptionHandler(ImportExportException ex) {
        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setMessage(ex.getLocalizedMsg());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
