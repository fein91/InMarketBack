package com.fein91.model;

import java.math.BigDecimal;

public interface CalculableTrade {

    BigDecimal getDaysToPaymentMultQtyTraded();

    BigDecimal getQuantity();
}
