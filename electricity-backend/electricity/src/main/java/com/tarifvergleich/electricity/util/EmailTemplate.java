package com.tarifvergleich.electricity.util;

import org.springframework.stereotype.Component;

@Component
public class EmailTemplate {

	public String createOtpEmailBody(String name, String otp) {
		return "<div style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>"
				+ "<h2>Welcome to Tarifvergleich Electricity!</h2>" + "<p>Hello " + name + ",</p>"
				+ "<p>Thank you for signing up. To complete your registration and start comparing energy tariffs, please use the following One-Time Password (OTP):</p>"
				+ "<div style='background: #0085e0; padding: 15px; text-align: center; font-size: 24px; font-weight: bold; letter-spacing: 5px; color: #2e7d32; border-radius: 5px;'>"
				+ otp + "</div>" + "<br>" + "<p>Best Regards,<br><strong>Tarifvergleich Support Team</strong></p>"
				+ "</div>";
	}

	public String createForgotPasswordEmailBody(String name, String otp) {
		return "<div style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>"
				+ "<h2>Password Reset Request</h2>" + "<p>Hello " + name + ",</p>"
				+ "<p>We received a request to reset the password for your <strong>Tarifvergleich Electricity</strong> account.</p>"
				+ "<p>Please use the following One-Time Password (OTP) to proceed with resetting your password:</p>"
				+ "<div style='background: #f4f4f4; padding: 15px; text-align: center; font-size: 24px; font-weight: bold; letter-spacing: 5px; color: #d32f2f; border-radius: 5px;'>"
				+ otp + "</div>" +
//	           "<p><strong>Note:</strong> This code is valid for 10 minutes. If you did not request a password reset, you can safely ignore this email; your account remains secure.</p>" +
				"<br>" + "<p>Best Regards,<br><strong>Tarifvergleich Security Team</strong></p>" + "</div>";
	}

	public String createPasswordResetSuccessEmailBody(String salutation, String lastName, String firstName,
			String email, String dateTime) {
		return "<div style='font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; border: 1px solid #eee; padding: 20px;'>"
				+ "<h2 style='color: #2e7d32;'>You have successfully reset your password!</h2>" + "<p>Dear "
				+ salutation + " " + lastName + ",</p>" + "<p>" + firstName + " " + lastName + "</p>"
				+ "<p>You successfully reset your password for the email address <strong>" + email + "</strong> on "
				+ dateTime + ".</p>" +

				"<p>If you changed your password yourself, you don't need to do anything else.</p>" +

				"<div style='margin-top: 25px; padding: 15px; background-color: #fff3e0; border-left: 4px solid #ff9800;'>"
				+ "<p style='margin: 0; font-weight: bold;'>You did not carry out this action yourself?</p>"
				+ "<p style='margin: 10px 0 0 0;'>In this case, please reset your password immediately to secure your account.</p>"
				+ "</div>" +

				"<br>" + "<p>Best Regards,<br><strong>Tarifvergleich Security Team</strong></p>" + "</div>";
	}

	public String createServiceRequestOpenedEmailBody(String salutation, String lastName, String firstName,
			String ticketNumber, String serviceType, String email, String dateTime, String content) {

		return "<div style='font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; border: 1px solid #eee; padding: 20px;'>"
				+ "<h2 style='color: #2e7d32;'>You have opened a service request!</h2>"
				+ "<p style='font-size: 1.1em; font-weight: bold; margin-bottom: 20px;'>Ticket-Nr. " + ticketNumber
				+ "</p>" +

				"<p>Dear " + salutation + " " + lastName + ",</p>" + "<p>" + firstName + " " + lastName + "</p>" +

				"<p>On " + dateTime + ", you opened a service request regarding <strong>" + serviceType + "</strong> "
				+ "via the email address <strong>" + email + "</strong> with the following content:</p>" +

				"<div style='background-color: #f9f9f9; border-left: 4px solid #2e7d32; padding: 15px; margin: 20px 0; font-style: italic;'>"
				+ content + "</div>" +

				"<p>This request will be processed promptly by our consultant.</p>" +

				"<p>If you would like to reply to this message or add something, please log in to your customer account and "
				+ "send us the information under the <strong>\"Service Requests\"</strong> section.</p>" +

				"<p>If you sent this service request, you don't need to do anything else.</p>" +

				"<div style='margin-top: 25px; padding: 15px; background-color: #fff3e0; border-left: 4px solid #ff9800;'>"
				+ "<p style='margin: 0; font-weight: bold;'>You did not carry out this action yourself?</p>"
				+ "<p style='margin: 10px 0 0 0;'>Please check all the information in your customer account and, if necessary, reset your password.</p>"
				+ "</div>" +

				"<br>" + "<p>Best Regards,<br><strong>Tarifvergleich Team</strong></p>" + "</div>";
	}

