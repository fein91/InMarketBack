package com.fein91.service;

import com.fein91.core.model.OrderBook;
import com.fein91.core.model.OrderSide;
import com.fein91.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Autowired
    public TransactionHistoryService(HistoryTradeService historyTradeService,
                                     @Qualifier("HistoryOrderRequestServiceImpl") HistoryOrderRequestService historyOrderRequestService) {
        this.historyTradeService = historyTradeService;
        this.historyOrderRequestService = historyOrderRequestService;
    }

    public void saveLimitOrdersHistory(OrderRequest limitOrderRequest) {
        historyOrderRequestService.save(historyOrderRequestService.convertFrom(limitOrderRequest));
    }

    public void saveMarketOrdersHistory(OrderRequest orderRequest, OrderBook lob, OrderResult result) {
        HistoryOrderRequest currentCounterpartyHOR = writeHistoryOrderRequestToCurrentCounterpartyTransactionHistory(orderRequest, lob, result);

        Map<Counterparty, List<HistoryTrade>> tradesByTargetCounterparty = currentCounterpartyHOR.getHistoryTrades().stream()
                .collect(Collectors.groupingBy(HistoryTrade::getTarget));

        for (Map.Entry<Counterparty, List<HistoryTrade>> entry : tradesByTargetCounterparty.entrySet()) {
            writeHistoryOrderRequestToTargetCounterpartyTransactionHistory(entry.getKey(), orderRequest.getCounterparty(),
                    entry.getValue(), orderRequest.getSide().oppositeSide());
        }
    }

    private HistoryOrderRequest writeHistoryOrderRequestToCurrentCounterpartyTransactionHistory(OrderRequest orderRequest,
                                                                                                OrderBook lob,
                                                                                                OrderResult result) {
        HistoryOrderRequest executedHor = historyOrderRequestService.convertFrom(orderRequest);
        executedHor.setQuantity(result.getSatisfiedDemand());
        executedHor.setPrice(result.getApr());
        executedHor.setAvgDiscountPerc(result.getAvgDiscountPerc());
        executedHor.setHistoryTrades(historyTradeService.convertFrom(lob.getTape()));
        return historyOrderRequestService.save(executedHor);
    }

    private void writeHistoryOrderRequestToTargetCounterpartyTransactionHistory(Counterparty counterparty, Counterparty target,
                                                                                List<HistoryTrade> trades, OrderSide orderSide) {
        HistoryOrderRequest targetHor = new HistoryOrderRequest();
        BigDecimal qty = BigDecimal.ZERO;
        for (HistoryTrade historyTrade : trades) {
            qty = qty.add(historyTrade.getQuantity());
        }
        targetHor.setQuantity(qty);
        targetHor.setCounterparty(counterparty);
        targetHor.setDate(new Date());
        targetHor.setHistoryTrades(historyTradeService.copyAndUpdateTarget(target, trades));
        targetHor.setType(HistoryOrderType.EXECUTED_LIMIT);
        targetHor.setSide(orderSide);
        historyOrderRequestService.save(targetHor);

    }

}
