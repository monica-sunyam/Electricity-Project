package com.tarifvergleich.electricity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tarifvergleich.electricity.model.AdminAsset;

@Repository
public interface AdminAssetRepository extends JpaRepository<AdminAsset, Integer> {

}
