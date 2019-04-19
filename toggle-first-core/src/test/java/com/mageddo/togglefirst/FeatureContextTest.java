package com.mageddo.togglefirst;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FeatureContextTest {

	@Test
	public void shouldReturnTheSameReferenceOnEveryCall() {
		assertEquals(FeatureContext.getFeatureManager(), FeatureContext.getFeatureManager());
	}
}
