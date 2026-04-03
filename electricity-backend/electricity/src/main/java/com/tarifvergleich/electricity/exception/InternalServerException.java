package com.tarifvergleich.electricity.exception;

import org.springframework.http.HttpStatus;

public class InternalServerException extends RuntimeException {
	
	private final String message;
	private final HttpStatus code;

	public InternalServerException(String message, HttpStatus code) {
		super();
		this.message = message;
		this.code = code;
	}

	@Override
	public String toString() {
		return message;
	}

	public String getMessage() {
		return message;
	}
	
	public HttpStatus getCode() {
		return code;
	}

}
