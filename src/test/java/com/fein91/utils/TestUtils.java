package com.fein91.utils;

import com.fein91.core.model.Trade;
import com.fein91.model.Counterparty;
import com.fein91.model.HistoryOrderRequest;
import com.fein91.model.HistoryOrderType;
import com.fein91.model.HistoryTrade;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class TestUtils {

    public Date getCurrentDayPlusDays(int days) {
        Instant instant = LocalDate.now().plusDays(days).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        return Date.from(instant);
    }

    public Trade findTradeByBuyerAndSeller(List<Trade> trades, Long buyerID, Long sellerId) {
        for (Trade trade : trades) {
            if (trade.getBuyer() == buyerID && trade.getSeller() == sellerId) {
                return trade;
            }
        }

        return null;
    }

    public HistoryOrderRequest findHistoryOrderRequestByOrderSide(List<HistoryOrderRequest> transHistory, HistoryOrderType orderType) {
        return transHistory.stream()
                .filter(historyOrderRequest -> orderType.equals(historyOrderRequest.getType()))
                .findFirst()
                .get();
    }

    public HistoryOrderRequest findHistoryOrderRequestByOrderSideAndPrice(List<HistoryOrderRequest> transHistory, HistoryOrderType orderType, BigDecimal price) {
        return transHistory.stream()
                .filter(historyOrderRequest -> orderType.equals(historyOrderRequest.getType()) && price.compareTo(historyOrderRequest.getPrice()) == 0)
                .findFirst()
                .get();
    }

    public HistoryTrade findHistoryTradeByTarget(List<HistoryTrade> trades, Counterparty target) {
        return trades.stream()
                .filter(trade -> target.equals(trade.getTarget()))
                .findFirst()
                .get();
    }

    public HistoryTrade findHistoryTradeByTargetAndInvoiceId(List<HistoryTrade> trades, Counterparty target, Long invoiceId) {
        return trades.stream()
                .filter(trade -> target.equals(trade.getTarget()) && invoiceId.equals(trade.getInvoice().getId()))
                .findFirst()
                .get();
    }
}
