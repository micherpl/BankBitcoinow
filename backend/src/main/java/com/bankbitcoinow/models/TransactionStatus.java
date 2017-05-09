package com.bankbitcoinow.models;

public enum TransactionStatus {
	PREPARED(1),
	SIGNED(2),
	UNCONFIRMED(3),
	CONFIRMED(4);

	private final int dbValue;

	TransactionStatus(int dbValue) {
		this.dbValue = dbValue;
	}

	public int getDbValue() {
		return dbValue;
	}
}
