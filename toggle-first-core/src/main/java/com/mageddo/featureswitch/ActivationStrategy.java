package com.mageddo.featureswitch;

import java.util.UUID;

public interface ActivationStrategy {
	UUID id();
	boolean isActive(FeatureMetadata featureMetadata);
}
