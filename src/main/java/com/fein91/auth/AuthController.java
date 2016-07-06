package com.fein91.auth;

import com.fein91.dao.CounterpartyRepository;
import com.fein91.model.Counterparty;
import com.fein91.service.CounterPartyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        Counterparty byLogin = counterpartyRepository.findByLoginName(user.getName());
        return byLogin;
    }

}
