package com.fein91.service;

import com.fein91.InMarketApplication;
import com.fein91.model.Counterparty;
import com.fein91.model.Invoice;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.List;

import static java.math.BigDecimal.ZERO;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = InMarketApplication.class)
public class ImportExportTest {

    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    @Autowired
    ImportExportService importExportService;
    @Autowired
    CounterPartyService counterPartyService;
    @Autowired
    InvoiceService invoiceService;

    @Test
    @Transactional
    @Rollback
    public void testImport() throws Exception {
        Counterparty counterparty = counterPartyService.addCounterParty("import test");

        MultipartFile file = getMultipartFile("./src/main/resources/import_test_data/", "tc1.xlsx", "application/vnd.ms-excel");
        importExportService.importExcel(counterparty.getId(), file);

        List<Invoice> supplierInvoices = invoiceService.getByTargetId(counterparty.getId());
        Assert.assertEquals(3, supplierInvoices.size());
    }

    @Test
    @Transactional
    @Rollback
    public void testImportSameData() throws Exception {
        Counterparty counterparty = counterPartyService.addCounterParty("import test");

        MultipartFile file = getMultipartFile("./src/main/resources/import_test_data/", "tc1.xlsx", "application/vnd.ms-excel");
        importExportService.importExcel(counterparty.getId(), file);

        List<Invoice> supplierInvoices = invoiceService.getByTargetId(counterparty.getId());
        Assert.assertEquals(3, supplierInvoices.size());

        importExportService.importExcel(counterparty.getId(), file);

        supplierInvoices = invoiceService.getByTargetId(counterparty.getId());
        Assert.assertEquals(3, supplierInvoices.size());
    }

    @Test
    @Transactional
    @Rollback
    public void testExport() throws Exception {
        Counterparty supplier1 = counterPartyService.addCounterParty("supplier1");
        Counterparty supplier2 = counterPartyService.addCounterParty("supplier2");
        Counterparty buyer = counterPartyService.addCounterParty("buyer");

        invoiceService.addInvoice(new Invoice(supplier1, buyer, BigDecimal.valueOf(200), ZERO, FORMAT.parse("30.05.2018")));
        invoiceService.addInvoice(new Invoice(supplier1, buyer, BigDecimal.valueOf(200), BigDecimal.valueOf(100.5), FORMAT.parse("26.12.2017")));
        invoiceService.addInvoice(new Invoice(supplier2, buyer, BigDecimal.valueOf(300), ZERO, FORMAT.parse("01.01.2017")));

        String actualSupplier1Csv = importExportService.exportCsvBySource(supplier1.getId());
        String expectedSupplier1Csv = "supplier1,buyer,200,0,30.05.2018\n" +
                "supplier1,buyer,99.5,100.5,26.12.2017\n";

        Assert.assertEquals(expectedSupplier1Csv, actualSupplier1Csv);

        String actualBuyerCsv = importExportService.exportCsvByTarget(buyer.getId());
        String expectedBuyerCsv = "supplier1,buyer,200,0,30.05.2018\n" +
                "supplier1,buyer,99.5,100.5,26.12.2017\n" +
                "supplier2,buyer,300,0,01.01.2017\n";

        Assert.assertEquals(expectedBuyerCsv, actualBuyerCsv);
    }

    private MultipartFile getMultipartFile(String folderPath, String fileName, String contentType) throws IOException {
        Path path = Paths.get(folderPath + fileName);
        byte[] content = null;

        content = Files.readAllBytes(path);

        return new MockMultipartFile(fileName, fileName, contentType, content);
    }
}
