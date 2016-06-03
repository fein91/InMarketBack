package com.fein91.rest;

import com.fein91.model.OrderResult;
import com.fein91.model.OrderRequest;
import com.fein91.service.OrderRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/orderRequests")
public class OrderRequestController {

    @Autowired
    OrderRequestService orderRequestService;

    @RequestMapping(method = RequestMethod.POST, value = "/process")
    public OrderResult process(@RequestBody OrderRequest orderRequest) {
        return orderRequestService.processOrderRequest(orderRequest);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<OrderRequest> getByCounterpartyId(@RequestParam("counterpartyId") BigInteger counterpartyId) {
        return orderRequestService.getByCounterpartyId(counterpartyId);
    }


}
