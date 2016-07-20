package com.fein91.core;

import com.fein91.builders.OrderBuilder;
import com.fein91.core.model.Order;
import com.fein91.core.model.OrderList;
import junit.framework.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Iterator;

public class OrderListTest {

    @Test
    public void test() throws Exception {
        OrderList orders = new OrderList();
        long smallerTimestamp = 1234;
        long biggerTimestamp = 12345;
        long theBiggestTimestamp = 123456;

        Order firstOrder = new OrderBuilder(1L)
                .price(BigDecimal.TEN)
                .quantity(BigDecimal.valueOf(20))
                .timestamp(smallerTimestamp)
                .build();
        orders.appendOrder(firstOrder);

        Order secondOrder = new OrderBuilder(2L)
                .price(BigDecimal.TEN)
                .quantity(BigDecimal.valueOf(10))
                .timestamp(biggerTimestamp)
                .build();
        orders.appendOrder(secondOrder);


        Order thirdOrder = new OrderBuilder(3L)
                .price(BigDecimal.TEN)
                .quantity(BigDecimal.valueOf(10))
                .timestamp(theBiggestTimestamp)
                .build();
        orders.appendOrder(thirdOrder);

        Assert.assertEquals(40, orders.getVolume().intValue());
        Assert.assertEquals(3, orders.getLength().intValue());

        Assert.assertEquals(firstOrder, orders.getHeadOrder());
        Assert.assertEquals(thirdOrder, orders.getTailOrder());

        Iterator<Order> iterator = orders.iterator();
        Assert.assertEquals(firstOrder, iterator.next());
        Assert.assertEquals(secondOrder, iterator.next());
        Assert.assertEquals(thirdOrder, iterator.next());
        Assert.assertFalse(iterator.hasNext());

        orders.removeOrder(secondOrder);

        Assert.assertEquals(30, orders.getVolume().intValue());
        Assert.assertEquals(2, orders.getLength().intValue());

        Assert.assertEquals(firstOrder, orders.getHeadOrder());
        Assert.assertEquals(thirdOrder, orders.getTailOrder());

        iterator = orders.iterator();
        Assert.assertEquals(firstOrder, iterator.next());
        Assert.assertEquals(thirdOrder, iterator.next());
        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void test2() throws Exception {
        OrderList orders = new OrderList();
        long smallerTimestamp = 1234;
        long biggerTimestamp = 12345;
        long theBiggestTimestamp = 123456;


        Order secondOrder = new OrderBuilder(2L)
                .price(BigDecimal.TEN)
                .quantity(BigDecimal.valueOf(10))
                .timestamp(biggerTimestamp)
                .build();
        orders.appendOrder(secondOrder);

        Order firstOrder = new OrderBuilder(1L)
                .price(BigDecimal.TEN)
                .quantity(BigDecimal.valueOf(20))
                .timestamp(smallerTimestamp)
                .build();
        orders.appendOrder(firstOrder);

        Order thirdOrder = new OrderBuilder(3L)
                .price(BigDecimal.TEN)
                .quantity(BigDecimal.valueOf(10))
                .timestamp(theBiggestTimestamp)
                .build();
        orders.appendOrder(thirdOrder);

        Assert.assertEquals(40, orders.getVolume().intValue());
        Assert.assertEquals(3, orders.getLength().intValue());
        Assert.assertEquals(firstOrder, orders.getHeadOrder());
        Assert.assertEquals(thirdOrder, orders.getTailOrder());
        Assert.assertEquals(firstOrder, orders.iterator().next());
    }
}
