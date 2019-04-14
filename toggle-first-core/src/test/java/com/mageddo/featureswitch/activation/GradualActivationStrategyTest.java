package com.mageddo.featureswitch.activation;

import com.mageddo.featureswitch.BasicFeature;
import com.mageddo.featureswitch.DefaultFeatureMetadata;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GradualActivationStrategyTest {

	@Test
	public void shouldBeInactiveForNullUser() {

		// arrange
		final var metadata = new DefaultFeatureMetadata(new BasicFeature("CAT_BOX"));
		final var activationStrategy = new GradualActivationStrategy();

		// act
		final boolean active = activationStrategy.isActive(metadata);

		// assert
		assertFalse(active);

	}

	@Test
	public void shouldBeActiveForUser56441642() {

		// arrange
		final var metadata = new DefaultFeatureMetadata(new BasicFeature("CAT_BOX"));
		final var activationStrategy = new GradualActivationStrategy();

		// act
		final boolean active = activationStrategy.isActive(metadata, "56441642");

		// assert
		assertTrue(active);

	}

	@Test
	public void shouldBeAlwaysActiveWhenPercentageIs100() {

		// arrange
		final var metadata = new DefaultFeatureMetadata(new BasicFeature("CAT_BOX"))
			.set(GradualActivationStrategy.PARAM_PERCENTAGE, "100")
		;
		final var activationStrategy = new GradualActivationStrategy();

		// act // assert
		assertTrue(activationStrategy.isActive(metadata, "544"));
		assertTrue(activationStrategy.isActive(metadata, "3321"));
		assertTrue(activationStrategy.isActive(metadata, "56441642"));

	}


}
