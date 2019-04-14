package com.mageddo.featureswitch;

public interface ActivationStrategy {
	boolean isActive(FeatureMetadata featureMetadata);
	boolean isActive(FeatureMetadata featureMetadata, FeatureMetadata userFeatureMetadata);
}
