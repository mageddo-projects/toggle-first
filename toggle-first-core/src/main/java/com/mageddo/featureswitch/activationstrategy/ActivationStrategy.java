package com.mageddo.featureswitch.activationstrategy;

import com.mageddo.featureswitch.FeatureKeys;
import com.mageddo.featureswitch.FeatureManager;
import com.mageddo.featureswitch.FeatureMetadata;
import com.mageddo.featureswitch.Status;

import java.util.UUID;

public interface ActivationStrategy {

	/**
	 * Unique id for the strategy
	 */
	UUID id();

	/**
	 * Name used to display on gui
	 */
	default String name(){
		return getClass().getSimpleName();
	}

	/**
	 * Description used to display on gui
	 */
	String description();

	boolean isActive(FeatureMetadata featureMetadata);

	/**
	 * Called when {@link #isActive(FeatureMetadata)} returns true
	 * <br>
	 * Update feature metadata
	 */
	default void postHandleActive(FeatureManager featureManager, FeatureMetadata metadata){
		metadata.set(FeatureKeys.STATUS, String.valueOf(Status.ACTIVE.getCode()));
		featureManager.updateMetadata(metadata.feature(), metadata.parameters());
	}
}
