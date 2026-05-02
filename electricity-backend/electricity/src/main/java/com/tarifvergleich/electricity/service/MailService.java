package com.tarifvergleich.electricity.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.tarifvergleich.electricity.exception.InternalServerException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MailService {
	private final JavaMailSender mailSender;

	@Value("${email.name}")
	private String sendFrom;

	@Async
	public void sendMail(String to, String subject, String body) {
		
		try {
			MimeMessage message = mailSender.createMimeMessage();
			
			MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
			helper.setFrom(sendFrom);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(body, true);

			mailSender.send(message);
		} catch (MessagingException e) {
			throw new InternalServerException("Failed to send HTML email", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@Async
	public void sendMailWithAttachment(String to, String subject, String body, String localFilePath) {
	    try {
	    	
	    	if(localFilePath == null || localFilePath.isEmpty())
	    		throw new InternalServerException("File path not found", HttpStatus.BAD_REQUEST);
	    	
	        MimeMessage message = mailSender.createMimeMessage();
	        
	        MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
	        
	        helper.setFrom(sendFrom);
	        helper.setTo(to);
	        helper.setSubject(subject);
	        helper.setText(body, true);

	        Path path = Paths.get(localFilePath);
	        if (Files.exists(path)) {
	            FileSystemResource file = new FileSystemResource(path.toFile());
	            
	            helper.addAttachment("Contract_Details.pdf", file);
	        }
	        else
	        	throw new InternalServerException("File path does not exits", HttpStatus.BAD_REQUEST);

	        mailSender.send(message);
	    } catch (MessagingException e) {
	        throw new InternalServerException("Failed to send email with attachment", HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
}
