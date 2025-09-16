
package com.transcodeservice.common.exception.exception;

import com.transcodeservice.common.exception.logging.CustomException;

import lombok.Getter;

@Getter
public class HttpClientUtilException extends CustomException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -834623710938881199L;
	private final long errorCode;
	private final String[] params;

	public HttpClientUtilException(long errorCode, String errorMsg, String... params) {
		super(errorMsg);
		this.errorCode = errorCode;
		this.params = params;
	}

	public HttpClientUtilException(long errorCode, Exception exception, String... params) {
		super(exception);
		this.errorCode = errorCode;
		this.params = params;
	}
}
