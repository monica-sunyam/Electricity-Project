package com.tarifvergleich.electricity.exception;

import java.util.Map;

public class EnergyApiUnavailableException extends RuntimeException {
	
	private final Map<String, Object> errorDetails;
	
	
	public EnergyApiUnavailableException(String message, Map<String, Object> errorDetails) {
        super(message);
        this.errorDetails = errorDetails;
    }
	
	public EnergyApiUnavailableException(String message, Map<String, Object> errorDetails, Throwable cause) {
        super(message, cause);
        this.errorDetails = errorDetails;
    }

	public Map<String, Object> getErrorDetails() {
        return errorDetails;
    }
}
