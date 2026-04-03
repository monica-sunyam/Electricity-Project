package com.tarifvergleich.electricity.util;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class Helper {

	private String getClientIp(HttpServletRequest request) {
		String[] headers = {
	            "X-Forwarded-For",
	            "Proxy-Client-IP",
	            "WL-Proxy-Client-IP",
	            "HTTP_X_FORWARDED_FOR",
	            "HTTP_X_FORWARDED",
	            "HTTP_X_CLUSTER_CLIENT_IP",
	            "HTTP_CLIENT_IP",
	            "HTTP_FORWARDED_FOR",
	            "HTTP_FORWARDED",
	            "HTTP_VIA",
	            "REMOTE_ADDR"
	        };

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
}
