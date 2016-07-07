package com.fein91.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fein91.model.Counterparty;
import com.fein91.service.CounterPartyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class RestAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private CounterPartyService counterPartyService;
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response, Authentication authentication)
            throws ServletException, IOException {
        //Counterparty counterparty = counterPartyService.findByLogin(authentication.getName());

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        //writer.write(mapper.writeValueAsString(counterparty));
        response.setStatus(HttpServletResponse.SC_OK);
        writer.flush();
        writer.close();
    }
}