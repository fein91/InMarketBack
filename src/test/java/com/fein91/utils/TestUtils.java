package com.fein91.utils;

import com.fein91.core.model.Trade;
import com.fein91.model.Counterparty;
import com.fein91.model.HistoryOrderRequest;
import com.fein91.model.HistoryOrderType;
import com.fein91.model.HistoryTrade;

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
                .filter(historyOrderRequest -> orderType.equals(historyOrderRequest.getHistoryOrderType()))
                .findFirst()
                .get();
    }

    public HistoryTrade findHistoryTradeByTarget(List<HistoryTrade> trades, Counterparty target) {
        return trades.stream()
                .filter(trade -> target.equals(trade.getTarget()))
                .findFirst()
                .get();
    }
}
