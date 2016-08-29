package com.fein91.service;

import com.fein91.core.model.OrderBook;
import com.fein91.core.model.OrderSide;
import com.fein91.core.model.Trade;
import com.fein91.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TransactionHistoryService {

    private final HistoryTradeService historyTradeService;
    private final HistoryOrderRequestService historyOrderRequestService;
    private final CalculationService calculationService;

    @Autowired
    public TransactionHistoryService(HistoryTradeService historyTradeService,
                                     HistoryOrderRequestService historyOrderRequestService,
                                     CalculationService calculationService) {
        this.historyTradeService = historyTradeService;
        this.historyOrderRequestService = historyOrderRequestService;
        this.calculationService = calculationService;
    }

    public void saveLimitOrdersHistory(OrderRequest limitOrderRequest) {
        historyOrderRequestService.save(historyOrderRequestService.convertFrom(limitOrderRequest));
    }

    public void saveMarketOrdersHistory(OrderRequest orderRequest, OrderBook lob, OrderResult result) {
        HistoryOrderRequest currentCounterpartyHOR = writeHistoryOrderRequestToCurrentCounterpartyTransactionHistory(orderRequest, lob.getTape(), result);

        Map<HistoryOrderRequest, List<HistoryTrade>> tradesByTargetCounterparty = currentCounterpartyHOR.getHistoryTrades().stream()
                .collect(Collectors.groupingBy(HistoryTrade::getAffectedOrderRequest));

        for (Map.Entry<HistoryOrderRequest, List<HistoryTrade>> entry : tradesByTargetCounterparty.entrySet()) {
            writeHistoryOrderRequestToTargetCounterpartyTransactionHistory(entry.getKey(), orderRequest.getCounterparty(),
                    entry.getValue(), orderRequest.getSide().oppositeSide());
        }
    }

    private HistoryOrderRequest writeHistoryOrderRequestToCurrentCounterpartyTransactionHistory(OrderRequest orderRequest,
                                                                                                List<Trade> trades,
                                                                                                OrderResult result) {
        HistoryOrderRequest executedHor = historyOrderRequestService.convertFrom(orderRequest);
        executedHor.setQuantity(result.getSatisfiedDemand());
        executedHor.setPrice(result.getApr());
        executedHor.setAvgDiscountPerc(result.getAvgDiscountPerc());
        executedHor.setAvgDaysToPayment(result.getAvgDaysToPayment());
        executedHor.setHistoryTrades(historyTradeService.convertFrom(trades));
        return historyOrderRequestService.save(executedHor);
    }

    private void writeHistoryOrderRequestToTargetCounterpartyTransactionHistory(HistoryOrderRequest executedLimitHor,
                                                                                Counterparty target,
                                                                                List<HistoryTrade> trades, OrderSide orderSide) {
        HistoryOrderRequest targetHor = new HistoryOrderRequest();
        BigDecimal qty = BigDecimal.ZERO;
        BigDecimal totalInvoicesSum = BigDecimal.ZERO;
        BigDecimal totalDiscountsSum = BigDecimal.ZERO;
        for (HistoryTrade historyTrade : trades) {
            qty = qty.add(historyTrade.getQuantity());
            totalInvoicesSum = totalInvoicesSum.add(historyTrade.getUnpaidInvoiceValue());
            totalDiscountsSum = totalDiscountsSum.add(historyTrade.getDiscountValue());
        }
        targetHor.setQuantity(qty);
        targetHor.setPrice(executedLimitHor.getPrice());
        targetHor.setAvgDiscountPerc(calculationService.calculateAvgDiscountPerc(totalDiscountsSum, totalInvoicesSum));
        //TODO calc avg days to payment here
        //targetHor.setAvgDaysToPayment(calculationService.calculateAvgDaysToPayment());
        targetHor.setCounterparty(executedLimitHor.getCounterparty());
        targetHor.setDate(new Date());
        targetHor.setHistoryTrades(historyTradeService.copyAndUpdateTarget(target, trades));
        targetHor.setType(HistoryOrderType.EXECUTED_LIMIT);
        targetHor.setSide(orderSide);
        historyOrderRequestService.save(targetHor);

    }

}
