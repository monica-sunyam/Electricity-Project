package com.tarifvergleich.electricity.util;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import ua_parser.Client;
import ua_parser.Parser;

@Component
public class Helper {

	private String getClientIp(HttpServletRequest request) {
		String[] headers = { "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_X_FORWARDED_FOR",
				"HTTP_X_FORWARDED", "HTTP_X_CLUSTER_CLIENT_IP", "HTTP_CLIENT_IP", "HTTP_FORWARDED_FOR",
				"HTTP_FORWARDED", "HTTP_VIA", "REMOTE_ADDR" };

		for (String header : headers) {
			String ip = request.getHeader(header);
			if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
				// Handle multiple IPs in X-Forwarded-For
				if (ip.contains(",")) {
					return ip.split(",")[0].trim();
				}
				return ip;
			}
		}

		return request.getRemoteAddr();
	}

	public String getIp(HttpServletRequest request) {
		return getClientIp(request);
	}

	public String generateOtp() {
		SecureRandom random = new SecureRandom();

		int otp = 100_000 + random.nextInt(900_000);

		return String.valueOf(otp);

	}
	
	public boolean isPasswordSecure(String password, String email) {
	    if (password == null || password.length() < 8 || password.length() > 50) return false;

	    boolean hasUpper = false;
	    boolean hasLower = false;
	    boolean hasDigit = false;
	    boolean hasSpecial = false;
	    String specialChars = "!@$%^&*+#";


	    for (char c : password.toCharArray()) {
	        if (Character.isUpperCase(c)) hasUpper = true;
	        else if (Character.isLowerCase(c)) hasLower = true;
	        else if (Character.isDigit(c)) hasDigit = true;
	        else if (specialChars.indexOf(c) != -1) hasSpecial = true;
	    }
	    
	    String emailPrefix = email.split("@")[0].toLowerCase();

	    String lowerPassword = password.toLowerCase();

	    if (lowerPassword.contains(emailPrefix)) {
	        return false;
	    }
	    
	    return hasUpper && hasLower && hasDigit && hasSpecial;
	}
	
	public BigInteger toGermamUnixTimestamp(LocalDate localDate) {
		ZoneId zoneId = ZoneId.of("Europe/Berlin");
		ZonedDateTime zonedDateTime = localDate.atStartOfDay(zoneId);
		return BigInteger.valueOf(zonedDateTime.toEpochSecond());
	}
	
	public static final BigInteger getCurrentTimeBerlin() {
		ZonedDateTime nowInBerlin = ZonedDateTime.now(ZoneId.of("Europe/Berlin"));
		return BigInteger.valueOf(nowInBerlin.toEpochSecond());
	}
	
	public Map<String, Object> getDeviceInfo(String userAgentString){
		Parser uaParser = new Parser();
		Client client = uaParser.parse(userAgentString);
		
		return Map.of(
		        "os", client.os.family,        
		        "device", client.device.family, 
		        "browser", client.userAgent.family 
		    );
	}
	
}
