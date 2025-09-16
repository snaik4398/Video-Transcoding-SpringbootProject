package com.transcodeservice.auth.exception;

import com.transcodeservice.common.exception.logging.CustomException;

import lombok.Getter;

@Getter
public class AuthException extends CustomException {

	private static final long serialVersionUID = 6452670847768907628L;
	long errorCode;
	private String[] params;

	public AuthException(long errorCode, String message, String... params) {
		super(message);
		this.errorCode = errorCode;
		this.params = params;
	}

	public AuthException(long errorCode, Exception exception, String... params) {
		super(exception);
		this.errorCode = errorCode;
		this.params = params;
	}
}