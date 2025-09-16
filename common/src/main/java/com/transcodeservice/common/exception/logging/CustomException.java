package com.transcodeservice.common.exception.logging;

import lombok.Getter;

/**
 * Parent exception to be extended by all custom exceptions.
 * 
 * @author Sanjay
 *
 */

@Getter
public class CustomException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -968403411118747432L;
	private long errorCode;
	private String[] params;

	public CustomException(String errorMsg) {
		super(errorMsg);
	}

	public CustomException(Exception exception) {
		super(exception);
	}

	public CustomException(long errorCode, String errorMsg, String... params) {
		super(errorMsg);
		this.errorCode = errorCode;
		this.params = params;
	}

	public CustomException(long errorCode, Exception exception, String... params) {
		super(exception);
		this.errorCode = errorCode;
		this.params = params;
	}
}