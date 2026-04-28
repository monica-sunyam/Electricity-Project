package com.tarifvergleich.electricity.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tarifvergleich.electricity.model.AdminServiceMenu;

@Repository
public interface AdminServiceMenuRepository extends JpaRepository<AdminServiceMenu, Integer> {

	
	Optional<AdminServiceMenu> findByIdAndAdminIdAdminId(Integer id, Integer adminId);
}
