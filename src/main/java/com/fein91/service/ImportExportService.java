package com.fein91.service;

import com.fein91.model.Invoice;
import com.fein91.rest.exception.ImportExportException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Logger;

@Service
public class ImportExportService {

    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    private final static Logger LOGGER = Logger.getLogger(ImportExportService.class.getName());


    final CounterPartyService counterPartyService;
    final InvoiceService invoiceService;

    @Autowired
    public ImportExportService(CounterPartyService counterPartyService,
                               @Qualifier("InvoiceServiceImpl") InvoiceService invoiceService) {
        this.counterPartyService = counterPartyService;
        this.invoiceService = invoiceService;
    }

    public void importExcel(Long counterpartyId, MultipartFile file) throws ImportExportException {
        Workbook workbook = null;
        try {
            workbook = new XSSFWorkbook(file.getInputStream());
            for (Sheet sheet : workbook) {
                for (Row row : sheet) {
                    int rowNum = 0;
                    Long externalInvoiceId = new Double(row.getCell(rowNum++).getNumericCellValue()).longValue();
                    String source = row.getCell(rowNum++).getStringCellValue();
                    double amount = row.getCell(rowNum++).getNumericCellValue();
                    String stringDate = row.getCell(rowNum++).getStringCellValue();

                    Invoice existedInvoice = invoiceService.getByExternalId(externalInvoiceId);

                    if (existedInvoice == null) {
                        Invoice invoice = new Invoice();
                        invoice.setSource(counterPartyService.getByNameOrAdd(source));
                        invoice.setTarget(counterPartyService.getById(counterpartyId));
                        invoice.setValue(BigDecimal.valueOf(amount));
                        invoice.setPaymentDate(FORMAT.parse(stringDate));
                        invoice.setExternalId(externalInvoiceId);

                        invoiceService.addInvoice(invoice);
                        LOGGER.info("Invoice imported: " + invoice);
                    } else {
                        LOGGER.warning("Invoice with externalId: " + externalInvoiceId + " already exist");
                    }
                }
            }
        } catch (IOException | ParseException e) {
            throw new ImportExportException("exception while import/export occured");
        }
    }

    public String exportCsvBySource(Long counterpartyId) {
        return convertInvoicesToString(invoiceService.getBySourceId(counterpartyId));
    }

    public String exportCsvByTarget(Long counterpartyId) {
        return convertInvoicesToString(invoiceService.getByTargetId(counterpartyId));
    }

    private String convertInvoicesToString(List<Invoice> invoices) {
        StringBuilder stringBuilder = new StringBuilder();
        invoices.forEach(invoice -> stringBuilder
                .append(invoice.getSource().getName()).append(",")
                .append(invoice.getTarget().getName()).append(",")
                .append(invoice.getValue().subtract(invoice.getPrepaidValue())).append(",")
                .append(invoice.getPrepaidValue()).append(",")
                .append(FORMAT.format(invoice.getPaymentDate())).append("\n"));
        return stringBuilder.toString();
    }
}
