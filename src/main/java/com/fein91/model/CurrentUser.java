package com.fein91.model;

import org.springframework.security.core.authority.AuthorityUtils;

/**
 *
 */
public class CurrentUser extends org.springframework.security.core.userdetails.User{
    private User user;

    public CurrentUser(User user) {
        super(user.getName(), user.getPasswordHash(), AuthorityUtils.createAuthorityList(user.getRole().toString()));
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public Long getId() {
        return user.getId();
    }

    public User.Role getRole() {
        return user.getRole();
    }
}