	public String createServiceRequestReopenedEmailBody(String salutation, String lastName, String firstName,
			String ticketNumber, String originalDate, String serviceType, String email, String newContent) {

		return "<div style='font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; border: 1px solid #eee; padding: 20px;'>"
				+ "<h2 style='color: #2e7d32;'>You have reopened your closed service request!</h2>"
				+ "<p style='font-size: 1.1em; font-weight: bold; margin-bottom: 20px;'>Ticket-Nr. " + ticketNumber
				+ "</p>" +

				"<p>Dear " + salutation + " " + lastName + ",</p>" + "<p>" + firstName + " " + lastName + "</p>" +

				"<p>The service request you originally submitted on " + originalDate + " via the email address <strong>"
				+ email + "</strong> " + "regarding <strong>" + serviceType
				+ "</strong> has been reopened with the following content:</p>" +

				"<div style='background-color: #f9f9f9; border-left: 4px solid #ff9800; padding: 15px; margin: 20px 0; font-style: italic;'>"
				+ newContent + "</div>" +

				"<p>This request will be processed promptly by our consultant.</p>" +

				"<p>If you would like to reply to this message or add something, please log in to your customer account and "
				+ "send us the information under the <strong>\"Service Requests\"</strong> section.</p>" +

				"<p>If you sent this service request, you don't need to do anything else.</p>" +

				"<div style='margin-top: 25px; padding: 15px; background-color: #fff3e0; border-left: 4px solid #ff9800;'>"
				+ "<p style='margin: 0; font-weight: bold;'>You did not carry out this action yourself?</p>"
				+ "<p style='margin: 10px 0 0 0;'>Please check all the information in your customer account and, if necessary, reset your password.</p>"
				+ "</div>" +

				"<br>" + "<p>Best Regards,<br><strong>Tarifvergleich Team</strong></p>" + "</div>";
	}

	public String createServiceRequestResponseEmailBody(String salutation, String lastName, String firstName,
			String ticketNumber, String requestDate, String email, String responseMessage, String consultantName) {

		return "<div style='font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; border: 1px solid #eee; padding: 20px;'>"
				+ "<h2 style='color: #2e7d32;'>Response to your service request</h2>"
				+ "<p style='font-size: 1.1em; font-weight: bold; margin-bottom: 20px;'>Ticket-Nr. " + ticketNumber
				+ "</p>" +

				"<p>Dear " + salutation + " " + lastName + ",</p>" + "<p>" + firstName + " " + lastName + "</p>" +

				"<p>Thank you for your service request of " + requestDate + " via the email address <strong>" + email
				+ "</strong>.</p>" +

				"<div style='margin: 20px 0; padding: 15px; background-color: #f1f8e9; border-left: 4px solid #2e7d32;'>"
				+ "<p style='margin: 0;'>" + responseMessage + "</p>" + "</div>" +

				"<p>If you would like to reply to this message or add something, please log in to your customer account and "
				+ "send us the information under the <strong>\"Service Requests\"</strong> section.</p>" +

				"<br>" + "<p>Best regards,<br>" + "<strong>" + consultantName + "</strong><br>" + "Customer advisor</p>"
				+

				"<hr style='border: 0; border-top: 1px solid #eee; margin-top: 30px;'>"
				+ "<p style='font-size: 0.85em; color: #777;'>Tarifvergleich.Bayern - Your energy experts</p>"
				+ "</div>";
	}

