package com.fein91.dao;

import com.fein91.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 *
 */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findOneByName(String name);
}
