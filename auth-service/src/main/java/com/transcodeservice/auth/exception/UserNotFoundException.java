package com.transcodeservice.auth.exception;

import com.transcodeservice.common.exception.logging.CustomException;

import lombok.Getter;

@Getter
public class UserNotFoundException extends CustomException {
	private static final long serialVersionUID = -724233323823946682L;
	long errorCode;
	private String[] params;

	public UserNotFoundException(long errorCode, String message, String... params) {
		super(message);
		this.errorCode = errorCode;
		this.params = params;
	}

	public UserNotFoundException(long errorCode, Exception exception, String... params) {
		super(exception);
		this.errorCode = errorCode;
		this.params = params;
	}
}