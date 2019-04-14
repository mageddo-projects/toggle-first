package com.mageddo.featureswitch.activation;

import com.mageddo.featureswitch.FeatureKeys;
import com.mageddo.featureswitch.FeatureManager;
import com.mageddo.featureswitch.FeatureMetadata;
import com.mageddo.featureswitch.Status;

import java.util.Collection;
import java.util.Collections;
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

	default boolean isActive(FeatureMetadata featureMetadata){
		return isActive(featureMetadata, null);
	}

	boolean isActive(FeatureMetadata featureMetadata, String user);

	/**
	 * Called when {@link #isActive(FeatureMetadata, String)} returns true
	 * <br>
	 * Update feature metadata
	 */
	default void postHandleActive(FeatureManager featureManager, FeatureMetadata metadata){
		metadata.set(FeatureKeys.STATUS, String.valueOf(Status.ACTIVE.getCode()));
		featureManager.updateMetadata(metadata.feature(), metadata.parameters());
	}

	/**
	 * Parameters used by the strategy to make the necessary calculation
	 * @return
	 */
	default Collection<Parameter> parameters(){
		return Collections.emptyList();
	}
}
