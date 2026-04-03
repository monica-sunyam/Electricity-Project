package com.tarifvergleich.electricity.service;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.tarifvergleich.electricity.dto.AdminAssetDto;
import com.tarifvergleich.electricity.exception.InternalServerException;
import com.tarifvergleich.electricity.model.AdminAsset;
import com.tarifvergleich.electricity.model.AdminUser;
import com.tarifvergleich.electricity.repository.AdminAssetRepository;
import com.tarifvergleich.electricity.repository.AdminUserRepository;
import com.tarifvergleich.electricity.util.FileServiceSuperAdmin;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminAssetService {

	private final AdminUserRepository adminUserRepo;
	private final AdminAssetRepository adminAssetRepo;
	private final FileServiceSuperAdmin fileUtil;

	@Transactional
	public Map<String, Object> addAsset(AdminAssetDto assetDto, MultipartFile file) {

		if (assetDto.getAdminId() == null || assetDto.getAdminId() == 0)
			throw new InternalServerException("Admin id not found", HttpStatus.BAD_REQUEST);

		AdminUser adminUser = adminUserRepo.findById(assetDto.getAdminId())
				.orElseThrow(() -> new InternalServerException("Invalid admin id", HttpStatus.BAD_REQUEST));

		if (assetDto.getContentType() == null || assetDto.getContentType().isEmpty())
			throw new InternalServerException("Content type not found", HttpStatus.BAD_REQUEST);

		if (assetDto.getContentPlace() == null || assetDto.getContentPlace().isEmpty())
			throw new InternalServerException("Content place not found", HttpStatus.BAD_REQUEST);

		int order = adminUser.getAdminAssets() != null ? adminUser.getAdminAssets().size() : 1;

		String fileUrl = fileUtil.saveFile(file, assetDto.getContentType());

		if (fileUrl == null || fileUrl.isEmpty())
			throw new InternalServerException("File cannot be saved", HttpStatus.INTERNAL_SERVER_ERROR);

		AdminAsset asset = AdminAsset.builder().adminId(adminUser).heading(assetDto.getHeading())
				.subHeading(assetDto.getSubHeading()).contentType(assetDto.getContentType()).contentUrl(fileUrl)
				.place(assetDto.getContentPlace()).order(order).originalFileName(file.getOriginalFilename()).build();

		adminUser.addAdminAsset(asset);

		adminUser = adminUserRepo.save(adminUser);

		return Map.of("res", true, "Data", adminUser.getAdminAssets().getLast());
	}

	public Map<String, Object> getAllAssets(Integer id) {
		if (id == null || id == 0)
			throw new InternalServerException("Invalid Admin id", HttpStatus.BAD_REQUEST);

		AdminUser adminUser = adminUserRepo.findById(id)
				.orElseThrow(() -> new InternalServerException("Admin not found", HttpStatus.BAD_REQUEST));

		return Map.of("res", true, "data", adminUser.getAdminAssets());
	}
}
