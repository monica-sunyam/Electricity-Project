package com.tarifvergleich.electricity.service;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.tarifvergleich.electricity.dto.AdminAssetDto;
import com.tarifvergleich.electricity.dto.AdminServiceMenuDto;
import com.tarifvergleich.electricity.exception.InternalServerException;
import com.tarifvergleich.electricity.model.AdminAsset;
import com.tarifvergleich.electricity.model.AdminServiceMenu;
import com.tarifvergleich.electricity.model.AdminUser;
import com.tarifvergleich.electricity.repository.AdminAssetRepository;
import com.tarifvergleich.electricity.repository.AdminServiceMenuRepository;
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
	private final AdminServiceMenuRepository adminServiceMenuRepo;

	@Transactional
	public Map<String, Object> addAsset(AdminAssetDto assetDto, MultipartFile file) {

		if (assetDto.getAdminId() == null || assetDto.getAdminId() == 0)
			throw new InternalServerException("Admin id not found", HttpStatus.BAD_REQUEST);

		AdminUser adminUser = adminUserRepo.findById(assetDto.getAdminId())
				.orElseThrow(() -> new InternalServerException("Invalid admin id", HttpStatus.BAD_REQUEST));

		if (assetDto.getType() == null || assetDto.getType() <= 0)
			throw new InternalServerException("Content type not found", HttpStatus.BAD_REQUEST);

		String fileUrl = null;

		String contentType = "";
		int type = assetDto.getType();

		if (type == 1)
			contentType = "navigation-menu";
		else if (type == 2)
			contentType = "sidebar-menu";
		else if (type == 3)
			contentType = "home-banner";
		else if (type == 4)
			contentType = "about-us";

		if (file != null) {
			fileUrl = fileUtil.saveFile(file, contentType);

			if (fileUrl == null || fileUrl.isEmpty())
				throw new InternalServerException("File cannot be saved", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		if (assetDto.getId() != null && assetDto.getId() != 0) {
			AdminAsset adminAsset = adminAssetRepo.findById(assetDto.getId()).orElseThrow(
					() -> new InternalServerException("Content not found with the id", HttpStatus.BAD_REQUEST));

			String oldContentUrl = adminAsset.getContentUrl();

			if (fileUrl != null) {
				adminAsset.setContentUrl(fileUrl);
			}

			adminAsset.setUpdatedOn(BigInteger.valueOf(Instant.now().getEpochSecond()));
			adminAsset.setType(type);
			adminAsset.setHeading(assetDto.getHeading());
			adminAsset.setSubHeading(assetDto.getSubHeading());
			if (assetDto.getOrder() != null && assetDto.getOrder() > 0)
				adminAsset.setOrder(assetDto.getOrder());
			if (file != null)
				adminAsset.setOriginalFileName(file.getOriginalFilename());
			adminAsset.setSaving(assetDto.getSaving());
			adminAsset.setSavingPriceDetail(assetDto.getSavingDetail());
			adminAsset.setContactNumber(assetDto.getContact());

			AdminAsset updateAsset = adminAssetRepo.save(adminAsset);

			if (fileUrl != null)
				fileUtil.deleteFile(oldContentUrl);

			return Map.of("res", true, "Data", updateAsset);
		}

		int order = adminUser.getAdminAssets().stream().filter(content -> content.getType() == assetDto.getType())
				.mapToInt(AdminAsset::getOrder).max().orElse(0) + 1;

		AdminAsset asset = AdminAsset.builder().adminId(adminUser).heading(assetDto.getHeading())
				.subHeading(assetDto.getSubHeading()).type(type).contentUrl(fileUrl).saving(assetDto.getSaving())
				.savingPriceDetail(assetDto.getSavingDetail())
				.contactNumber(assetDto.getContact())
				.order(order)
				.originalFileName(file != null ? file.getOriginalFilename() : null).build();

		adminUser.addAdminAsset(asset);

		adminUser = adminUserRepo.save(adminUser);

		return Map.of("res", true, "Data", adminUser.getAdminAssets().getLast());
	}

	public Map<String, Object> getAllAssets(Integer id, Integer type) {
		if (id == null || id == 0)
			throw new InternalServerException("Invalid Admin id", HttpStatus.BAD_REQUEST);

		AdminUser adminUser = adminUserRepo.findById(id)
				.orElseThrow(() -> new InternalServerException("Admin not found", HttpStatus.BAD_REQUEST));

		if (type != null && type != 0) {
			List<AdminAsset> adminAssets = adminUser.getAdminAssets().stream()
					.filter(asset -> asset.getType().equals(type)).toList();

			return Map.of("res", true, "data", adminAssets != null ? adminAssets : List.of());
		}

		return Map.of("res", true, "data", adminUser.getAdminAssets());
	}

	@Transactional
	public Map<String, Object> deleteAsset(Integer adminId, Integer id) {
		if (adminId == null || adminId == 0)
			throw new InternalServerException("Admin not found", HttpStatus.BAD_REQUEST);

		AdminUser adminUser = adminUserRepo.findById(adminId)
				.orElseThrow(() -> new InternalServerException("Admin not found", HttpStatus.BAD_REQUEST));

		AdminAsset assetToDelete = adminUser.getAdminAssets().stream().filter(content -> content.getId().equals(id))
				.findFirst().orElseThrow(() -> new InternalServerException("Menu not found", HttpStatus.BAD_REQUEST));

		String oldContentUrl = assetToDelete.getContentUrl();

		adminUser.getAdminAssets().remove(assetToDelete);

		adminUserRepo.save(adminUser);

		if (oldContentUrl != null)
			fileUtil.deleteFile(oldContentUrl);

		return Map.of("res", true);
	}

	@Transactional
	public Map<String, Object> addServiceMenu(AdminServiceMenuDto menuDto, MultipartFile file) {

		if (menuDto.getAdminId() == null || menuDto.getAdminId() == 0)
			throw new InternalServerException("Admin not found", HttpStatus.BAD_REQUEST);

		AdminUser adminUser = adminUserRepo.findById(menuDto.getAdminId())
				.orElseThrow(() -> new InternalServerException("Invalid admin id", HttpStatus.BAD_REQUEST));

		if (menuDto.getHeading() == null || menuDto.getHeading().isEmpty())
			throw new InternalServerException("Heading missing", HttpStatus.BAD_REQUEST);

		if (menuDto.getType() == null || menuDto.getType() <= 0 || menuDto.getType() > 2)
			throw new InternalServerException("Type undefined", HttpStatus.BAD_REQUEST);

		String contentType = "";

		if (menuDto.getType() == 1)
			contentType = "free-service";
		else if (menuDto.getType() == 2)
			contentType = "other-service";

		String fileUrl = null;

		if (file != null) {
			fileUrl = fileUtil.saveFile(file, contentType);
		}

		if (menuDto.getId() != null && menuDto.getId() != 0) {

			AdminServiceMenu adminServiceMenu = adminServiceMenuRepo.findById(menuDto.getId())
					.orElseThrow(() -> new InternalServerException("Record not found", HttpStatus.BAD_REQUEST));

			String oldContentUrl = adminServiceMenu.getContentUrl();

			adminServiceMenu.setHeading(menuDto.getHeading());
			adminServiceMenu.setSubheading(menuDto.getSubheading());
			adminServiceMenu.setType(menuDto.getType());
			if (menuDto.getType() == 1 && menuDto.getHighlight() != null && menuDto.getHighlight() == 1)
				adminServiceMenu.setHighlight(1);
			else
				adminServiceMenu.setHighlight(0);
			if (fileUrl != null) {
				adminServiceMenu.setContentUrl(fileUrl);
				adminServiceMenu.setOriginalFileName(file.getOriginalFilename());
			}
			adminServiceMenu.setUpdatedOn(BigInteger.valueOf(Instant.now().getEpochSecond()));
			adminServiceMenu.setOrder(menuDto.getOrder());

			AdminServiceMenu updatedServiceMenu = adminServiceMenuRepo.save(adminServiceMenu);

			if (fileUrl != null)
				fileUtil.deleteFile(oldContentUrl);

			return Map.of("res", true, "data", updatedServiceMenu);

		}

		int order = adminUser.getAdminServiceMenu().stream().filter(content -> content.getType() == menuDto.getType())
				.mapToInt(AdminServiceMenu::getOrder).max().orElse(0) + 1;

		int hightlight = menuDto.getType() == 1 && menuDto.getHighlight() != null && menuDto.getHighlight() == 1 ? 1
				: 0;

		AdminServiceMenu newRecord = AdminServiceMenu.builder().contentUrl(fileUrl).adminId(adminUser)
				.heading(menuDto.getHeading()).subheading(menuDto.getSubheading()).type(menuDto.getType())
				.highlight(hightlight).originalFileName(fileUrl != null ? file.getOriginalFilename() : null)
				.order(order).build();

		adminUser.addAdminServiceMenu(newRecord);

		adminUser = adminUserRepo.save(adminUser);

		return Map.of("res", true, "Data", adminUser.getAdminServiceMenu().getLast());
	}

	public Map<String, Object> getAllAdminServiceMenu(Integer adminId, Integer type, Integer highlight) {
		if (adminId == null || adminId <= 0)
			throw new InternalServerException("Admin not found", HttpStatus.BAD_REQUEST);

		AdminUser adminUser = adminUserRepo.findById(adminId)
				.orElseThrow(() -> new InternalServerException("Admin not found", HttpStatus.BAD_REQUEST));

		List<AdminServiceMenu> adminServiceMenus = adminUser.getAdminServiceMenu();

		if (type != null)
			adminServiceMenus = adminServiceMenus.stream().filter(service -> service.getType().equals(type)).toList();

		if (highlight != null)
			adminServiceMenus = adminServiceMenus.stream().filter(service -> service.getHighlight().equals(highlight))
					.toList();

		return Map.of("res", true, "data", adminServiceMenus);
	}

	public Map<String, Object> deleteService(Integer adminId, Integer id) {
		if (adminId == null || adminId == 0)
			throw new InternalServerException("Admin not found", HttpStatus.BAD_REQUEST);

		AdminUser adminUser = adminUserRepo.findById(adminId)
				.orElseThrow(() -> new InternalServerException("Admin not found", HttpStatus.BAD_REQUEST));

		AdminServiceMenu removeService = adminUser.getAdminServiceMenu().stream()
				.filter(content -> content.getId().equals(id)).findFirst()
				.orElseThrow(() -> new InternalServerException("Service not found", HttpStatus.BAD_REQUEST));

		String oldContentUrl = removeService.getContentUrl();

		adminUser.getAdminServiceMenu().remove(removeService);

		adminUserRepo.save(adminUser);

		if (oldContentUrl != null)
			fileUtil.deleteFile(oldContentUrl);

		return Map.of("res", true);
	}
}
