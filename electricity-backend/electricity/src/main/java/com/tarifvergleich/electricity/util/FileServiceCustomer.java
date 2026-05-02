package com.tarifvergleich.electricity.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.tarifvergleich.electricity.exception.InternalServerException;

@Service
public class FileServiceCustomer {
	private final Path rootLocation;

	public FileServiceCustomer(@Value("${file.upload-dir.customer}") String uploadDir) {
		this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
		try {
			Files.createDirectories(rootLocation);
		} catch (IOException e) {
			throw new RuntimeException("Could not initialize storage location", e);
		}
	}

	public String saveFile(MultipartFile file, String lookUpFolder) {
		try {
			if (file.isEmpty())
				throw new RuntimeException("Failed to store empty file.");

			String folderName = lookUpFolder.toLowerCase().trim();
			Path targetDir = this.rootLocation.resolve(folderName);

			String originalFileName = file.getOriginalFilename().replace(" ", "_").replaceAll("[^a-zA-Z0-9.-]", "_");

			if (!Files.exists(targetDir)) {
				Files.createDirectories(targetDir);
			}

			String fileName = UUID.randomUUID().toString() + "_" + originalFileName;

			Files.copy(file.getInputStream(), targetDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);

			return folderName + "/" + fileName;

		} catch (IOException e) {
			throw new RuntimeException("Failed to store file", e);
		}
	}

	public String saveBase64Pdf(String base64String, String fileName, String lookUpFolder) throws IOException {

		if (base64String == null || base64String.isEmpty() || lookUpFolder == null || lookUpFolder.isEmpty())
			throw new RuntimeException("Failed to store empty file.");

		String folderName = lookUpFolder.toLowerCase().trim();
		Path targetDir = this.rootLocation.resolve(folderName);

		if (!Files.exists(targetDir)) {
			Files.createDirectories(targetDir);
		}

		String fullFileName = UUID.randomUUID().toString() + "_" + fileName + ".pdf";

		byte[] decodedBytes = Base64.getMimeDecoder().decode(base64String.replaceAll("\\s", ""));

		Path filePath = targetDir.resolve(fullFileName);

		Files.write(filePath, decodedBytes);

		return folderName + "/" + fileName;
	}

	public Resource loadFile(String relativePath) {
		try {
			Path file = rootLocation.resolve(relativePath).normalize();
			Resource resource = new UrlResource(file.toUri());

			if (resource.exists() || resource.isReadable()) {
				return resource;
			}
			throw new RuntimeException("Could not read file: " + relativePath);
		} catch (MalformedURLException e) {
			throw new RuntimeException("Error: " + e.getMessage());
		}
	}

	public String getAbsolutePath(String relativePath) {
		if (relativePath == null || relativePath.isEmpty())
			throw new InternalServerException("Relative path not found", HttpStatus.BAD_REQUEST);

		String absolutePath = rootLocation.resolve(relativePath).toString();

		return absolutePath;
	}

	public void deleteFile(String relativePath) {
		try {
			Path file = rootLocation.resolve(relativePath).normalize();
			Files.deleteIfExists(file);
		} catch (IOException e) {
			throw new RuntimeException("Error deleting file: " + relativePath);
		}
	}
}
