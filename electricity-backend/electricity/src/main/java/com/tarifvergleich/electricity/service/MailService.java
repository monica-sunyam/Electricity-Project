package com.tarifvergleich.electricity.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
}
