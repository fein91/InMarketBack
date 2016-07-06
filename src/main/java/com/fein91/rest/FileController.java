package com.fein91.rest;

import com.fein91.model.Counterparty;
import com.fein91.model.Invoice;
import com.fein91.service.InvoiceService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;

/**
 *
 */
@CrossOrigin
@RestController
public class FileController {
    private final InvoiceService invoiceService;
    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    @Autowired
    public FileController(@Qualifier("InvoiceServiceImpl") InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public ResponseEntity uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());

            String loggedInUser = SecurityContextHolder.getContext().getAuthentication().getName();
            for (Sheet sheet : workbook) {
                for (Row row : sheet) {
                    int rowNum = 0;
                    String source = row.getCell(rowNum++).getStringCellValue();
                    double amount = row.getCell(rowNum++).getNumericCellValue();
                    String stringDate = row.getCell(rowNum++).getStringCellValue();

                    Invoice invoice = new Invoice();
                    invoice.setSource(Counterparty.of(source));
                    invoice.setTarget(Counterparty.of(loggedInUser));
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

    @RequestMapping(value = "/exportInvoices", method = RequestMethod.POST, produces = "text/csv")
    @ResponseBody
    public ResponseEntity<String> exportInvoices(final HttpServletResponse response) throws IOException {
        response.setHeader("Content-Disposition", "attachment");
        response.setContentType("text/csv");
        String csvResponse = "1,2,3" + "\n" + "4,5,6";
        return new ResponseEntity<>(csvResponse, HttpStatus.OK);

    }
}
