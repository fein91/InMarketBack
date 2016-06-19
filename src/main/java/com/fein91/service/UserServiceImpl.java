package com.fein91.service;

import com.fein91.dao.UserRepository;
import com.fein91.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

/**
 *
 */
@Component
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> getUserById(long id) {
        return Optional.ofNullable(userRepository.findOne(id));
    }

    @Override
    public Optional<User> getUserByName(String name) {
        return userRepository.findOneByName(name);
    }

    @Override
    public Collection<User> getAllUsers() {
        return userRepository.findAll(new Sort("name"));
    }
}
