package com.bankbitcoinow.models;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class TransactionStatusConverter implements AttributeConverter<TransactionStatus, Integer> {

	@Override
	public Integer convertToDatabaseColumn(TransactionStatus transactionStatus) {
		return transactionStatus.getDbValue();
	}

	@Override
	public TransactionStatus convertToEntityAttribute(Integer dbData) {
		if (dbData == null) {
			return null;
		}

		for (TransactionStatus transactionStatus : TransactionStatus.values()) {
			if (transactionStatus.getDbValue() == dbData) {
				return transactionStatus;
			}
		}

		throw new IllegalArgumentException("Could not convert transaction status from db value: " + dbData);
	}
}
