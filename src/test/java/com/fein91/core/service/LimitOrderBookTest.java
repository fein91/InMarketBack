package com.fein91.core.service;

import com.fein91.core.model.*;
import org.junit.Test;

import java.util.Random;

public class LimitOrderBookTest {

//    @Test
//    public void test1() throws Exception {
//        LimitOrderBookDecorator lobService = new LimitOrderBookDecorator();
//        lobService.addAskLimitOrder(50, 30d);
//        lobService.addAskLimitOrder(100, 29d);
//        lobService.addAskLimitOrder(200, 28d);
//        lobService.addAskLimitOrder(150, 27d);
//        lobService.addAskLimitOrder(250, 26d);
//        lobService.addAskLimitOrder(50, 25d);
//
//        MarketOrderResult marketOrderResult = lobService.addBidMarketOrder(400);
//        BigDecimal apr = marketOrderResult.getApr();
//        BigDecimal expectedApr = BigDecimal.valueOf(26.125).setScale(1, BigDecimal.ROUND_HALF_UP);
//        Assert.assertEquals("Expected APR: [" + expectedApr + "], actual APR: [" + apr + "]", 0, expectedApr.compareTo(apr));
//
//        marketOrderResult = lobService.addBidMarketOrder(300);
//        apr = marketOrderResult.getApr();
//        expectedApr = BigDecimal.valueOf(28);
//        Assert.assertEquals("Expected APR: [" + expectedApr + "], actual APR: [" + apr + "]", 0, expectedApr.compareTo(apr));
//    }
//
//    @Test
//    public void test2() throws Exception {
//        LimitOrderBookDecorator lobService = new LimitOrderBookDecorator();
//        lobService.addBidLimitOrder(200, 25d);
//        lobService.addBidLimitOrder(400, 27.5d);
//        lobService.addBidLimitOrder(400, 30d);
//
//        int demand = 1100;
//        MarketOrderResult marketOrderResult = lobService.addAskMarketOrder(demand);
//        BigDecimal apr = marketOrderResult.getApr();
//        BigDecimal expectedApr = BigDecimal.valueOf(28);
//        Assert.assertEquals("Expected APR: [" + expectedApr + "], actual APR: [" + apr + "]", 0, expectedApr.compareTo(apr));
//
//        int expectedSatisfiedDemand = 1000;
//        Assert.assertEquals(expectedSatisfiedDemand, marketOrderResult.getSatisfiedDemand());
//
//        Assert.assertEquals(expectedApr.doubleValue(), lobService.lob.getBestOffer());
//        Assert.assertEquals(demand - expectedSatisfiedDemand, lobService.lob.getVolumeAtPrice(OrderSide.ASK.getCoreName(), expectedApr.doubleValue()));
//    }

    @Test
    public void test3() throws Exception {
        Order order1 = new Order(System.nanoTime(), true, 200, new Random(100000).nextInt(), OrderSide.BID.getCoreName(), 25d);
        Order order2 = new Order(System.nanoTime(), true, 400, new Random(100000).nextInt(), OrderSide.BID.getCoreName(), 27.5d);
        Order order3 = new Order(System.nanoTime(), true, 400, new Random(100000).nextInt(), OrderSide.BID.getCoreName(), 30d);

        Order order4 = new Order(System.nanoTime(), false, 1200, new Random(100000).nextInt(), OrderSide.ASK.getCoreName());

        OrderBook orderBook = new OrderBook(0.01d);
        orderBook.processOrder(order1, false);
        System.out.println(orderBook);
        orderBook.processOrder(order2, false);
        System.out.println(orderBook);
        orderBook.processOrder(order3, false);
        System.out.println(orderBook);
        OrderReport orderReport = orderBook.processOrder(order4, false);
        System.out.println(orderBook);
        System.out.println(orderReport);
    }

//    @Test(expected = IllegalArgumentException.class)
//    public void testEmptyBookThrowsException() throws Exception {
//        LimitOrderBookDecorator lobDecorator = new LimitOrderBookDecorator();
//        lobDecorator.addBidMarketOrder(100);
//    }
//
//    @Test(expected = IllegalArgumentException.class)
//    public void testZeroAskMarketOrderThrowsException() throws Exception {
//        LimitOrderBookDecorator lobDecorator = new LimitOrderBookDecorator();
//        lobDecorator.addAskMarketOrder(0);
//    }
//
//    @Test(expected = IllegalArgumentException.class)
//    public void testZeroBidLimitOrderThrowsException() throws Exception {
//        LimitOrderBookDecorator lobDecorator = new LimitOrderBookDecorator();
//        lobDecorator.addAskLimitOrder(100, 0);
//    }
}
