package com.fein91.service;

import static com.fein91.rest.exception.ExceptionMessages.EXCEPTION_WHILE_IMPORT_OCCURRED;
import static org.springframework.util.StringUtils.hasText;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fein91.model.Invoice;
import com.fein91.rest.exception.ImportExportException;

@Service
public class ImportExportService {

    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    private final static Logger LOGGER = Logger.getLogger(ImportExportService.class.getName());


    final CounterPartyService counterPartyService;
    final InvoiceService invoiceService;

    @Autowired
    public ImportExportService(CounterPartyService counterPartyService,
                               InvoiceService invoiceService) {
        this.counterPartyService = counterPartyService;
        this.invoiceService = invoiceService;
    }

    public void importExcel(Long counterpartyId, MultipartFile file) throws ImportExportException {
        Workbook workbook = null;
        try {
            workbook = new XSSFWorkbook(file.getInputStream());
            for (Sheet sheet : workbook) {
                for (Row row : sheet) {
                    int cellNum = 0;
                    String externalInvoiceId = row.getCell(cellNum++).getStringCellValue();
                    String source = row.getCell(cellNum++).getStringCellValue();
                    double amount = row.getCell(cellNum++).getNumericCellValue();
                    Date paymentDate = parseDate(row.getCell(cellNum++));

                    if (!hasText(externalInvoiceId)) continue;

                    Invoice existedInvoice = invoiceService.getByExternalId(externalInvoiceId);

                    if (existedInvoice == null) {
                        Invoice invoice = new Invoice();
                        invoice.setSource(counterPartyService.getByNameOrAdd(source));
                        invoice.setTarget(counterPartyService.getById(counterpartyId));
                        invoice.setValue(BigDecimal.valueOf(amount));
                        invoice.setPaymentDate(paymentDate);
                        invoice.setExternalId(externalInvoiceId);
                        invoice.setSourceChecked(true);
                        invoice.setTargetChecked(true);

                        invoiceService.addInvoice(invoice);
                        LOGGER.info("Invoice imported: " + invoice);
                    } else {
                        LOGGER.warning("Invoice with externalId: " + externalInvoiceId + " already exist");
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.severe("Error occurred while import, " + e);
            throw new ImportExportException(EXCEPTION_WHILE_IMPORT_OCCURRED.getMessage(), EXCEPTION_WHILE_IMPORT_OCCURRED.getLocalizedMessage());
        }
    }

    private Date parseDate(Cell cell) throws ParseException {
        String cellValue = new DataFormatter().formatCellValue(cell);
        return hasText(cellValue) ? FORMAT.parse(cellValue) : null;
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
