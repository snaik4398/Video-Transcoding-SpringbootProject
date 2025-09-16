package com.transcodeservice.auth.exception;

import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * All the exceptions raised in the Auth service will be centrally handled by
 * this class.
 * 
 * @author Sanjay
 *
 */

@ControllerAdvice
public class AuthExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(AuthException.class)
	public ResponseEntity<?> authException(AuthException ex, WebRequest request) {
		ExceptionResponse errorDetails = new ExceptionResponse(ex.getErrorCode(), new Date(), ex.getMessage(),
				request.getDescription(false));
		return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(LoginException.class)
	public ResponseEntity<?> invalidCredentialsException(LoginException ex, WebRequest request) {
		ExceptionResponse errorDetails = new ExceptionResponse(ex.getErrorCode(), new Date(), ex.getMessage(),
				request.getDescription(false));
		return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<?> userNotFoundException(UserNotFoundException ex, WebRequest request) {
		ExceptionResponse errorDetails = new ExceptionResponse(ex.getErrorCode(), new Date(), ex.getMessage(),
				request.getDescription(false));
		return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
	}

//	@Override
//	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
//			HttpHeaders headers, HttpStatus status, WebRequest request) {
//		try {
//			return new ResponseEntity<>(CommonUtils.GetGenericExceptionForValidation(21000,
//					Objects.nonNull(ex) && Objects.nonNull(ex.getFieldError()) ? ex.getFieldError().getDefaultMessage()
//							: ""),
//					HttpStatus.BAD_REQUEST);
//		} catch (Exception exception) {
//			return new ResponseEntity<>(CommonUtils.GetGenericExceptionForValidation(21000, ex.getMessage()),
//					HttpStatus.BAD_REQUEST);
//		}
//	}
//
//	@Override
//	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
//			HttpHeaders headers, HttpStatus status, WebRequest request) {
//		String errorMessage = "Malformed JSON request";
//		ExceptionResponse errorDetails = new ExceptionResponse(21001, new Date(), errorMessage, ex.getMessage());
//		return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
//	}
//
//	@Override
//	protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
//			HttpStatus status, WebRequest request) {
//		String errorMessage = "Malformed Parameter request";
//		ExceptionResponse errorDetails = new ExceptionResponse(21003, new Date(), errorMessage, ex.getMessage());
//		return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
//	}
}
