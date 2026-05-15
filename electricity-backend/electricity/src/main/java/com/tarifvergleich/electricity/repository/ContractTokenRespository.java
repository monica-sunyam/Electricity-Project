package com.tarifvergleich.electricity.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tarifvergleich.electricity.model.ContractToken;

@Repository
public interface ContractTokenRespository extends JpaRepository<ContractToken, Integer> {

	Optional<ContractToken> findByToken(String token);
}
