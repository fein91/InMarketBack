package com.fein91.rest;

import com.fein91.core.model.Order;
import com.fein91.model.ErrorResponse;
import com.fein91.model.OrderRequest;
import com.fein91.model.OrderResult;
import com.fein91.model.OrderType;
import com.fein91.rest.exception.OrderRequestException;
import com.fein91.rest.exception.OrderRequestProcessingException;
import com.fein91.service.OrderRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/orderRequests")
public class OrderRequestController {

    private final OrderRequestService orderRequestServiceImpl;

    @Autowired
    public OrderRequestController(@Qualifier("OrderRequestServiceImpl") OrderRequestService orderRequestServiceImpl) {
        this.orderRequestServiceImpl = orderRequestServiceImpl;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/process")
    public ResponseEntity<OrderResult> process(@RequestBody OrderRequest orderRequest) throws OrderRequestException {
        checkOrderRequest(orderRequest);
        return new ResponseEntity<>(orderRequestServiceImpl.process(orderRequest), HttpStatus.OK);
    }


    @RequestMapping(method = RequestMethod.POST, value = "/calculate")
    public ResponseEntity<OrderResult> calculate(@RequestBody OrderRequest orderRequest) throws OrderRequestException {
        checkOrderRequest(orderRequest);
        return new ResponseEntity<>(orderRequestServiceImpl.calculate(orderRequest), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET)
    public OrderRequest getById(@RequestParam(value = "orderId", required = true) Long orderId) {
        return orderRequestServiceImpl.getById(orderId);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/update")
    public ResponseEntity<OrderRequest> update(@RequestBody OrderRequest orderRequest) throws OrderRequestException {
        checkOrderRequest(orderRequest);
        if (OrderType.LIMIT != orderRequest.getOrderType()) {
            throw new OrderRequestException("Only limit order request can be updated");
        }

        return new ResponseEntity<>(orderRequestServiceImpl.update(orderRequest), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE)
    public ResponseEntity delete(@RequestParam(value = "orderId", required = true) Long orderId) {
        orderRequestServiceImpl.removeById(orderId);
        return new ResponseEntity(HttpStatus.OK);
    }

    private void checkOrderRequest(OrderRequest orderRequest) throws OrderRequestException {
        if (orderRequest.getCounterparty() == null) {
            throw new OrderRequestException("Order request counterparty isn't filled");
        }
        if (orderRequest.getQuantity() == null) {
            throw new OrderRequestException("Order request quantity isn't filled");
        }
        if (orderRequest.getQuantity().signum() <= 0) {
            throw new OrderRequestException("Order request quantity incorrect value: " + orderRequest.getQuantity());
        }
        if (orderRequest.getOrderType() == null) {
            throw new OrderRequestException("Order request type isn't filled");
        }
        if (orderRequest.getOrderSide() == null) {
            throw new OrderRequestException("Order request side isn't filled");
        }
        if (OrderType.LIMIT == orderRequest.getOrderType() && orderRequest.getPrice() == null) {
            throw new OrderRequestException("Limit order request price isn't filled");
        }
        if (OrderType.LIMIT == orderRequest.getOrderType()
                && (orderRequest.getPrice().signum() <= 0 || orderRequest.getPrice().compareTo(BigDecimal.valueOf(100)) >= 0)) {
            throw new OrderRequestException("Limit order request price incorrect value: " + orderRequest.getPrice());
        }
    }

    @ExceptionHandler(OrderRequestException.class)
    public ResponseEntity<ErrorResponse> orderRequestExceptionHandler(Exception ex) {
        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(HttpStatus.PRECONDITION_FAILED.value());
        error.setMessage(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.PRECONDITION_FAILED);
    }

    @ExceptionHandler(OrderRequestProcessingException.class)
    public ResponseEntity<ErrorResponse> orderRequestProcessingExceptionHandler(Exception ex) {
        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setMessage(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
