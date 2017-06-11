package com.bankbitcoinow.web;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts {@link BindingResult} to nicer response.
 */
public class ValidationErrorResponse {

	private final Errors errors = new Errors();

	public ValidationErrorResponse(BindingResult bindingResult) {
		for (ObjectError objectError : bindingResult.getGlobalErrors()) {
			errors.global.add(objectError.getDefaultMessage());
		}

		for (FieldError fieldError : bindingResult.getFieldErrors()) {
			String field = fieldError.getField();

			if (!errors.fields.containsKey(field)) {
				errors.fields.put(field, fieldError.getDefaultMessage());
			}
		}
	}

	public Errors getErrors() {
		return errors;
	}

	private static class Errors {

		private final List<String> global = new ArrayList<>();
		private final Map<String, String> fields = new HashMap<>();

		public List<String> getGlobal() {
			return global;
		}

		public Map<String, String> getFields() {
			return fields;
		}
	}
}