	public String createNewMessageNotificationToCustomerBody(String salutation, String lastName, String firstName,
			String ticketNumber, String serviceType, String messageDate, String messageContent) {

		String brandColor = "#fff3e0";

		return "<div style='font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; border: 1px solid #eee; padding: 20px;'>"
				+ "<h2 style='color: " + brandColor + ";'>New message from your consultant</h2>"
				+ "<p style='font-size: 1.1em; font-weight: bold; margin-bottom: 20px;'>Ticket-Nr. " + ticketNumber
				+ "</p>" +

				"<p>Dear " + salutation + " " + lastName + ",</p>" + "<p>" + firstName + " " + lastName + "</p>" +

				"<p>A consultant has added a new message to your service request regarding <strong>" + serviceType
				+ "</strong> on " + messageDate + ":</p>" +

				"<div style='background-color: #f5f5f5; border-left: 4px solid " + brandColor
				+ "; padding: 15px; margin: 20px 0;'>" + "<p style='margin: 0; font-style: italic;'>" + messageContent
				+ "</p>" + "</div>" +

				"<p>To reply or view the full conversation history, please log in to your customer account under the <strong>\"Service Requests\"</strong> section.</p>"
				+

				"<div style='margin-top: 25px; padding: 15px; background-color: #e8f5e9; border: 1px solid "
				+ brandColor + ";'>"
				+ "<p style='margin: 0; color: #2e7d32;'><strong>Information:</strong> Our team is working to resolve your request as quickly as possible.</p>"
				+ "</div>" +

				"<br>" + "<p>Best Regards,<br><strong>Tarifvergleich Team</strong></p>" + "</div>";
	}

	public String createAdminServiceRequestOpenedEmailBody(String adminName, String customerName, String ticketNumber,
			String serviceType, String content) {
		return "<div style='font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; border: 1px solid #eee; padding: 20px;'>"
				+ "<h2 style='color: #1976d2;'>Action Required: New Service Request</h2>" + "<p>Hello " + adminName
				+ ",</p>" + "<p>A new ticket has been opened by <strong>" + customerName + "</strong>.</p>"
				+ "<div style='background-color: #f5f5f5; padding: 15px; border-left: 4px solid #1976d2; margin: 15px 0;'>"
				+ "<strong>Ticket:</strong> " + ticketNumber + "<br>" + "<strong>Service:</strong> " + serviceType
				+ "<br><br>" + "<strong>Initial Message:</strong><br><em>" + content + "</em>" + "</div>"
				+ "<p>Please log in to the admin panel to assign this ticket or respond.</p>"
				+ "<br><p>Best Regards,<br><strong>Tarifvergleich System</strong></p></div>";
	}

	public String createAdminServiceRequestReopenedEmailBody(String adminName, String customerName, String ticketNumber,
			String content) {
		return "<div style='font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; border: 1px solid #ff9800; padding: 20px;'>"
				+ "<h2 style='color: #e65100;'>Urgent: Ticket Reopened</h2>" + "<p>Hello " + adminName + ",</p>"
				+ "<p>The following ticket has been <strong>reopened</strong> by " + customerName + ":</p>"
				+ "<div style='background-color: #fff3e0; padding: 15px; border-left: 4px solid #ff9800; margin: 15px 0;'>"
				+ "<strong>Ticket-Nr:</strong> " + ticketNumber + "<br><br>"
				+ "<strong>Customer Reason:</strong><br><em>" + content + "</em>" + "</div>"
				+ "<p>This requires immediate attention as the customer is following up on a closed matter.</p>"
				+ "</div>";
	}

	public String createAdminNewMessageNotificationBody(String adminName, String customerName, String ticketNumber,
			String content) {
		return "<div style='font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; border: 1px solid #eee; padding: 20px;'>"
				+ "<h2 style='color: #455a64;'>New Message Received</h2>" + "<p>Hello " + adminName + ",</p>"
				+ "<p>You have a new message from <strong>" + customerName + "</strong> regarding Ticket <strong>"
				+ ticketNumber + "</strong>.</p>"
				+ "<div style='background-color: #fcfcfc; border: 1px solid #eee; padding: 15px; margin: 15px 0; font-style: italic;'>"
				+ content + "</div>" + "<p>Log in to your advisor dashboard to view the full thread.</p>" + "</div>";
	}

