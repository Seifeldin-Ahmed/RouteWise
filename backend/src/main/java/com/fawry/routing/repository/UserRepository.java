package com.fawry.routing.repository;

import com.fawry.routing.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

    User findUserByEmail(String email);

    boolean existsByEmail(String email);
}
