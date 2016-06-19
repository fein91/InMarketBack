package com.fein91.service;

import com.fein91.model.User;

import java.util.Collection;
import java.util.Optional;

/**
 *
 */
public interface UserService {

    Optional<User> getUserById(long id);

    Optional<User> getUserByName(String name);

    Collection<User> getAllUsers();
}
