package com.transcodeservice.auth.exception;

import com.transcodeservice.common.exception.logging.CustomException;

import lombok.Getter;

@Getter
public class LoginException extends CustomException {

	private static final long serialVersionUID = -8649280753457216115L;
	long errorCode;
	private String[] params;

	public LoginException(long errorCode, String message, String... params) {
		super(message);
		this.errorCode = errorCode;
		this.params = params;
	}

	public LoginException(long errorCode, Exception exception, String... params) {
		super(exception);
		this.errorCode = errorCode;
		this.params = params;
	}
}