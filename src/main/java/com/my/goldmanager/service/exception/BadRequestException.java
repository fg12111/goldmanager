package com.my.goldmanager.service.exception;

public class BadRequestException extends RuntimeException {

	private static final long serialVersionUID = -2864181346243820388L;

	public BadRequestException() {
		super();
	}

	public BadRequestException(String message, Throwable cause) {
		super(message, cause);

	}

	public BadRequestException(String message) {
		super(message);

	}

}