	public String createCustomerConsentEmailBody(String salutation, String lastName, String encodedCustomerId) {
		String confirmationUrl = "http://192.168.0.155:8080/auth/mark-acknowledgement?token=" + encodedCustomerId;

		return "<div style='font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; border: 1px solid #eee; padding: 20px;'>"
				+ "<h2 style='color: #2e7d32;'>Action Required: Confirm Your Consent</h2>" + "<p>Dear " + salutation
				+ " " + lastName + ",</p>"
				+ "<p>Thank you for choosing <strong>Tarifvergleich Electricity</strong>. To finalize your energy tariff switch and process your delivery details, we require your formal consent.</p>"
				+ "<p>By clicking the button below, you confirm that the details provided are correct and you authorize us to proceed with your application.</p>"
				+ "<div style='text-align: center; margin: 30px 0;'>" + "<!--[if mso]>"
				+ "<v:roundrect xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:w=\"urn:schemas-microsoft-com:office:word\" href=\""
				+ confirmationUrl
				+ "\" style=\"height:50px;v-text-anchor:middle;width:200px;\" arcsize=\"10%\" strokecolor=\"#2e7d32\" fillcolor=\"#2e7d32\">"
				+ "<w:anchorlock/>"
				+ "<center style=\"color:#ffffff;font-family:sans-serif;font-size:16px;font-weight:bold;\">Confirm Consent</center>"
				+ "</v:roundrect>" + "<![endif]-->" + "<a href=\"" + confirmationUrl
				+ "\" style=\"background-color:#2e7d32; border-radius:5px; color:#ffffff; display:inline-block; font-family:sans-serif; font-size:16px; font-weight:bold; line-height:50px; text-align:center; text-decoration:none; width:200px; -webkit-text-size-adjust:none; mso-hide:all;\">Confirm Consent</a>"
				+ "<p style='font-style: italic; color: #e53935; font-size: 0.85em; margin-top: 15px;'>"
				+ "Note: This is a test template. More enhancements will be pushed soon." + "</p>" + "</div>"
				+ "<p style='font-size: 0.9em; color: #666;'>If the button above does not work, please copy and paste the following link into your browser:</p>"
				+ "<p style='font-size: 0.8em; word-break: break-all;'><a href=\"" + confirmationUrl + "\">"
				+ confirmationUrl + "</a></p>" + "<br>" + "<p>Best Regards,<br><strong>Tarifvergleich Team</strong></p>"
				+ "</div>";
	}

	public String createBookingExpiryEmailBody(String salutation, String lastName, String firstName,
			String providerName, String expiryDate) {
		return "<div style='font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; border: 1px solid #eee; padding: 20px;'>"
				+ "<h2 style='color: #d32f2f;'>Important: Your booking with " + providerName + " is expiring soon!</h2>"
				+ "<p>Dear " + salutation + " " + lastName + ",</p>" + "<p>Hello " + firstName + " " + lastName
				+ ",</p>" + "<p>We are writing to inform you that your current energy booking/contract with <strong>"
				+ providerName + "</strong> is scheduled to expire on <strong>" + expiryDate + "</strong>.</p>" +

				"<div style='margin-top: 25px; padding: 15px; background-color: #e3f2fd; border-left: 4px solid #2196f3;'>"
				+ "<p style='margin: 0; font-weight: bold;'>What does this mean for you?</p>"
				+ "<p style='margin: 10px 0 0 0;'>To ensure a seamless transition and avoid moving to a more expensive basic supply tariff (Grundversorgung), we recommend reviewing your renewal options immediately.</p>"
				+ "</div>" +

				"<p style='margin-top: 20px;'>If you have already initiated a new contract through <strong>Tarifvergleich</strong>, you can ignore this message. Otherwise, please log in to your account to view the latest tariffs available for your region.</p>"
				+

				"<br>" + "<p>Best Regards,<br><strong>Tarifvergleich Customer Support Team</strong></p>"
				+ "<hr style='border: 0; border-top: 1px solid #eee; margin: 20px 0;'>"
				+ "<p style='font-size: 12px; color: #777;'>This is an automated notification based on your contract end date. Please do not reply directly to this email.</p>"
				+ "</div>";
	}

	public String createAdminNewCounsellingRequestEmailBody(String adminName, String date, String timeSlot,
			String mobile, String description) {
		return "<div style='font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; border: 1px solid #2196F3; padding: 20px; border-radius: 8px;'>"
				+ "<h2 style='color: #0D47A1; margin-top: 0;'>📅 New Counselling Request</h2>" + "<p>Hello <strong>"
				+ adminName + "</strong>,</p>"
				+ "<p>A new appointment request has been submitted. Please find the contact details below:</p>" +

				"<div style='background-color: #E3F2FD; padding: 15px; border-left: 4px solid #2196F3; margin: 20px 0;'>"
				+ "<table style='width: 100%; border-collapse: collapse;'>"
				+ "<tr><td style='padding: 5px 0;'><strong>Contact Mobile:</strong></td><td style='padding: 5px 0;'>"
				+ mobile + "</td></tr>"
				+ "<tr><td style='padding: 5px 0;'><strong>Scheduled Date:</strong></td><td style='padding: 5px 0;'>"
				+ date + "</td></tr>"
				+ "<tr><td style='padding: 5px 0;'><strong>Time Slot:</strong></td><td style='padding: 5px 0;'>"
				+ timeSlot + "</td></tr>" + "</table>" + "<br><strong>Message / Notes:</strong><br>"
				+ "<em style='color: #555;'>"
				+ (description != null && !description.isEmpty() ? description : "No additional notes provided.")
				+ "</em>" + "</div>" +

				"<p>Please contact the requester at the number provided above to confirm the session.</p>"
				+ "<hr style='border: 0; border-top: 1px solid #eee; margin: 20px 0;'>"
				+ "<p style='font-size: 12px; color: #888;'>This is an automated notification from the Tarifvergleich Electricity Admin Portal.</p>"
				+ "</div>";
	}

