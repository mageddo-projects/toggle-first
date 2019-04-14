package com.mageddo.featureswitch.activationstrategy;

import com.mageddo.featureswitch.FeatureMetadata;

import java.util.UUID;

public interface ActivationStrategy {
	UUID id();
	boolean isActive(FeatureMetadata featureMetadata);
}
