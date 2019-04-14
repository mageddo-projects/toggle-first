package com.mageddo.featureswitch;

import com.mageddo.featureswitch.activation.ActivationStrategy;

public final class FeatureKeys {
	/**
	 * Value stored at the feature used to general purpose
	 */
	public static final String VALUE = "value";

	/**
	 * The current status
	 * @see Status
	 */
	public static final String STATUS = "status";

	/**
	 * All activation strategies used determine if feature is active or not
	 *
	 * @see ActivationStrategy
	 */
	public static final String ACTIVATION_STRATEGIES = "activationStrategies";
}
