package com.tarifvergleich.electricity.repository;

import com.tarifvergleich.electricity.model.CustomerQueryContactCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerQueryContactCategoryRepository extends JpaRepository<CustomerQueryContactCategory, Integer> {
}
