package com.bankbitcoinow;

import org.mockito.invocation.Invocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Comparator;

import static org.mockito.Mockito.mockingDetails;

public class MockUtils {

	private static final Logger LOG = LoggerFactory.getLogger(MockUtils.class);

	private MockUtils() {
	}

	/**
	 * Logs all invocations from given mocks (spies, etc) in order they occurred.
	 * @param mocks
	 */
	public static void printInvocationsInOrder(Object... mocks) {
		Arrays.stream(mocks)
				.flatMap(mock -> mockingDetails(mock).getInvocations().stream())
				.sorted(Comparator.comparingInt(Invocation::getSequenceNumber))
				.forEach(invocation -> LOG.debug("Invocation: {}", invocation));
	}
}
