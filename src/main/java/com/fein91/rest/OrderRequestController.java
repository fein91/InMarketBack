package com.fein91.rest;

import com.fein91.model.OrderRequest;
import com.fein91.service.OrderRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orderRequest")
public class OrderRequestController {

    @Autowired
    OrderRequestService orderRequestService;

    @RequestMapping(method = RequestMethod.POST, value = "/process")
    public void process(@RequestBody OrderRequest orderRequest) {
        orderRequestService.processOrderRequest(orderRequest);
    }
}
