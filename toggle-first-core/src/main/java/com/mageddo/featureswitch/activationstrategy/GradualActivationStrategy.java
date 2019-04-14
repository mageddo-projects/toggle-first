package com.mageddo.featureswitch.activationstrategy;

import com.mageddo.featureswitch.FeatureMetadata;

import java.util.UUID;

public class GradualActivationStrategy implements ActivationStrategy {

	public static final UUID ID = UUID.fromString("d781b1cb-2eab-46d5-9c12-db8b4dd53d98");

	@Override
	public UUID id() {
		return ID;
	}

	@Override
	public String description() {
		return "Activate feature by percentage chance";
	}

	@Override
	public boolean isActive(FeatureMetadata featureMetadata) {
		return true;
	}
}
