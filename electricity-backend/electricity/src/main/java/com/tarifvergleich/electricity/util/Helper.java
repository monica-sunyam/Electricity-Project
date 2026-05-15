package com.tarifvergleich.electricity.util;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.tarifvergleich.electricity.exception.InternalServerException;

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
		if (password == null || password.length() < 8 || password.length() > 50)
			return false;

		boolean hasUpper = false;
		boolean hasLower = false;
		boolean hasDigit = false;
		boolean hasSpecial = false;
		String specialChars = "!@$%^&*+#";

		for (char c : password.toCharArray()) {
			if (Character.isUpperCase(c))
				hasUpper = true;
			else if (Character.isLowerCase(c))
				hasLower = true;
			else if (Character.isDigit(c))
				hasDigit = true;
			else if (specialChars.indexOf(c) != -1)
				hasSpecial = true;
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
	
	public LocalDate toGermalDateStamp(BigInteger dateAndTime) {
		ZoneId zoneId = ZoneId.of("Europe/Berlin");
		long timeStamp = dateAndTime.longValue();
		Instant instant = Instant.ofEpochSecond(timeStamp);
		return instant.atZone(zoneId).toLocalDate();
	}

	public static final BigInteger getCurrentTimeBerlin() {
		ZonedDateTime nowInBerlin = ZonedDateTime.now(ZoneId.of("Europe/Berlin"));
		return BigInteger.valueOf(nowInBerlin.toEpochSecond());
	}

	public static LocalDateTime getLocalDateTimeFromBerlinEpoch() {
		long seconds = getCurrentTimeBerlin().longValue();

		Instant instant = Instant.ofEpochSecond(seconds);

		return LocalDateTime.ofInstant(instant, ZoneId.of("Europe/Berlin"));
	}

	public static Map<String, Object> getLocalDateTimeFromBigInteger(BigInteger epochTime) {
		if (epochTime == null)
			return null;

		LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochSecond(epochTime.longValue()),
				ZoneId.of("Europe/Berlin"));

		Map<String, Object> timeMap = new HashMap<>();
		timeMap.put("year", ldt.getYear());
		timeMap.put("month", ldt.getMonthValue());
		timeMap.put("monthName", ldt.getMonth().name());
		timeMap.put("date", ldt.getDayOfMonth());
		timeMap.put("hour", ldt.getHour() % 12 + ldt.getHour() / 12);
		timeMap.put("minute", Integer.valueOf(ldt.getMinute()) < 10 ? "0" + ldt.getMinute() : ldt.getMinute());
		String amPm = ldt.format(DateTimeFormatter.ofPattern("a", java.util.Locale.ENGLISH));
		timeMap.put("amPm", amPm);
		timeMap.put("second", ldt.getSecond());
		timeMap.put("dayOfWeek", ldt.getDayOfWeek().name());

		return timeMap;
	}

	public BigInteger toGermanTimestampWithDynamicTime(LocalDate date, int hour, int minute) {
		if (date == null)
			return null;

		long epochSeconds = date.atTime(hour, minute, 0).atZone(ZoneId.of("Europe/Berlin")).toEpochSecond();

		return BigInteger.valueOf(epochSeconds);
	}

	public Map<String, Object> getDeviceInfo(String userAgentString) {
		Parser uaParser = new Parser();
		Client client = uaParser.parse(userAgentString);

		return Map.of("os", client.os.family, "device", client.device.family, "browser", client.userAgent.family);
	}

	public static final String getUniqueIdForCustomerId() {
		String currentMilli = String.valueOf(System.currentTimeMillis());
		return "9" + currentMilli.substring(currentMilli.length() - 10).substring(1);
	}

	public static final String getUniqueId() {
		String currentMilli = String.valueOf(System.currentTimeMillis());
		return String.valueOf(ThreadLocalRandom.current().nextInt(10, 99))
				+ currentMilli.substring(currentMilli.length() - 8);
	}

	public static final String getUniqueTicketNumber() {
		String currentMilli = String.valueOf(System.currentTimeMillis());
		String ticketNumber = String.valueOf(ThreadLocalRandom.current().nextInt(1000, 9999))
				+ currentMilli.substring(currentMilli.length() - 10);
		return ticketNumber.substring(0, 4) + "-" + ticketNumber.substring(4, 8) + "-" + ticketNumber.substring(8, 12)
				+ "-" + ticketNumber.substring(12);
	}

	public final BigInteger getSecondValueOfDuration(Integer year, Integer month, Integer day, Integer hour,
			Integer minute, Integer second) {

		if (year == null || year <= 0)
			year = 0;
		if (month == null || month <= 0)
			month = 0;
		if (day == null || day <= 0)
			day = 0;
		if (hour == null || hour <= 0)
			hour = 0;
		if (minute == null || minute <= 0)
			minute = 0;
		if (second == null || second <= 0)
			second = 0;

		BigInteger getSecondOfDay = BigInteger.valueOf(24 * 60 * 60);

		BigInteger totalDuration = BigInteger.valueOf(year).multiply(BigInteger.valueOf(365)).multiply(getSecondOfDay)
				.add(BigInteger.valueOf(month).multiply(BigInteger.valueOf(30)).multiply(getSecondOfDay))
				.add(BigInteger.valueOf(day).multiply(getSecondOfDay))
				.add(BigInteger.valueOf(hour).multiply(BigInteger.valueOf(3600)))
				.add(BigInteger.valueOf(minute).multiply(BigInteger.valueOf(60))).add(BigInteger.valueOf(second));

		return totalDuration;
	}

	public LocalDate flexibleDateParser(String dateStr) {

		List<String> patterns = Arrays.asList("yyyy-MM-dd", // 2026-05-08
				"dd.MM.yyyy", // 08.05.2026
				"MM/dd/yyyy", // 05/08/2026
				"dd-MM-yyyy", // 08-05-2026
				"yyyy/MM/dd" // 2026/05/08
		);

		for (String pattern : patterns) {
			try {
				return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
			} catch (DateTimeParseException e) {
				continue;
			}
		}

		throw new IllegalArgumentException("Unknown date format: " + dateStr);
	}

	public String convertToBase64(MultipartFile file) {
		try {
			byte[] contentByte = file.getBytes();
			return Base64.getEncoder().encodeToString(contentByte);
		} catch (IOException e) {
			e.printStackTrace();
			throw new InternalServerException("Error encoding the file", HttpStatus.OK);
		}
	}
	
	public String generateUUId() {
		return UUID.randomUUID().toString();
	}
}
