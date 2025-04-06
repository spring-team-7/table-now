package org.example.tablenow.domain.category.repository;

import org.example.tablenow.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
