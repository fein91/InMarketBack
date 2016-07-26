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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = InMarketApplication.class)
public class ImportExportTest {

    @Autowired
    ImportExportService importExportService;
    @Autowired
    CounterPartyService counterPartyService;
    @Autowired
    @Qualifier("InvoiceServiceImpl")
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

    private MultipartFile getMultipartFile(String folderPath, String fileName, String contentType) throws IOException {
        Path path = Paths.get(folderPath + fileName);
        byte[] content = null;

        content = Files.readAllBytes(path);

        return new MockMultipartFile(fileName, fileName, contentType, content);
    }
}