	public String getPowerOfAttorneyEmailTemplate(String customerName) {
		return "Subject: Power of Attorney for your energy contracts\n\n" + "Dear " + customerName + ",\n\n"
				+ "We require a power of attorney for your energy contracts so that you can use some of our features and we can offer our comprehensive service. "
				+ "This allows us to, among other things, submit meter readings, request invoices, report changes, use our cancellation service, "
				+ "change your monthly payment, report meter replacements, send messages to energy suppliers, request invoices, and much more.\n\n"
				+ "Kind regards,\n\n" + "Your Support Team";
	}
	
	public String createSignatureRequestEmailBody(String salutation, String lastName, String token) {
	    String signatureUrl = "http://192.168.0.155:8080/public/customer/contract-signature?token=" + token;

	    return "<div style='font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: auto; border: 1px solid #ddd; border-radius: 8px; padding: 25px;'>"
	            + "<div style='text-align: center; border-bottom: 2px solid #2e7d32; padding-bottom: 10px; margin-bottom: 20px;'>"
	            + "<h2 style='color: #2e7d32; margin: 0;'>Contract Signature Required</h2>"
	            + "</div>"
	            + "<p>Dear " + salutation + " " + lastName + ",</p>"
	            + "<p>We are excited to help you switch to a more cost-effective energy plan! Your contract with <strong>Tarifvergleich Electricity</strong> is almost ready.</p>"
	            + "<p style='background-color: #f9f9f9; padding: 15px; border-left: 4px solid #2e7d32;'>"
	            + "To finalize your switch, please review the contract details and <strong>provide your digital signature</strong> by clicking the secure link below."
	            + "</p>"
	            + "<div style='text-align: center; margin: 35px 0;'>"
	            + "<!--[if mso]>"
	            + "<v:roundrect xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:w=\"urn:schemas-microsoft-com:office:word\" href=\"" + signatureUrl + "\" style=\"height:55px;v-text-anchor:middle;width:220px;\" arcsize=\"10%\" strokecolor=\"#1b5e20\" fillcolor=\"#2e7d32\">"
	            + "<w:anchorlock/>"
	            + "<center style=\"color:#ffffff;font-family:sans-serif;font-size:16px;font-weight:bold;\">Sign Your Contract</center>"
	            + "</v:roundrect>"
	            + "<![endif]-->"
	            + "<a href=\"" + signatureUrl + "\" style=\"background-color:#2e7d32; border-radius:5px; color:#ffffff; display:inline-block; font-family:sans-serif; font-size:16px; font-weight:bold; line-height:55px; text-align:center; text-decoration:none; width:220px; -webkit-text-size-adjust:none; mso-hide:all; box-shadow: 0 4px 6px rgba(0,0,0,0.1);\">Sign Your Contract</a>"
	            + "</div>"
	            + "<p style='font-size: 0.9em; color: #666;'><strong>Why is this needed?</strong> We require your signature to authorize the communication with your current energy provider and confirm the new bank mandate.</p>"
	            + "<hr style='border: 0; border-top: 1px solid #eee; margin: 20px 0;'>"
	            + "<p style='font-size: 0.85em; color: #888;'>If you cannot click the button, please copy this link: <br>"
	            + "<a href=\"" + signatureUrl + "\" style='color: #2e7d32; word-break: break-all;'>" + signatureUrl + "</a></p>"
	            + "<br>"
	            + "<p style='margin-bottom: 0;'>Best Regards,</p>"
	            + "<p style='margin-top: 5px;'><strong>The Tarifvergleich Support Team</strong></p>"
	            + "</div>";
	}
}
