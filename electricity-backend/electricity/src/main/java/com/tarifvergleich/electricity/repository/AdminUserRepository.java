package com.tarifvergleich.electricity.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tarifvergleich.electricity.model.AdminUser;

@Repository
public interface AdminUserRepository extends  JpaRepository<AdminUser, Integer>{

	Optional<AdminUser> findByEmail(String email);
}
