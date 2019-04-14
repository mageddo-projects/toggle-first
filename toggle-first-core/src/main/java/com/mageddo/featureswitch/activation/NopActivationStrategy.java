package com.mageddo.featureswitch.activation;

import com.mageddo.featureswitch.FeatureMetadata;

import java.util.UUID;

/**
 * Just delegate feature check to feature metadata
 */
public class NopActivationStrategy implements ActivationStrategy {

	public static final UUID ID = UUID.fromString("47293db9-dc9e-4f18-a899-68a20dd699d9");

	@Override
	public UUID id() {
		return ID;
	}

	@Override
	public String description() {
		return "Does nothing, delegates to feature metadata";
	}

	@Override
	public boolean isActive(FeatureMetadata featureMetadata, String user) {
		return featureMetadata.isActive();
	}
}
