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
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;

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

    @Autowired
    public OrderRequestServiceImpl(OrderRequestRepository orderRequestRepository,
                                   InvoiceRepository invoiceRepository,
                                   LimitOrderBookService lobService,
                                   OrderBookBuilder orderBookBuilder,
                                   CounterPartyService counterPartyService,
                                   @Qualifier("HistoryOrderRequestServiceImpl") HistoryOrderRequestService historyOrderRequestService,
                                   HistoryTradeService historyTradeService) {
        this.orderRequestRepository = orderRequestRepository;
        this.invoiceRepository = invoiceRepository;
        this.lobService = lobService;
        this.orderBookBuilder = orderBookBuilder;
        this.counterPartyService = counterPartyService;
        this.historyOrderRequestService = historyOrderRequestService;
        this.historyTradeService = historyTradeService;
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
    public OrderRequest save(OrderRequest orderRequest) {
        LOGGER.info("Order request to save: " + orderRequest);
        return orderRequestRepository.save(orderRequest);
    }

    @Override
    @Transactional
    public OrderRequest saveOrder(Order order) {
        OrderRequest orderRequest = new OrderRequestBuilder(counterPartyService.getById(order.getTakerId()))
                .orderSide(order.getOrderSide())
                .orderType(order.getOrderType())
                .price(BigDecimal.valueOf(order.getPrice()))
                .quantity(order.getQuantity())
                .build();
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
            HistoryOrderRequest executedHor = historyOrderRequestService.convertFrom(orderRequest);
            executedHor.setQuantity(result.getSatisfiedDemand());
            executedHor.setHistoryTrades(historyTradeService.convertFrom(lob.getTape()));
            historyOrderRequestService.save(executedHor);
        }

        BigDecimal unsatisfiedDemand = orderRequest.getQuantity().subtract(result.getSatisfiedDemand());
        if (unsatisfiedDemand.signum() > 0) {
            OrderRequest limitOrderRequest;
            if (OrderType.LIMIT == orderRequest.getOrderType()) {
                limitOrderRequest = orderRequest;
                limitOrderRequest.setQuantity(unsatisfiedDemand);
                orderRequestRepository.save(limitOrderRequest);
            } else {
                limitOrderRequest = new OrderRequestBuilder(orderRequest.getCounterparty())
                        .date(orderRequest.getDate())
                        .orderSide(orderRequest.getOrderSide())
                        .orderType(OrderType.LIMIT)
                        .price(result.getApr())
                        .quantity(unsatisfiedDemand)
                        .build();
                //it's needed here to validate if we can add this order
                findLimitOrderRequestsToTrade(limitOrderRequest);
                orderRequestRepository.save(limitOrderRequest);
            }
            historyOrderRequestService.save(historyOrderRequestService.convertFrom(limitOrderRequest));
        }
        return result;
    }

    @Override
    @Transactional
    public OrderResult calculate(OrderRequest orderRequest) {
        OrderBook lob = orderBookBuilder.getStubInstance();
        for (OrderRequest limitOrderRequest : findLimitOrderRequestsToTrade(orderRequest)) {
            lobService.addOrder(lob, limitOrderRequest);
        }

        OrderResult result = lobService.addOrder(lob, orderRequest);
        BigDecimal unsatisfiedDemand = orderRequest.getQuantity().subtract(result.getSatisfiedDemand());
        if (unsatisfiedDemand.signum() > 0) {
            if (OrderType.LIMIT == orderRequest.getOrderType()) {
                orderRequest.setQuantity(unsatisfiedDemand);
                findLimitOrderRequestsToTrade(orderRequest);
            } else {
                OrderRequest limitOrderRequest = new OrderRequestBuilder(orderRequest.getCounterparty())
                        .date(orderRequest.getDate())
                        .orderSide(orderRequest.getOrderSide())
                        .orderType(OrderType.LIMIT)
                        .price(result.getApr())
                        .quantity(unsatisfiedDemand)
                        .build();
                findLimitOrderRequestsToTrade(limitOrderRequest);
            }
        }
        return result;
    }

    @Override
    @Transactional
    public Set<OrderRequest> findLimitOrderRequestsToTrade(OrderRequest orderRequest) {
        OrderSide orderSide = orderRequest.getOrderSide();
        Long counterpartyId = orderRequest.getCounterparty().getId();
        List<Invoice> invoices = OrderSide.BID == orderSide
                ? invoiceRepository.findInvoicesBySourceId(counterpartyId)
                : invoiceRepository.findInvoicesByTargetId(counterpartyId);

        if (CollectionUtils.isEmpty(invoices)) {
            throw new OrderRequestProcessingException("No invoices were found while processing order request");
        }

        Set<Counterparty> counterparties = new HashSet<>();
        Set<OrderRequest> orderRequests = new HashSet<>();
        BigDecimal invoicesSum = BigDecimal.ZERO;
        BigDecimal discountsSum = BigDecimal.ZERO;
        for (Invoice invoice : invoices) {
            Boolean invoiceUnchecked = Boolean.FALSE.equals(orderRequest.getInvoicesChecked().get(invoice.getId()));
            if (invoiceUnchecked) {
                LOGGER.info("Invoice: " + invoice + " was skipped, because it was unchecked in UI");
                continue;
            }

            Counterparty giver = OrderSide.BID == orderSide
                    ? invoice.getTarget()
                    : invoice.getSource();

            if (counterparties.add(giver)) {
                orderRequests.addAll(orderRequestRepository.findByCounterpartyAndOrderSide(giver, orderSide.oppositeSide().getId()));
            }
            invoicesSum = invoicesSum.add(invoice.getValue());
            if (OrderType.LIMIT == orderRequest.getOrderType()) {
                discountsSum = discountsSum.add(calculateDiscount(orderRequest.getPrice(), getDaysToPayment(invoice.getPaymentDate())));
            }
        }

        if (OrderType.MARKET == orderRequest.getOrderType() && CollectionUtils.isEmpty(orderRequests)) {
            throw new OrderRequestProcessingException("No suitable order requests were found");
        }
        BigDecimal availableOrderAmount = invoicesSum.subtract(discountsSum);
        if (orderRequest.getQuantity().compareTo(availableOrderAmount) > 0) {
            throw new OrderRequestProcessingException("Requested order quantity: " + orderRequest.getQuantity()
                    + " is greater than available quantity = invoices - discounts: " + availableOrderAmount);
        }

        return orderRequests;
    }

    private BigDecimal calculateDiscount(BigDecimal apr, int daysToPayment) {
        //double discount = Math.pow(1 + apr / 100, daysBetween.getDays() / 365d) - 1;
        return apr.multiply(BigDecimal.valueOf(daysToPayment))
                .divide(BigDecimal.valueOf(365), 10, BigDecimal.ROUND_HALF_UP)
                .divide(BigDecimal.valueOf(100), 10, BigDecimal.ROUND_HALF_UP);
    }

    private int getDaysToPayment(Date paymentDate) {
        DateTime paymentDT = new DateTime(paymentDate);
        DateTime currDT = new DateTime();
        Days daysBetween = Days.daysBetween(currDT.toLocalDate(), paymentDT.toLocalDate());
        int daysToPayment = daysBetween.getDays();
        LOGGER.info("Days to payment date left: " + daysToPayment);
        return daysToPayment;
    }

    @Override
    @Transactional
    public void removeById(Long orderId) {
        orderRequestRepository.delete(orderId);
        LOGGER.info("Order with id: " + orderId + " request was removed");
    }

    @Override
    @Transactional
    public OrderRequest updateOrderRequest(Long orderId, BigDecimal qty) {
        OrderRequest orderRequest = orderRequestRepository.findOne(orderId);
        LOGGER.info(orderRequest + " quantity will be updated to: " + qty);
        orderRequest.setQuantity(qty);
        return orderRequestRepository.save(orderRequest);
    }
}
