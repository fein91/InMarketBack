package com.fein91.rest;

import com.fein91.model.ErrorResponse;
import com.fein91.model.OrderRequest;
import com.fein91.model.OrderResult;
import com.fein91.model.OrderType;
import com.fein91.rest.exception.OrderRequestException;
import com.fein91.rest.exception.OrderRequestProcessingException;
import com.fein91.rest.exception.RollbackOnCalculateException;
import com.fein91.service.OrderRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import static com.fein91.rest.exception.ExceptionMessages.*;

@RestController
@RequestMapping("/orderRequests")
public class OrderRequestController {

    private final OrderRequestService orderRequestServiceImpl;

    @Autowired
    public OrderRequestController(OrderRequestService orderRequestServiceImpl) {
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
        OrderResult result = null;
        try {
            orderRequestServiceImpl.calculate(orderRequest);
        } catch (RollbackOnCalculateException ex) {
            result = ex.getOrderResult();
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET)
    public OrderRequest getById(@RequestParam(value = "orderId", required = true) Long orderId) {
        return orderRequestServiceImpl.getById(orderId);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/update")
    public ResponseEntity<OrderRequest> update(@RequestBody OrderRequest orderRequest) throws OrderRequestException {
        checkOrderRequest(orderRequest);
        if (OrderType.LIMIT != orderRequest.getType()) {
            throw new OrderRequestException(ONLY_LIMIT_ORDER_REQUEST_CAN_BE_UPDATED.getMessage(), ONLY_LIMIT_ORDER_REQUEST_CAN_BE_UPDATED.getLocalizedMessage());
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
            throw new OrderRequestException(ORDER_REQUEST_COUNTERPARTY_ISNT_FILLED.getMessage(), ORDER_REQUEST_COUNTERPARTY_ISNT_FILLED.getLocalizedMessage());
        }
        if (orderRequest.getQuantity() == null) {
            throw new OrderRequestException(ORDER_REQUEST_QUANTITY_ISNT_FILLED.getMessage(), ORDER_REQUEST_QUANTITY_ISNT_FILLED.getLocalizedMessage());
        }
        if (orderRequest.getQuantity().signum() <= 0) {
            throw new OrderRequestException(String.format(ORDER_REQUEST_QUANTITY_INCORRECT_VALUE.getMessage(), orderRequest.getQuantity()),
                    String.format(ORDER_REQUEST_QUANTITY_INCORRECT_VALUE.getLocalizedMessage(), orderRequest.getQuantity()));
        }
        if (orderRequest.getType() == null) {
            throw new OrderRequestException(ORDER_REQUEST_TYPE_ISNT_FILLED.getMessage(), ORDER_REQUEST_TYPE_ISNT_FILLED.getLocalizedMessage());
        }
        if (orderRequest.getSide() == null) {
            throw new OrderRequestException(ORDER_REQUEST_SIDE_ISNT_FILLED.getMessage(), ORDER_REQUEST_SIDE_ISNT_FILLED.getLocalizedMessage());
        }
        if (OrderType.LIMIT == orderRequest.getType() && orderRequest.getPrice() == null) {
            throw new OrderRequestException(LIMIT_ORDER_REQUEST_PRICE_ISNT_FILLED.getMessage(), LIMIT_ORDER_REQUEST_PRICE_ISNT_FILLED.getLocalizedMessage());
        }
        if (OrderType.LIMIT == orderRequest.getType()
                && (orderRequest.getPrice().signum() <= 0 || orderRequest.getPrice().compareTo(BigDecimal.valueOf(100)) >= 0)) {
            throw new OrderRequestException(String.format(LIMIT_ORDER_REQUEST_PRICE_INCORRECT_VALUE.getMessage(), orderRequest.getPrice()),
                    String.format(LIMIT_ORDER_REQUEST_PRICE_INCORRECT_VALUE.getLocalizedMessage(), orderRequest.getPrice()));
        }
    }

    @ExceptionHandler(OrderRequestException.class)
    public ResponseEntity<ErrorResponse> orderRequestExceptionHandler(OrderRequestException ex) {
        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(HttpStatus.PRECONDITION_FAILED.value());
        error.setMessage(ex.getLocalizedMsg());
        return new ResponseEntity<>(error, HttpStatus.PRECONDITION_FAILED);
    }

    @ExceptionHandler(OrderRequestProcessingException.class)
    public ResponseEntity<ErrorResponse> orderRequestProcessingExceptionHandler(OrderRequestProcessingException ex) {
        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setMessage(ex.getLocalizedMsg());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
