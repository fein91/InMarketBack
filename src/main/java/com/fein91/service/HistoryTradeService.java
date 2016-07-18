package com.fein91.service;

import com.fein91.core.model.Trade;
import com.fein91.model.Counterparty;
import com.fein91.model.HistoryTrade;
import com.fein91.dao.HistoryTradeRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class HistoryTradeService {

    private final HistoryTradeRepository historyTradeRepository;
    private final InvoiceService invoiceService;
    private final HistoryOrderRequestService historyOrderRequestService;
    private final CounterPartyService counterPartyService;

    @Autowired
    public HistoryTradeService(HistoryTradeRepository historyTradeRepository,
                               @Qualifier("InvoiceServiceImpl") InvoiceService invoiceService,
                               @Qualifier("HistoryOrderRequestServiceImpl") HistoryOrderRequestService historyOrderRequestService,
                               CounterPartyService counterPartyService) {
        this.historyTradeRepository = historyTradeRepository;
        this.invoiceService = invoiceService;
        this.historyOrderRequestService = historyOrderRequestService;
        this.counterPartyService = counterPartyService;
    }

    public HistoryTrade getById(Long id) {
        return historyTradeRepository.findOne(id);
    }

    @Transactional
    public HistoryTrade save(HistoryTrade historyTrade) {
        return historyTradeRepository.save(historyTrade);
    }

    public HistoryTrade convertFrom(Trade trade) {
        HistoryTrade historyTrade = new HistoryTrade();
        historyTrade.setQuantity(trade.getQty());
        historyTrade.setDiscountValue(trade.getDiscountValue());
        historyTrade.setInvoice(invoiceService.getById(trade.getInvoiceId()));
        historyTrade.setTarget(counterPartyService.getById(trade.getProvider()));
        historyTrade.setAffectedOrderRequest(historyOrderRequestService.getByOriginOrderRequestId(trade.getOrderHit()));
        return historyTrade;
    }

    public List<HistoryTrade> convertFrom(List<Trade> trades) {
        List<HistoryTrade> result = new ArrayList<>();
        for (Trade trade : trades) {
            result.add(convertFrom(trade));
        }
        return result;
    }

    /**
     * this method is needed to copy history trades which will be saved to target counterparty transaction history
     * @param counterparty
     * @param historyTrades history trades which were saved for source counterparty which inited market order
     * @return list of copied counterparties which will be saved to affected in market order counterparties
     */
    public List<HistoryTrade> copyAndUpdateTarget(Counterparty counterparty, List<HistoryTrade> historyTrades) {
        List<HistoryTrade> result = new ArrayList<>();
        for (HistoryTrade historyTrade : historyTrades) {
            HistoryTrade historyTradeCopy = new HistoryTrade();
            BeanUtils.copyProperties(historyTrade, historyTradeCopy);
            historyTradeCopy.setTarget(counterparty);
            result.add(historyTradeCopy);
        }

        return result;
    }
}
