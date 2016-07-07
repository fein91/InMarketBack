package com.fein91.auth;

import com.fein91.dao.CounterpartyRepository;
import com.fein91.model.Counterparty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 *
 */
@RestController
public class AuthController {

    @Autowired
    private CounterpartyRepository counterpartyRepository;

    @RequestMapping("/user")
    @ResponseBody
    public Counterparty user(Principal user) {
        Counterparty byLogin = counterpartyRepository.findByLogin(user.getName());
        return byLogin;
    }

}
