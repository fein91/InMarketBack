package com.fein91.dao;

import com.fein91.model.Invoice;
import org.springframework.data.repository.Repository;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by olta1014 on 23.05.2016.
 */
public interface InvoiceRepository extends Repository<Invoice, BigInteger> {

    List<Invoice> findAll();
    Invoice findById(BigInteger id);
}
