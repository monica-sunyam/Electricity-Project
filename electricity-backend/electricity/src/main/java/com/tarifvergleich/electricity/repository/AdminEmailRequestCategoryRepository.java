package com.tarifvergleich.electricity.repository;

import com.tarifvergleich.electricity.model.AdminEmailRequestCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminEmailRequestCategoryRepository extends JpaRepository<AdminEmailRequestCategory, Long> {
}