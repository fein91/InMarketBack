package com.fein91.auth;

import com.fein91.model.CurrentUser;
import com.fein91.model.User;
import com.fein91.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

/**
 *
 */
@Service
public class CurrentUserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    @Autowired
    public CurrentUserDetailsServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        User user = userService.getUserByName(name).orElseThrow((Supplier<RuntimeException>)
                () -> new UsernameNotFoundException(name));
        return new CurrentUser(user);
    }
}
