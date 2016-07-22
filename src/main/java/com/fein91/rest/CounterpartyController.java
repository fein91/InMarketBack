package com.fein91.rest;

import com.fein91.model.HistoryOrderRequest;
import com.fein91.model.HistoryOrderType;
import com.fein91.model.Invoice;
import com.fein91.service.CounterPartyService;
import com.fein91.service.HistoryOrderRequestService;
import com.fein91.service.InvoiceService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/counterparties")
public class CounterpartyController {

    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    private final static Logger LOGGER = Logger.getLogger(CounterpartyController.class.getName());

    private final InvoiceService invoiceService;
    private final HistoryOrderRequestService historyOrderRequestService;
    private final CounterPartyService counterPartyService;

    @Autowired
    public CounterpartyController(@Qualifier("InvoiceServiceImpl") InvoiceService invoiceService,
                                  @Qualifier("HistoryOrderRequestServiceImpl") HistoryOrderRequestService historyOrderRequestService,
                                  CounterPartyService counterPartyService) {
        this.invoiceService = invoiceService;
        this.historyOrderRequestService = historyOrderRequestService;
        this.counterPartyService = counterPartyService;
    }


    @RequestMapping(method = RequestMethod.GET, value = "/{sourceId}/invoicesBySource")
    public List<Invoice> getBySourceId(@PathVariable Long sourceId) {
        LOGGER.info("Looking for invoices by sourceId: " + sourceId);
        return invoiceService.getBySourceId(sourceId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{targetId}/invoicesByTarget")
    public List<Invoice> getByTargetId(@PathVariable Long targetId) {
        LOGGER.info("Looking for invoices by target: " + targetId);
        return invoiceService.getByTargetId(targetId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{counterpartyId}/transactionHistory")
    public List<HistoryOrderRequest> getTransactionHistory(@PathVariable Long counterpartyId) {
        return historyOrderRequestService.getByCounterpartyIdAndHistoryOrderType(counterpartyId,
                Arrays.asList(HistoryOrderType.MARKET, HistoryOrderType.EXECUTED_LIMIT));
    }

    @RequestMapping(value = "/{counterpartyId}/importInvoices", method = RequestMethod.POST)
    public ResponseEntity uploadFile(@PathVariable Long counterpartyId,
                                     @RequestParam("file") MultipartFile file) {
        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());

            for (Sheet sheet : workbook) {
                for (Row row : sheet) {
                    int rowNum = 0;
                    String source = row.getCell(rowNum++).getStringCellValue();
                    double amount = row.getCell(rowNum++).getNumericCellValue();
                    String stringDate = row.getCell(rowNum++).getStringCellValue();

                    Invoice invoice = new Invoice();
                    invoice.setSource(counterPartyService.getByNameOrAdd(source));
                    invoice.setTarget(counterPartyService.getById(counterpartyId));
                    invoice.setValue(BigDecimal.valueOf(amount));
                    invoice.setPaymentDate(FORMAT.parse(stringDate));

                    invoiceService.addInvoice(invoice);
                }
            }
        } catch (Exception e) {
            return new ResponseEntity<>("{}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("{}", HttpStatus.OK);
    }

    @RequestMapping(value = "/{counterpartyId}//exportInvoices", method = RequestMethod.POST, produces = "text/csv")
    @ResponseBody
    public ResponseEntity<String> exportInvoices(@PathVariable Long counterpartyId,
                                                 final HttpServletResponse response) throws IOException {
        response.setHeader("Content-Disposition", "attachment");
        response.setContentType("text/csv");
        List<Invoice> invoices = invoiceService.getByTargetId(counterpartyId);
        StringBuilder stringBuilder = new StringBuilder();
        invoices.forEach(invoice -> stringBuilder
                .append(invoice.getSource().getName()).append(",")
                .append(invoice.getValue().subtract(invoice.getPrepaidValue())).append(",")
                .append(invoice.getPrepaidValue()).append(",")
                .append(FORMAT.format(invoice.getPaymentDate())).append("\n"));
        String csvResponse = stringBuilder.toString();
        return new ResponseEntity<>(csvResponse, HttpStatus.OK);
    }
}
