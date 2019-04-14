package com.mageddo.featureswitch.activation;

import com.mageddo.commons.Validate;
import com.mageddo.featureswitch.FeatureMetadata;

import java.util.*;

public class GradualActivationStrategy implements ActivationStrategy {

	public static final UUID ID = UUID.fromString("d781b1cb-2eab-46d5-9c12-db8b4dd53d98");
	public static final String PARAM_PERCENTAGE = "percentage";

	@Override
	public UUID id() {
		return ID;
	}

	@Override
	public String description() {
		return "Activate feature by percentage chance";
	}

	@Override
	public boolean isActive(FeatureMetadata featureMetadata, String user) {
		Validate.notNull(featureMetadata, "user is required");
		final int percentage = featureMetadata.asInteger(PARAM_PERCENTAGE, 10);
		if (percentage > 0) {
			final int calculatedPercentage = Math.abs(calculateHashCode(featureMetadata, user)) % 100;
			return calculatedPercentage < percentage;
		}
		return false;
	}

	protected int calculateHashCode(FeatureMetadata metadata, String user) {
		return new StringBuilder()
			.append(String.valueOf(user).toLowerCase(Locale.ENGLISH).trim())
			.append(":")
			.append(metadata.feature().name())
			.toString().hashCode();

	}

	@Override
	public Collection<Parameter> parameters() {
		return Collections.singletonList(
			ParameterBuilder
				.create(PARAM_PERCENTAGE)
				.label("Percentage")
				.matching("\\d{1,3}")
				.description(
					"Percentage of users for which the feature should be active (i.e. '25' for every fourth user).")
		);
	}

}
