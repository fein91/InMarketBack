package com.fein91.rest;

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

import java.util.List;

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
    public List<OrderRequest> getByCounterpartyId(@RequestParam(value = "counterpartyId", required = true) Long counterpartyId) {
        return orderRequestServiceImpl.getByCounterpartyId(counterpartyId);
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
        if (OrderType.LIMIT == orderRequest.getOrderType() && orderRequest.getPrice().signum() <= 0) {
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
