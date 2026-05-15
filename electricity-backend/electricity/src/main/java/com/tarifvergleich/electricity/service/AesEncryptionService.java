package com.tarifvergleich.electricity.service;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AesEncryptionService {

	private static final String ALGORITHM = "AES/GCM/NoPadding";
	private static final int TAG_LENGTH_BIT = 128;
	private static final int IV_LENGTH_BYTE = 12;

	@Value("${app.secrets.aesKey}")
	private String base64SecretKey;

	public String encrypt(String plainText) throws Exception {

		byte[] decodedKey = base64SecretKey.getBytes();
		SecretKey key = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

		byte[] iv = new byte[IV_LENGTH_BYTE];
		new SecureRandom().nextBytes(iv);

		Cipher cipher = Cipher.getInstance(ALGORITHM);
		GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
		cipher.init(Cipher.ENCRYPT_MODE, key, spec);

		byte[] cipherText = cipher.doFinal(plainText.getBytes());

		ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
		byteBuffer.put(iv);
		byteBuffer.put(cipherText);

		return Base64.getEncoder().encodeToString(byteBuffer.array());
	}

	public String decrypt(String encryptedText) throws Exception {

		byte[] decoded = Base64.getDecoder().decode(encryptedText);
		byte[] decodedKey = base64SecretKey.getBytes();
		SecretKey key = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

		ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
		byte[] iv = new byte[IV_LENGTH_BYTE];
		byteBuffer.get(iv);

		byte[] cipherText = new byte[byteBuffer.remaining()];
		byteBuffer.get(cipherText);

		Cipher cipher = Cipher.getInstance(ALGORITHM);
		GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
		cipher.init(Cipher.DECRYPT_MODE, key, spec);

		return new String(cipher.doFinal(cipherText));
	}

}
