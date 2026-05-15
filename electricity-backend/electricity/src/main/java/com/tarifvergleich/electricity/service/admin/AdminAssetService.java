package com.tarifvergleich.electricity.service.admin;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.tarifvergleich.electricity.dto.AdminAssetDto;
import com.tarifvergleich.electricity.dto.AdminAssetDto.AdminAssetSuffleDto;
import com.tarifvergleich.electricity.dto.AdminServiceMenuDto;
import com.tarifvergleich.electricity.dto.AdminSignatureDto;
import com.tarifvergleich.electricity.dto.AdminSignatureDto.AdminSignatureResponseDto;
import com.tarifvergleich.electricity.dto.ManageAdminDocumentDto;
import com.tarifvergleich.electricity.exception.InternalServerException;
import com.tarifvergleich.electricity.model.AdminAsset;
import com.tarifvergleich.electricity.model.AdminServiceMenu;
import com.tarifvergleich.electricity.model.AdminSignature;
import com.tarifvergleich.electricity.model.AdminUser;
import com.tarifvergleich.electricity.model.ManageAdminDocument;
import com.tarifvergleich.electricity.repository.AdminAssetRepository;
import com.tarifvergleich.electricity.repository.AdminServiceMenuRepository;
import com.tarifvergleich.electricity.repository.AdminSignatureRepository;
import com.tarifvergleich.electricity.repository.AdminUserRepository;
import com.tarifvergleich.electricity.repository.ManageAdminDocumentRepository;
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
	private final ManageAdminDocumentRepository manageAdminDocumentRepo;
	private final AdminSignatureRepository adminSignatureRepo;

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

			if (fileUrl != null && oldContentUrl != null) {
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
		} else if (type == 4 && assetDto.getAdminId() != null) {
			try {
				AdminAsset adminAsset = adminUser.getAdminAssets().stream().filter(menu -> menu.getType().equals(type))
						.findFirst().orElseThrow(
								() -> new InternalServerException("Menu of type 4 not found", HttpStatus.BAD_REQUEST));
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

				if (fileUrl != null && oldContentUrl != null)
					fileUtil.deleteFile(oldContentUrl);

				return Map.of("res", true, "Data", updateAsset);

			} catch (InternalServerException e) {
				System.err.println(e.toString());
			}
		}

		int order = adminUser.getAdminAssets().stream().filter(content -> content.getType() == assetDto.getType())
				.mapToInt(AdminAsset::getOrder).max().orElse(0) + 1;

		AdminAsset asset = AdminAsset.builder().adminId(adminUser).heading(assetDto.getHeading())
				.subHeading(assetDto.getSubHeading()).type(type).contentUrl(fileUrl).saving(assetDto.getSaving())
				.savingPriceDetail(assetDto.getSavingDetail()).contactNumber(assetDto.getContact()).order(order)
				.originalFileName(file != null ? file.getOriginalFilename() : null).build();

		adminUser.addAdminAsset(asset);

		adminUser = adminUserRepo.save(adminUser);

		return Map.of("res", true, "Data", adminUser.getAdminAssets().getLast());
	}

	public Map<String, Object> getSingleMenu(Integer id, Integer adminId) {
		if (id == null || id <= 0)
			throw new InternalServerException("Incorrect menu id", HttpStatus.BAD_REQUEST);

		if (adminId == 0 || adminId <= 0)
			throw new InternalServerException("Admin not found", HttpStatus.BAD_REQUEST);

		AdminUser admin = adminUserRepo.findById(adminId)
				.orElseThrow(() -> new InternalServerException("Admin not found", HttpStatus.BAD_REQUEST));

		AdminAsset asset = admin.getAdminAssets().stream().filter(menu -> menu.getId().equals(id)).findFirst()
				.orElseThrow(() -> new InternalServerException("Menu not found", HttpStatus.BAD_REQUEST));
		return Map.of("res", true, "data", asset);
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

		if (menuDto.getIsRedirect() == null)
			menuDto.setIsRedirect(false);

		if (menuDto.getIsRedirect() && (menuDto.getLink() == null || menuDto.getLink().isEmpty()))
			throw new InternalServerException("Link not found", HttpStatus.BAD_REQUEST);

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
			adminServiceMenu.setIsRedirect(menuDto.getIsRedirect());
			adminServiceMenu.setLink(menuDto.getLink());

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
				.isRedirect(menuDto.getIsRedirect()).link(menuDto.getLink()).order(order).build();

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

	public Map<String, Object> getSingleService(Integer adminId, Integer serviceId) {

		if (adminId == null || adminId <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);
		if (serviceId == null || serviceId <= 0)
			throw new InternalServerException("Service id missing", HttpStatus.OK);

		AdminServiceMenu adminServiceMenu = adminServiceMenuRepo.findByIdAndAdminIdAdminId(serviceId, adminId)
				.orElseThrow(
						() -> new InternalServerException("Service not found with this credential", HttpStatus.OK));

		return Map.of("res", true, "data", adminServiceMenu);
	}

	@Transactional
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

	@Transactional
	public Map<String, Object> suffleOrder(AdminAssetSuffleDto requestDto) {

		if (requestDto.getAdminId() == null || requestDto.getAdminId() <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);

		AdminUser adminUser = adminUserRepo.findById(requestDto.getAdminId())
				.orElseThrow(() -> new InternalServerException("Admin not found with this credential", HttpStatus.OK));

		requestDto.getMenu().forEach(menu -> {
			if (menu.getId() == null)
				throw new InternalServerException("Menu id missing", HttpStatus.OK);

			AdminAsset asset = adminAssetRepo.findById(menu.getId()).orElseThrow(
					() -> new InternalServerException("Menu not found with this credentials", HttpStatus.OK));

			if (!asset.getAdminId().getAdminId().equals(adminUser.getAdminId()))
				throw new InternalServerException("Admin asset not accessible", HttpStatus.OK);

			asset.setOrder(menu.getOrder());

			adminAssetRepo.save(asset);
		});

		return Map.of("res", true, "message", "Order updated");
	}

	@Transactional
	public Map<String, Object> addAdminDocument(ManageAdminDocumentDto documentDto, MultipartFile file) {

		if (documentDto == null)
			if (documentDto == null)
				throw new InternalServerException("Insufficient data", HttpStatus.OK);

		if (documentDto.getAdminId() == null || documentDto.getAdminId() <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);

		if (documentDto.getDocumentCategory() == null || documentDto.getDocumentCategory().isEmpty())
			throw new InternalServerException("Document category missing", HttpStatus.OK);

		if (documentDto.getAdminDocId() != null && documentDto.getAdminDocId() > 0) {
			Boolean addNewFile = false;
			ManageAdminDocument existingDoc = manageAdminDocumentRepo
					.findByIdAndAdminAdminId(documentDto.getAdminDocId(), documentDto.getAdminId())
					.orElseThrow(() -> new InternalServerException("Admin document not found with this credentials",
							HttpStatus.OK));

			if (file != null) {
				String relativePath = existingDoc.getFilePath();
				String newPath = fileUtil.saveFilePdf(file, "documents");

				if (newPath == null || newPath.isEmpty())
					throw new InternalServerException("Error saving file", HttpStatus.OK);

				existingDoc.setFilePath(newPath);
				existingDoc.setOriginalFileName(file.getOriginalFilename());

				existingDoc = manageAdminDocumentRepo.save(existingDoc);

				if (addNewFile)
					fileUtil.deleteFile(relativePath);
			} else {
				existingDoc.setDocumentCategory(documentDto.getDocumentCategory());
				existingDoc = manageAdminDocumentRepo.save(existingDoc);
			}

			return Map.of("res", true, "adminDocId", existingDoc.getId());
		}

		if (file == null)
			throw new InternalServerException("File missing", HttpStatus.OK);

		AdminUser admin = adminUserRepo.findById(documentDto.getAdminId())
				.orElseThrow(() -> new InternalServerException("Admin not found with this credential", HttpStatus.OK));

		String newFilePath = fileUtil.saveFilePdf(file, "documents");

		ManageAdminDocument newDoc = ManageAdminDocument.builder().filePath(newFilePath)
				.originalFileName(file.getOriginalFilename())
				.documentCategory(documentDto.getDocumentCategory().toUpperCase()).admin(admin).build();

		newDoc = manageAdminDocumentRepo.save(newDoc);

		return Map.of("res", true, "adminDocId", newDoc.getId());
	}

	public Map<String, Object> fetchAdminDocuments(Integer adminId, Integer page, Integer size) {
		if (adminId == null || adminId <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);

		if (page == null || page <= 0)
			page = 1;

		if (size == null || size <= 0)
			size = 5;

		Pageable pageable = PageRequest.of(page - 1, size, Sort.by("addedOn").descending());
		Page<ManageAdminDocument> documentPage = manageAdminDocumentRepo.findAllByAdminAdminId(adminId, pageable);

		List<ManageAdminDocumentDto.ManageAdminDocumentResDto> documents = documentPage.getContent().stream()
				.map(ManageAdminDocumentDto::mapForAdmin).toList();

		return Map.of("res", true, "data", documents, "page", page, "totalPage", documentPage.getTotalPages());
	}

	@Transactional
	public Map<String, Object> addAdminSignature(AdminSignatureDto signatureDto, MultipartFile file) {

		if (signatureDto.getAdminId() == null || signatureDto.getAdminId() <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);

		AdminUser admin = adminUserRepo.findById(signatureDto.getAdminId())
				.orElseThrow(() -> new InternalServerException("Admin not found with this credential", HttpStatus.OK));

		AdminSignature adminSignature = admin.getAdminSignatures();

		if (signatureDto.getAdminSignatureId() != null && signatureDto.getAdminSignatureId() > 0
				&& adminSignature != null) {

			if (!adminSignature.getId().equals(signatureDto.getAdminSignatureId()))
				throw new InternalServerException("Signature and admin mis-match", HttpStatus.OK);

			Boolean newFileAdded = false;

			String oldSignature = adminSignature.getFilePath();

			if (file != null && !file.isEmpty()) {
				if (!file.getContentType().equals("image/png") && !file.getContentType().equals("image/jpeg"))
					throw new InternalServerException("Content type mismatch", HttpStatus.OK);
				String newPath = fileUtil.saveFile(file, "signature");
				adminSignature.setFilePath(newPath);
				newFileAdded = true;
			}

			adminSignature.setOriginalFileName(file.getOriginalFilename());

			adminSignature = adminSignatureRepo.save(adminSignature);

			if (newFileAdded)
				fileUtil.deleteFile(oldSignature);

			return Map.of("res", true, "adminSignatureId", adminSignature.getId());
		}

		if (file == null || file.isEmpty())
			throw new InternalServerException("Signature missing", HttpStatus.OK);

		if (!file.getContentType().equals("image/png") && !file.getContentType().equals("image/jpeg"))
			throw new InternalServerException("Content type mismatch", HttpStatus.OK);

		String newFilePath = fileUtil.saveFile(file, "signature");

		adminSignature = AdminSignature.builder().filePath(newFilePath).originalFileName(file.getOriginalFilename())
				.admin(admin).build();

		adminSignature = adminSignatureRepo.save(adminSignature);

		return Map.of("res", true, "adminSignatureId", adminSignature.getId());
	}

	public Map<String, Object> fetchAdminSignature(AdminSignatureDto adminSignDto) {
		if (adminSignDto.getAdminId() == null || adminSignDto.getAdminId() <= 0)
			throw new InternalServerException("Admin id missing", HttpStatus.OK);

		AdminSignature adminSignature = adminSignatureRepo.findByAdminAdminId(adminSignDto.getAdminId()).orElseThrow(
				() -> new InternalServerException("Admin signature not found with this credential", HttpStatus.OK));

		AdminSignatureResponseDto signatureResponse = AdminSignatureDto.mapSignatureResponse(adminSignature);

		return Map.of("res", true, "data", signatureResponse);
	}
}
