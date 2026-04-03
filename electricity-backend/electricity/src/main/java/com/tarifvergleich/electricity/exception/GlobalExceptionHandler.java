package com.tarifvergleich.electricity.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(EnergyApiUnavailableException.class)
	public ResponseEntity<?> handleEnergyApiError(EnergyApiUnavailableException ex) {

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", LocalDateTime.now());
		body.put("status", HttpStatusCode.valueOf((int) ex.getErrorDetails().get("code")));
		body.put("error", ex.getErrorDetails().get("error"));
		body.put("message", ex.getErrorDetails().get("message"));
		body.put("details", ex.getErrorDetails());

		return new ResponseEntity<>(body, HttpStatusCode.valueOf((int) ex.getErrorDetails().get("code")));
	}

	@ExceptionHandler(InternalServerException.class)
	public ResponseEntity<?> handleCustomException(InternalServerException ex) {
		Map<String, Object> body = new LinkedHashMap<String, Object>();

		body.put("res", false);
		body.put("errMessage", ex.toString());

		return new ResponseEntity<>(body, ex.getCode());
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> handleAllExceptions(Exception ex){
		Map<String, Object> body = new LinkedHashMap<String, Object>();
		
		body.put("res", false);
		body.put("errMessage", "Etwas ist schiefgelaufen");
		
		return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
		
	}
}
