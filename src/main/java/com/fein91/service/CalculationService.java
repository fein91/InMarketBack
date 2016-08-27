package com.fein91.service;

import com.fein91.core.model.Trade;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.fein91.Constants.ROUNDING_MODE;
import static com.fein91.Constants.CALCULATION_SCALE;

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
                .divide(BigDecimal.valueOf(365*100), CALCULATION_SCALE, ROUNDING_MODE);
    }

    public BigDecimal calculateMaxPossibleInvoicePrepaidValue(BigDecimal invoiceValue, BigDecimal discountPercent) {
        return invoiceValue.divide(BigDecimal.ONE.add(discountPercent), CALCULATION_SCALE, ROUNDING_MODE);
    }

    public int getDaysToPayment(Date paymentDate) {
        DateTime paymentDT = new DateTime(paymentDate);
        DateTime currDT = new DateTime();
        Days daysBetween = Days.daysBetween(currDT.toLocalDate(), paymentDT.toLocalDate());
        return daysBetween.getDays();
    }

    public BigDecimal calculateAvgDiscountPerc(BigDecimal totalDiscountSum, BigDecimal totalInvoicesSum) {
        return totalInvoicesSum.signum() > 0
                ? totalDiscountSum.divide(totalInvoicesSum, ROUNDING_MODE).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
    }

    /**
     * avgDaysToPayment = (invoice1.getDaysToPayment() * paymentByInvoice1 + ... + invoiceN.getDaysToPayment() * paymentByInvoiceN) / paymentByInvoice1 + ... + paymentByInvoiceN
     * @param trades
     * @return
     */
    public BigDecimal calculateAvgDaysToPayment(List<Trade> trades) {
        BigDecimal totalSumInvoicesDaysToPaymentMultQtyTraded = BigDecimal.ZERO;
        BigDecimal paymentsSum = BigDecimal.ZERO;
        for (Trade trade : trades) {
            totalSumInvoicesDaysToPaymentMultQtyTraded = totalSumInvoicesDaysToPaymentMultQtyTraded.add(trade.getDaysToPaymentMultQtyTraded());
            paymentsSum = paymentsSum.add(trade.getQty());
        }
        return paymentsSum.signum() > 0 ?
                totalSumInvoicesDaysToPaymentMultQtyTraded.divide(paymentsSum, ROUNDING_MODE)
                : BigDecimal.ZERO;
    }

    public BigDecimal calculateTotalInvoicesSum(List<Trade> trades) {
        BigDecimal result = BigDecimal.ZERO;
        for (Trade trade : trades) {
            result = result.add(trade.getInvoiceValue());
        }
        return result;
    }


    public BigDecimal calculateAPR(List<Trade> trades, BigDecimal satisfiedDemand) {
        BigDecimal apr = BigDecimal.ZERO;
        for (Trade trade : trades) {
            apr = apr.add(BigDecimal.valueOf(trade.getPrice()).multiply(trade.getQty()).divide(satisfiedDemand, ROUNDING_MODE));
        }

        return apr;
    }

    public BigDecimal calculateTotalDiscountSum(List<Trade> trades) {
        BigDecimal result = BigDecimal.ZERO;
        for (Trade trade : trades) {
            result = result.add(trade.getDiscountValue());
        }
        return result;
    }
}
