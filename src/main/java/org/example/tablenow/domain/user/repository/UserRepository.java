package org.example.tablenow.domain.user.repository;

import org.example.tablenow.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
}
