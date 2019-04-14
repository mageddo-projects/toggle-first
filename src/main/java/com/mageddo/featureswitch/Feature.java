package com.mageddo.featureswitch;

/**
 * Represents a feature, you can create a feature defining a unique name.
 * <br>
 * You can check if this feature is active, metadata, etc. using {@link FeatureManager}
 *
 * @see BasicFeature
 * @see InteractiveFeature
 * @see FeatureManager
 */
public interface Feature {
	String name();
}
