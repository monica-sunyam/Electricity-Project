package com.tarifvergleich.electricity.service.admin;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.tarifvergleich.electricity.exception.InternalServerException;
import com.tarifvergleich.electricity.model.AdminAsset;
import com.tarifvergleich.electricity.model.AdminServiceMenu;
import com.tarifvergleich.electricity.model.AdminUser;
import com.tarifvergleich.electricity.repository.AdminAssetRepository;
import com.tarifvergleich.electricity.repository.AdminServiceMenuRepository;
import com.tarifvergleich.electricity.repository.AdminUserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ViewService {

	private final AdminAssetRepository adminAssetRepo;
	private final AdminServiceMenuRepository adminServiceMenuRepo;
	private final AdminUserRepository adminUserRepo;

	public Map<String, Object> getAllView() {

		AdminUser adminUser = adminUserRepo.findById(1)
				.orElseThrow(() -> new InternalServerException("Content not found", HttpStatus.BAD_REQUEST));

		List<AdminAsset> mainAdminAssets = adminUser.getAdminAssets();
		List<AdminServiceMenu> mainAdminServiceMenu = adminUser.getAdminServiceMenu();

		List<AdminAsset> sidebarMenu = new LinkedList<AdminAsset>();
		List<AdminAsset> homeBanner = new LinkedList<AdminAsset>();
		List<AdminAsset> aboutUs = new LinkedList<AdminAsset>();
		List<AdminAsset> navigationMenu = new LinkedList<AdminAsset>();

		mainAdminAssets.forEach(content -> {
			if (content.getType().equals(1))
				navigationMenu.add(content);
			else if (content.getType().equals(2))
				sidebarMenu.add(content);
			else if (content.getType().equals(3))
				homeBanner.add(content);
			else if (content.getType().equals(4))
				aboutUs.add(content);
		});

		List<AdminServiceMenu> freeService = new LinkedList<AdminServiceMenu>();
		List<AdminServiceMenu> otherService = new LinkedList<AdminServiceMenu>();

		mainAdminServiceMenu.forEach(service -> {
			if (service.getType().equals(1))
				freeService.add(service);
			else if (service.getType().equals(2))
				otherService.add(service);
		});

		return Map.of("res", true, "menu",
				Map.of("nav", navigationMenu,"sidebar", sidebarMenu,"banner", homeBanner,
						"about", aboutUs),
				"service", Map.of("free-service", freeService, "other-service", otherService));
	}

}
