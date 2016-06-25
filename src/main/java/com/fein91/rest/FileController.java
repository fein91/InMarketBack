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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Date;

/**
 *
 */
@CrossOrigin
@RestController
public class FileController {
    private final InvoiceService invoiceService;

    @Autowired
    public FileController(@Qualifier("InvoiceServiceImpl") InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public ResponseEntity uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());

            for (Sheet sheet : workbook) {
                for (Row row : sheet) {
                    String source = row.getCell(0).getStringCellValue();
                    String target = row.getCell(1).getStringCellValue();
                    double amount = row.getCell(2).getNumericCellValue();
                    String stringCellValue = row.getCell(3).getStringCellValue();

                    Invoice invoice = new Invoice();
                    invoice.setSource(Counterparty.of(source));
                    invoice.setTarget(Counterparty.of(target));
                    invoice.setValue(BigDecimal.valueOf(amount));
                    invoice.setPaymentDate(new Date());
                    //TODO: you know what to do
//                    invoiceService.addInvoice();
                }
            }
        } catch (Exception e) {
            return new ResponseEntity<>("{}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("{}", HttpStatus.OK);
    }
}
