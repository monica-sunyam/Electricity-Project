package com.tarifvergleich.electricity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tarifvergleich.electricity.model.AdminLoginHistory;

public interface AdminLoginHistoryRepository extends JpaRepository<AdminLoginHistory, Integer> {

}
