package com.fein91.service;

import com.fein91.core.model.OrderSide;
import com.fein91.model.Invoice;
import com.fein91.model.OrderRequest;
import com.fein91.model.OrderResult;
import com.fein91.model.OrderType;
import com.fein91.rest.exception.OrderRequestProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

import static com.fein91.Constants.ROUNDING_MODE;
import static com.fein91.Constants.UI_SCALE;
import static com.fein91.rest.exception.ExceptionMessages.*;
import static com.fein91.rest.exception.ExceptionMessages.SUPPLIERS_ORDERS_SUM_NO_ENOUGH;

@Service
public class OrderRequestValidator {

    private final CalculationService calculationService;

    @Autowired
    public OrderRequestValidator(CalculationService calculationService) {
        this.calculationService = calculationService;
    }

    public void validateByInvoices(OrderRequest orderRequest, List<Invoice> invoices) {
        BigDecimal availableOrderAmount = BigDecimal.ZERO;

        if (CollectionUtils.isEmpty(invoices)) {
            if (OrderSide.BID == orderRequest.getSide()) {
                throw new OrderRequestProcessingException(NO_BUYER_INVOICES_WERE_FOUND_WHILE_PROCESSING_ORDER_REQUEST.getMessage(),
                        NO_BUYER_INVOICES_WERE_FOUND_WHILE_PROCESSING_ORDER_REQUEST.getLocalizedMessage());
            } else if (OrderSide.ASK == orderRequest.getSide()) {
                throw new OrderRequestProcessingException(NO_SUPPLIER_INVOICES_WERE_FOUND_WHILE_PROCESSING_ORDER_REQUEST.getMessage(),
                        NO_SUPPLIER_INVOICES_WERE_FOUND_WHILE_PROCESSING_ORDER_REQUEST.getLocalizedMessage());
            }
        }

        for (Invoice invoice : invoices) {
            BigDecimal unpaidInvoiceValue = invoice.getValue().subtract(invoice.getPrepaidValue());
            if (OrderType.LIMIT == orderRequest.getType()) {
                BigDecimal discountPercent = calculationService.calculateDiscountPercent(orderRequest.getPrice(), invoice.getPaymentDate());
                BigDecimal maxPrepaidInvoiceValue = calculationService.calculateMaxPossibleInvoicePrepaidValue(unpaidInvoiceValue, discountPercent);
                availableOrderAmount = availableOrderAmount.add(maxPrepaidInvoiceValue);
            } else if (OrderType.MARKET == orderRequest.getType()) {
                availableOrderAmount = availableOrderAmount.add(unpaidInvoiceValue);
            }
        }

        if (orderRequest.getQuantity().compareTo(availableOrderAmount) > 0) {
            throw new OrderRequestProcessingException(
                    String.format(REQUESTED_ORDER_QUANTITY_IS_GREATER_THAN_AVAILABLE_QUANTITY.getMessage(),
                            orderRequest.getQuantity().setScale(UI_SCALE, ROUNDING_MODE),
                            availableOrderAmount.setScale(UI_SCALE, ROUNDING_MODE)),
                    String.format(REQUESTED_ORDER_QUANTITY_IS_GREATER_THAN_AVAILABLE_QUANTITY.getLocalizedMessage(),
                            orderRequest.getQuantity().setScale(UI_SCALE, ROUNDING_MODE),
                            availableOrderAmount.setScale(UI_SCALE, ROUNDING_MODE)));
        }
    }

