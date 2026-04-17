package com.tarifvergleich.electricity.util;

import org.springframework.stereotype.Component;

@Component
public class EmailTemplate {

	public String createOtpEmailBody(String name, String otp) {
	    return "<div style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
	           "<h2>Welcome to Tarifvergleich Electricity!</h2>" +
	           "<p>Hello " + name + ",</p>" +
	           "<p>Thank you for signing up. To complete your registration and start comparing energy tariffs, please use the following One-Time Password (OTP):</p>" +
	           "<div style='background: #0085e0; padding: 15px; text-align: center; font-size: 24px; font-weight: bold; letter-spacing: 5px; color: #2e7d32; border-radius: 5px;'>" +
	           otp +
	           "</div>"+
	           "<br>" +
	           "<p>Best Regards,<br><strong>Tarifvergleich Support Team</strong></p>" +
	           "</div>";
	}
	
	public String createForgotPasswordEmailBody(String name, String otp) {
	    return "<div style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
	           "<h2>Password Reset Request</h2>" +
	           "<p>Hello " + name + ",</p>" +
	           "<p>We received a request to reset the password for your <strong>Tarifvergleich Electricity</strong> account.</p>" +
	           "<p>Please use the following One-Time Password (OTP) to proceed with resetting your password:</p>" +
	           "<div style='background: #f4f4f4; padding: 15px; text-align: center; font-size: 24px; font-weight: bold; letter-spacing: 5px; color: #d32f2f; border-radius: 5px;'>" +
	           otp +
	           "</div>" +
//	           "<p><strong>Note:</strong> This code is valid for 10 minutes. If you did not request a password reset, you can safely ignore this email; your account remains secure.</p>" +
	           "<br>" +
	           "<p>Best Regards,<br><strong>Tarifvergleich Security Team</strong></p>" +
	           "</div>";
	}
}
