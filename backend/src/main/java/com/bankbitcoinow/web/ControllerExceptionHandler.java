package com.bankbitcoinow.web;

import com.bankbitcoinow.web.ValidationErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Handles exceptions thrown from all controllers.
 */
@ControllerAdvice
@ResponseBody
public class ControllerExceptionHandler {

	@ExceptionHandler(BindException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Object handleBindException(BindException e) {
		return new ValidationErrorResponse(e);
	}
}