    public void validateByOrdersToTrade(OrderRequest orderRequest, List<OrderRequest> orderRequestsToTrade) {
        BigDecimal orderRequestsToTradeSum = BigDecimal.ZERO;
        orderRequestsToTradeSum = orderRequestsToTrade.stream()
                .map(OrderRequest :: getQuantity)
                .reduce(orderRequestsToTradeSum, BigDecimal::add);

        if (OrderType.MARKET == orderRequest.getType()) {
            if (CollectionUtils.isEmpty(orderRequestsToTrade)) {
                throw new OrderRequestProcessingException(NO_SUITABLE_ORDER_REQUESTS_WERE_FOUND.getMessage(),
                        NO_SUITABLE_ORDER_REQUESTS_WERE_FOUND.getLocalizedMessage());
            } else if (orderRequest.getQuantity().compareTo(orderRequestsToTradeSum) > 0) {
                BigDecimal unsatisfiedDemand = orderRequest.getQuantity().subtract(orderRequestsToTradeSum);
                if (OrderSide.BID == orderRequest.getSide()) {
                    throw new OrderRequestProcessingException(
                            String.format(BUYERS_ORDERS_SUM_NO_ENOUGH.getMessage(),
                                    orderRequest.getQuantity().setScale(UI_SCALE, ROUNDING_MODE),
                                    orderRequestsToTradeSum.setScale(UI_SCALE, ROUNDING_MODE),
                                    unsatisfiedDemand),
                            String.format(BUYERS_ORDERS_SUM_NO_ENOUGH.getLocalizedMessage(),
                                    orderRequest.getQuantity().setScale(UI_SCALE, ROUNDING_MODE),
                                    orderRequestsToTradeSum.setScale(UI_SCALE, ROUNDING_MODE),
                                    unsatisfiedDemand));
                } else if (OrderSide.ASK == orderRequest.getSide()) {
                    throw new OrderRequestProcessingException(
                            String.format(SUPPLIERS_ORDERS_SUM_NO_ENOUGH.getMessage(),
                                    orderRequest.getQuantity().setScale(UI_SCALE, ROUNDING_MODE),
                                    orderRequestsToTradeSum.setScale(UI_SCALE, ROUNDING_MODE),
                                    unsatisfiedDemand),
                            String.format(SUPPLIERS_ORDERS_SUM_NO_ENOUGH.getLocalizedMessage(),
                                    orderRequest.getQuantity().setScale(UI_SCALE, ROUNDING_MODE),
                                    orderRequestsToTradeSum.setScale(UI_SCALE, ROUNDING_MODE),
                                    unsatisfiedDemand));
                }
            }
        }
    }

    public void validateByResult(OrderRequest orderRequest, OrderResult result) {
        if (OrderType.LIMIT == orderRequest.getType() && result.getSatisfiedDemand().signum() > 0) {
            if (OrderSide.ASK == orderRequest.getSide()) {
                throw new OrderRequestProcessingException(String.format(ASK_LIMIT_ORDER_CAN_BE_PROCESSED_AS_MARKET.getMessage(), result.getSatisfiedDemand(), result.getApr()),
                        String.format(ASK_LIMIT_ORDER_CAN_BE_PROCESSED_AS_MARKET.getLocalizedMessage(), result.getSatisfiedDemand(), result.getApr()));
            } else {
                throw new OrderRequestProcessingException(String.format(BID_LIMIT_ORDER_CAN_BE_PROCESSED_AS_MARKET.getMessage(), result.getSatisfiedDemand(), result.getApr()),
                        String.format(BID_LIMIT_ORDER_CAN_BE_PROCESSED_AS_MARKET.getLocalizedMessage(), result.getSatisfiedDemand(), result.getApr()));
            }
        } else if (OrderType.MARKET == orderRequest.getType()
                && orderRequest.getQuantity().compareTo(result.getSatisfiedDemand()) > 0) {
            throw new OrderRequestProcessingException(
                    String.format(REQUESTED_ORDER_QUANTITY_IS_GREATER_THAN_AVAILABLE_QUANTITY.getMessage(),
                            orderRequest.getQuantity().setScale(UI_SCALE, ROUNDING_MODE),
                            result.getSatisfiedDemand().setScale(UI_SCALE, ROUNDING_MODE)),
                    String.format(REQUESTED_ORDER_QUANTITY_IS_GREATER_THAN_AVAILABLE_QUANTITY.getLocalizedMessage(),
                            orderRequest.getQuantity().setScale(UI_SCALE, ROUNDING_MODE),
                            result.getSatisfiedDemand().setScale(UI_SCALE, ROUNDING_MODE)));
        }
    }
}
