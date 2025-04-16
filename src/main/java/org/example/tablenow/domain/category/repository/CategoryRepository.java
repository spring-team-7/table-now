package org.example.tablenow.domain.category.repository;

import org.example.tablenow.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("""
            SELECT COUNT(c) > 0
            FROM Category c
            WHERE c.name = :name
            """)
    boolean existsByName(String name);
}
