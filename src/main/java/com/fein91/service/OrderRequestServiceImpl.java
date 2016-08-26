package com.fein91.service;

import com.fein91.core.model.OrderBook;
import com.fein91.core.model.OrderSide;
import com.fein91.core.service.LimitOrderBookService;
import com.fein91.core.service.OrderBookBuilder;
import com.fein91.dao.InvoiceRepository;
import com.fein91.dao.OrderRequestRepository;
import com.fein91.model.*;
import com.fein91.rest.exception.OrderRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service("OrderRequestServiceImpl")
public class OrderRequestServiceImpl implements OrderRequestService {

    private final static Logger LOGGER = Logger.getLogger(OrderRequestServiceImpl.class.getName());

    private final OrderRequestRepository orderRequestRepository;
    private final InvoiceRepository invoiceRepository;
    private final LimitOrderBookService lobService;
    private final OrderBookBuilder orderBookBuilder;
    private final TransactionHistoryService transactionHistoryService;
    private final OrderRequestValidator orderRequestValidator;

    @Autowired
    public OrderRequestServiceImpl(OrderRequestRepository orderRequestRepository,
                                   InvoiceRepository invoiceRepository,
                                   LimitOrderBookService lobService,
                                   OrderBookBuilder orderBookBuilder,
                                   TransactionHistoryService transactionHistoryService,
                                   OrderRequestValidator orderRequestValidator) {
        this.orderRequestRepository = orderRequestRepository;
        this.invoiceRepository = invoiceRepository;
        this.lobService = lobService;
        this.orderBookBuilder = orderBookBuilder;
        this.transactionHistoryService = transactionHistoryService;
        this.orderRequestValidator = orderRequestValidator;
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

        OrderResult result = validateAndTrade(lob, orderRequest);

        if (result.getSatisfiedDemand().signum() > 0) {
            transactionHistoryService.saveMarketOrdersHistory(orderRequest, lob, result);
        }

        BigDecimal unsatisfiedDemand = orderRequest.getQuantity().subtract(result.getSatisfiedDemand());
        if (unsatisfiedDemand.signum() > 0) {
            OrderRequest limitOrderRequest;
            if (OrderType.LIMIT == orderRequest.getType()) {
                limitOrderRequest = orderRequest;
                limitOrderRequest.setQuantity(unsatisfiedDemand);
                limitOrderRequest = save(limitOrderRequest);
                transactionHistoryService.saveLimitOrdersHistory(limitOrderRequest);
            }
        }
        return result;
    }

    @Override
    @Transactional
    public OrderResult calculate(OrderRequest orderRequest) {
        OrderBook lob = orderBookBuilder.getStubInstance();
        return validateAndTrade(lob, orderRequest);
    }

    protected OrderResult validateAndTrade(OrderBook lob, OrderRequest orderRequest) {
        List<Invoice> checkedInvoices = findCheckedInvoices(orderRequest);

        if (OrderType.MARKET == orderRequest.getType()) {
            orderRequestValidator.validateByInvoices(orderRequest, checkedInvoices);
        }

        List<OrderRequest> limitOrdersToTrade = findLimitOrderRequestsToTrade(orderRequest, checkedInvoices);

        if (OrderType.MARKET == orderRequest.getType()) {
            orderRequestValidator.validateByOrdersToTrade(orderRequest, limitOrdersToTrade);
        }

        limitOrdersToTrade.forEach(limitOrder -> lobService.addOrder(lob, limitOrder));
        OrderResult result = lobService.addOrder(lob, orderRequest);

        orderRequestValidator.validateByResult(orderRequest, result);

        return result;
    }

    private List<OrderRequest> findLimitOrderRequestsToTrade(OrderRequest orderRequest, List<Invoice> invoices) {
        OrderSide side = orderRequest.getSide();
        Set<Counterparty> counterpartiesToTrade = findUniqueCounterpartiesToTrade(invoices, side);

        int oppositeSide = side.oppositeSide().getId();
        List<OrderRequest> result = new ArrayList<>();
        counterpartiesToTrade.forEach(counterparty -> {
            result.addAll(orderRequestRepository.findByCounterpartyAndSide(counterparty, oppositeSide));
        });

        return result;
    }

    private Set<Counterparty> findUniqueCounterpartiesToTrade(List<Invoice> invoices, OrderSide orderSide) {
        Set<Counterparty> result = new HashSet<>();
        invoices.forEach(invoice -> result.add(
                OrderSide.BID == orderSide
                        ? invoice.getTarget()
                        : invoice.getSource()));
        return result;
    }

    private List<Invoice> findCheckedInvoices(OrderRequest orderRequest) {
        OrderSide orderSide = orderRequest.getSide();
        Long counterpartyId = orderRequest.getCounterparty().getId();
        List<Invoice> invoices = OrderSide.BID == orderSide
                ? invoiceRepository.findInvoicesBySourceId(counterpartyId)
                : invoiceRepository.findInvoicesByTargetId(counterpartyId);
        return invoices.stream()
                .filter(invoice -> Boolean.TRUE.equals(orderRequest.getInvoicesChecked().get(invoice.getId())))
                .collect(Collectors.toList());
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
