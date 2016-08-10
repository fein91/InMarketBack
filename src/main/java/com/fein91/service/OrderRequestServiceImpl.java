package com.fein91.service;

import com.fein91.builders.OrderRequestBuilder;
import com.fein91.core.model.Order;
import com.fein91.core.model.OrderBook;
import com.fein91.core.model.OrderSide;
import com.fein91.core.service.LimitOrderBookService;
import com.fein91.core.service.OrderBookBuilder;
import com.fein91.dao.InvoiceRepository;
import com.fein91.dao.OrderRequestRepository;
import com.fein91.model.*;
import com.fein91.rest.exception.OrderRequestException;
import com.fein91.rest.exception.OrderRequestProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.fein91.rest.exception.ExceptionMessages.*;
import static com.fein91.Constants.ROUNDING_MODE;
import static com.fein91.Constants.UI_SCALE;

@Service("OrderRequestServiceImpl")
public class OrderRequestServiceImpl implements OrderRequestService {

    private final static Logger LOGGER = Logger.getLogger(OrderRequestServiceImpl.class.getName());

    private final OrderRequestRepository orderRequestRepository;
    private final InvoiceRepository invoiceRepository;
    private final LimitOrderBookService lobService;
    private final OrderBookBuilder orderBookBuilder;
    private final CounterPartyService counterPartyService;
    private final HistoryOrderRequestService historyOrderRequestService;
    private final HistoryTradeService historyTradeService;
    private final CalculationService calculationService;

    @Autowired
    public OrderRequestServiceImpl(OrderRequestRepository orderRequestRepository,
                                   InvoiceRepository invoiceRepository,
                                   LimitOrderBookService lobService,
                                   OrderBookBuilder orderBookBuilder,
                                   CounterPartyService counterPartyService,
                                   @Qualifier("HistoryOrderRequestServiceImpl") HistoryOrderRequestService historyOrderRequestService,
                                   HistoryTradeService historyTradeService,
                                   CalculationService calculationService) {
        this.orderRequestRepository = orderRequestRepository;
        this.invoiceRepository = invoiceRepository;
        this.lobService = lobService;
        this.orderBookBuilder = orderBookBuilder;
        this.counterPartyService = counterPartyService;
        this.historyOrderRequestService = historyOrderRequestService;
        this.historyTradeService = historyTradeService;
        this.calculationService = calculationService;
    }

    @Override
    public List<OrderRequest> getByCounterpartyId(Long counterpartyId) {
        return orderRequestRepository.findByCounterpartyId(counterpartyId);
    }

    @Override
    public OrderRequest getById(Long id) {
        return orderRequestRepository.findOne(id);
    }

    @Override
    @Transactional
    public OrderRequest update(OrderRequest orderRequest) {
        calculate(orderRequest);
        return orderRequestRepository.save(orderRequest);
    }

    @Override
    @Transactional
    public OrderRequest save(OrderRequest orderRequest) {
        LOGGER.info("Order request to save: " + orderRequest);
        return orderRequestRepository.save(orderRequest);
    }

    @Override
    @Transactional
    public OrderResult process(OrderRequest orderRequest) throws OrderRequestException {
        OrderBook lob = orderBookBuilder.getInstance();
        for (OrderRequest limitOrderRequest : findLimitOrderRequestsToTrade(orderRequest)) {
            lobService.addOrder(lob, limitOrderRequest);
        }

        OrderResult result = lobService.addOrder(lob, orderRequest);

        if (result.getSatisfiedDemand().signum() > 0) {
            saveMarketOrdersHistory(orderRequest, lob, result);
        }

        BigDecimal unsatisfiedDemand = orderRequest.getQuantity().subtract(result.getSatisfiedDemand());
        if (unsatisfiedDemand.signum() > 0) {
            saveLimitOrderRequest(orderRequest, result, unsatisfiedDemand);
        }
        return result;
    }


