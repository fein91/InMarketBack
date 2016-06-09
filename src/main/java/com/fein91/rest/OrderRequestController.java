package com.fein91.rest;

import com.fein91.model.OrderResult;
import com.fein91.model.OrderRequest;
import com.fein91.service.OrderRequestService;
import com.fein91.service.OrderRequestServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/orderRequests")
public class OrderRequestController {

    @Autowired
    @Qualifier("OrderRequestServiceImpl")
    OrderRequestService orderRequestServiceImpl;

    @RequestMapping(method = RequestMethod.POST, value = "/process")
    public OrderResult process(@RequestBody OrderRequest orderRequest) {
        return orderRequestServiceImpl.processOrderRequest(orderRequest);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/calculate")
    public OrderResult calculate(@RequestBody OrderRequest orderRequest) {
        return orderRequestServiceImpl.calculateOrderRequest(orderRequest);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<OrderRequest> getByCounterpartyId(@RequestParam("counterpartyId") Long counterpartyId) {
        return orderRequestServiceImpl.getByCounterpartyId(counterpartyId);
    }


}
