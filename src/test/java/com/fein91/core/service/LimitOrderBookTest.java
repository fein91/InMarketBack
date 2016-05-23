package com.fein91.core.service;

import com.fein91.core.model.MarketOrderResult;
import com.fein91.core.model.OrderTree;
import com.fein91.core.model.OrderType;
import junit.framework.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class LimitOrderBookTest {

    @Test
    public void test1() throws Exception {
        LimitOrderBookDecorator lobService = new LimitOrderBookDecorator();
        lobService.addAskLimitOrder(50, 30d);
        lobService.addAskLimitOrder(100, 29d);
        lobService.addAskLimitOrder(200, 28d);
        lobService.addAskLimitOrder(150, 27d);
        lobService.addAskLimitOrder(250, 26d);
        lobService.addAskLimitOrder(50, 25d);

        MarketOrderResult marketOrderResult = lobService.addBidMarketOrder(400);
        BigDecimal apr = marketOrderResult.getApr();
        BigDecimal expectedApr = BigDecimal.valueOf(26.125);
        Assert.assertEquals("Expected APR: [" + expectedApr + "], actual APR: [" + apr + "]", 0, expectedApr.compareTo(apr));

        marketOrderResult = lobService.addBidMarketOrder(300);
        apr = marketOrderResult.getApr();
        expectedApr = BigDecimal.valueOf(28);
        Assert.assertEquals("Expected APR: [" + expectedApr + "], actual APR: [" + apr + "]", 0, expectedApr.compareTo(apr.setScale(2, RoundingMode.HALF_UP)));
    }

    @Test
    public void test2() throws Exception {
        LimitOrderBookDecorator lobService = new LimitOrderBookDecorator();
        lobService.addBidLimitOrder(200, 25d);
        lobService.addBidLimitOrder(400, 27.5d);
        lobService.addBidLimitOrder(400, 30d);

        int demand = 1100;
        MarketOrderResult marketOrderResult = lobService.addAskMarketOrder(demand);
        BigDecimal apr = marketOrderResult.getApr();
        BigDecimal expectedApr = BigDecimal.valueOf(28);
        Assert.assertEquals("Expected APR: [" + expectedApr + "], actual APR: [" + apr + "]", 0, expectedApr.compareTo(apr));

        int expectedSatisfiedDemand = 1000;
        Assert.assertEquals(expectedSatisfiedDemand, marketOrderResult.getSatisfiedDemand());
        Assert.assertEquals(expectedApr.doubleValue(), lobService.lob.getBestOffer());
        Assert.assertEquals(demand - expectedSatisfiedDemand, lobService.lob.getVolumeAtPrice(OrderType.ASK.getCoreName(), expectedApr.doubleValue()));
    }
}