    private void saveMarketOrdersHistory(OrderRequest orderRequest, OrderBook lob, OrderResult result) {
        HistoryOrderRequest currentCounterpartyHOR = writeHistoryOrderRequestToCurrentCounterpartyTransactionHistory(orderRequest, lob, result);

        Map<Counterparty, List<HistoryTrade>> tradesByTargetCounterparty = currentCounterpartyHOR.getHistoryTrades().stream()
                .collect(Collectors.groupingBy(HistoryTrade::getTarget));

        for (Map.Entry<Counterparty, List<HistoryTrade>> entry : tradesByTargetCounterparty.entrySet()) {
            writeHistoryOrderRequestToTargetCounterpartyTransactionHistory(entry.getKey(), orderRequest.getCounterparty(),
                    entry.getValue(), orderRequest.getOrderSide().oppositeSide());
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
        targetHor.setHistoryOrderType(HistoryOrderType.EXECUTED_LIMIT);
        targetHor.setOrderSide(orderSide);
        historyOrderRequestService.save(targetHor);

    }

    private void saveLimitOrderRequest(OrderRequest orderRequest, OrderResult result, BigDecimal unsatisfiedDemand) {
        OrderRequest limitOrderRequest;
        if (OrderType.LIMIT == orderRequest.getOrderType()) {
            limitOrderRequest = orderRequest;
            limitOrderRequest.setQuantity(unsatisfiedDemand);
            limitOrderRequest = save(limitOrderRequest);
            historyOrderRequestService.save(historyOrderRequestService.convertFrom(limitOrderRequest));
        }
    }

    @Override
    @Transactional
    public OrderResult calculate(OrderRequest orderRequest) {
        OrderBook lob = orderBookBuilder.getStubInstance();
        for (OrderRequest limitOrderRequest : findLimitOrderRequestsToTrade(orderRequest)) {
            lobService.addOrder(lob, limitOrderRequest);
        }

        OrderResult result = lobService.addOrder(lob, orderRequest);

        if (OrderType.LIMIT == orderRequest.getOrderType() && result.getSatisfiedDemand().signum() > 0) {
            if (OrderSide.ASK == orderRequest.getOrderSide()) {
                throw new OrderRequestProcessingException(String.format(ASK_LIMIT_ORDER_CAN_BE_PROCESSED_AS_MARKET.getMessage(), result.getSatisfiedDemand(), result.getApr()),
                        String.format(ASK_LIMIT_ORDER_CAN_BE_PROCESSED_AS_MARKET.getLocalizedMessage(), result.getSatisfiedDemand(), result.getApr()));
            } else {
                throw new OrderRequestProcessingException(String.format(BID_LIMIT_ORDER_CAN_BE_PROCESSED_AS_MARKET.getMessage(), result.getSatisfiedDemand(), result.getApr()),
                        String.format(BID_LIMIT_ORDER_CAN_BE_PROCESSED_AS_MARKET.getLocalizedMessage(), result.getSatisfiedDemand(), result.getApr()));
            }
        }

        return result;
    }

    protected Set<OrderRequest> findLimitOrderRequestsToTrade(OrderRequest orderRequest) {
        OrderSide orderSide = orderRequest.getOrderSide();
        Long counterpartyId = orderRequest.getCounterparty().getId();
        List<Invoice> invoices = OrderSide.BID == orderSide
                ? invoiceRepository.findInvoicesBySourceId(counterpartyId)
                : invoiceRepository.findInvoicesByTargetId(counterpartyId);

        if (CollectionUtils.isEmpty(invoices)) {
            if (OrderSide.BID == orderSide) {
                throw new OrderRequestProcessingException(NO_BUYER_INVOICES_WERE_FOUND_WHILE_PROCESSING_ORDER_REQUEST.getMessage(),
                        NO_BUYER_INVOICES_WERE_FOUND_WHILE_PROCESSING_ORDER_REQUEST.getLocalizedMessage());
            } else if (OrderSide.ASK == orderSide) {
                throw new OrderRequestProcessingException(NO_SUPPLIER_INVOICES_WERE_FOUND_WHILE_PROCESSING_ORDER_REQUEST.getMessage(),
                        NO_SUPPLIER_INVOICES_WERE_FOUND_WHILE_PROCESSING_ORDER_REQUEST.getLocalizedMessage());
            }
        }

        Set<Counterparty> counterparties = new HashSet<>();
        Set<OrderRequest> orderRequestsToTrade = new HashSet<>();
        BigDecimal orderRequestsToTradeSum = BigDecimal.ZERO;
        BigDecimal availableOrderAmount = BigDecimal.ZERO;
        for (Invoice invoice : invoices) {
            boolean invoiceUnchecked = Boolean.FALSE.equals(orderRequest.getInvoicesChecked().get(invoice.getId()));
            if (invoiceUnchecked) {
                LOGGER.info("Invoice: " + invoice + " was skipped, because it was unchecked in UI");
                continue;
            }

            Counterparty giver = OrderSide.BID == orderSide
                    ? invoice.getTarget()
                    : invoice.getSource();

            if (counterparties.add(giver)) {
                List<OrderRequest> orderRequests = orderRequestRepository.findByCounterpartyAndOrderSide(giver, orderSide.oppositeSide().getId());
                orderRequestsToTradeSum = orderRequests.stream()
                            .map(OrderRequest :: getQuantity)
                            .reduce(orderRequestsToTradeSum, BigDecimal::add);
                orderRequestsToTrade.addAll(orderRequests);
            }
            BigDecimal unpaidInvoiceValue = invoice.getValue().subtract(invoice.getPrepaidValue());
            if (OrderType.LIMIT == orderRequest.getOrderType()) {
                BigDecimal discountPercent = calculationService.calculateDiscountPercent(orderRequest.getPrice(), invoice.getPaymentDate());
                BigDecimal maxPrepaidInvoiceValue = calculationService.calculateMaxPossibleInvoicePrepaidValue(unpaidInvoiceValue, discountPercent);
                availableOrderAmount = availableOrderAmount.add(maxPrepaidInvoiceValue);
            }
        }

        if (OrderType.MARKET == orderRequest.getOrderType()) {
            if (CollectionUtils.isEmpty(orderRequestsToTrade)) {
                throw new OrderRequestProcessingException(NO_SUITABLE_ORDER_REQUESTS_WERE_FOUND.getMessage(),
                        NO_SUITABLE_ORDER_REQUESTS_WERE_FOUND.getLocalizedMessage());
            } else if (orderRequest.getQuantity().compareTo(orderRequestsToTradeSum) > 0) {
                BigDecimal unsatisfiedDemand = orderRequest.getQuantity().subtract(orderRequestsToTradeSum);
                if (OrderSide.BID == orderRequest.getOrderSide()) {
                    throw new OrderRequestProcessingException(
                            String.format(BUYERS_ORDERS_SUM_NO_ENOUGH.getMessage(),
                                    orderRequest.getQuantity().setScale(UI_SCALE, ROUNDING_MODE),
                                    orderRequestsToTradeSum.setScale(UI_SCALE, ROUNDING_MODE),
                                    unsatisfiedDemand),
                            String.format(BUYERS_ORDERS_SUM_NO_ENOUGH.getLocalizedMessage(),
                                    orderRequest.getQuantity().setScale(UI_SCALE, ROUNDING_MODE),
                                    orderRequestsToTradeSum.setScale(UI_SCALE, ROUNDING_MODE),
                                    unsatisfiedDemand));
                } else if (OrderSide.ASK == orderRequest.getOrderSide()) {
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

        //works for limit orders only
        if (OrderType.LIMIT == orderRequest.getOrderType() && orderRequest.getQuantity().compareTo(availableOrderAmount) > 0) {
            throw new OrderRequestProcessingException(
                    String.format(REQUESTED_ORDER_QUANTITY_IS_GREATER_THAN_AVAILABLE_QUANTITY.getMessage(),
                        orderRequest.getQuantity().setScale(UI_SCALE, ROUNDING_MODE),
                        availableOrderAmount.setScale(UI_SCALE, ROUNDING_MODE)),
                    String.format(REQUESTED_ORDER_QUANTITY_IS_GREATER_THAN_AVAILABLE_QUANTITY.getLocalizedMessage(),
                            orderRequest.getQuantity().setScale(UI_SCALE, ROUNDING_MODE),
                            availableOrderAmount.setScale(UI_SCALE, ROUNDING_MODE)));
        }

        return orderRequestsToTrade;
    }

    @Override
    @Transactional
    public void removeById(Long orderId) {
        orderRequestRepository.delete(orderId);
        LOGGER.info("Order with id: " + orderId + " request was removed");
    }

    @Override
    @Transactional
    public OrderRequest update(Long orderId, BigDecimal qty) {
        OrderRequest orderRequest = orderRequestRepository.findOne(orderId);
        LOGGER.info(orderRequest + " quantity will be updated to: " + qty);
        orderRequest.setQuantity(qty);
        return orderRequestRepository.save(orderRequest);
    }
}
