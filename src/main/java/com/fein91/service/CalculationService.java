package com.fein91.service;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class CalculationService {

    /**
     *
     * @param apr value in percents from 0 to 100
     * @param paymentDate
     * @return discount percent value in fractions from 0 to 1
     */
    public BigDecimal calculateDiscountPercent(BigDecimal apr, Date paymentDate) {
        return calculateDiscountPercent(apr, getDaysToPayment(paymentDate));
    }


    /**
     *
     * @param apr value in percents from 0 to 100
     * @param daysToPayment
     * @return discount percent value in fractions from 0 to 1
     */
    public BigDecimal calculateDiscountPercent(BigDecimal apr, int daysToPayment) {
        return apr.multiply(BigDecimal.valueOf(daysToPayment))
                .divide(BigDecimal.valueOf(365), 10, BigDecimal.ROUND_HALF_UP)
                .divide(BigDecimal.valueOf(100), 10, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal calculateMaxPossibleInvoicePrepaidValue(BigDecimal invoiceValue, BigDecimal discountPercent) {
        return invoiceValue.divide(BigDecimal.ONE.add(discountPercent), BigDecimal.ROUND_HALF_UP);
    }

    public int getDaysToPayment(Date paymentDate) {
        DateTime paymentDT = new DateTime(paymentDate);
        DateTime currDT = new DateTime();
        Days daysBetween = Days.daysBetween(currDT.toLocalDate(), paymentDT.toLocalDate());
        return daysBetween.getDays();
    }
}