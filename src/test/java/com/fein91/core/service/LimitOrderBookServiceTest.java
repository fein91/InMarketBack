package com.fein91.core.service;

import com.fein91.InMarketApplication;
import com.fein91.dao.CounterpartyRepository;
import com.fein91.model.Counterparty;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigInteger;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = InMarketApplication.class)
public class LimitOrderBookServiceTest {

    public static final BigInteger BUYER1_ID = BigInteger.valueOf(1);
    public static final BigInteger BUYER2_ID = BigInteger.valueOf(3);
    public static final BigInteger BUYER3_ID = BigInteger.valueOf(4);
    public static final BigInteger SUPPLIER_ID = BigInteger.valueOf(2);

    @Autowired
    LimitOrderBookService limitOrderBookService;
    @Autowired
    CounterpartyRepository counterpartyRepository;

    @Test
    public void test1() throws Exception {
        LimitOrderBookDecorator lob = new LimitOrderBookDecorator();

        Counterparty buyer1 = counterpartyRepository.findById(BUYER1_ID);
        Counterparty buyer2 = counterpartyRepository.findById(BUYER2_ID);
        Counterparty buyer3 = counterpartyRepository.findById(BUYER3_ID);

        limitOrderBookService.addBidLimitOrder(lob, buyer1, 100, 27d);
        limitOrderBookService.addBidLimitOrder(lob, buyer2, 100, 28d);
        limitOrderBookService.addBidLimitOrder(lob, buyer3, 100, 29d);

        Counterparty supplier = counterpartyRepository.findById(SUPPLIER_ID);
        limitOrderBookService.addAskMarketOrder(lob, supplier, 150);

    }
}
