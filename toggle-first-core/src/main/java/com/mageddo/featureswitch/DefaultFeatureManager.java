package com.mageddo.featureswitch;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mageddo.common.jackson.JsonUtils;
import com.mageddo.featureswitch.activationstrategy.ActivationStrategy;
import com.mageddo.featureswitch.repository.FeatureRepository;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultFeatureManager implements FeatureManager {

	private FeatureRepository featureRepository;
	private FeatureMetadataProvider featureMetadataProvider;
	private Set<ActivationStrategy> activationStrategies;

	public DefaultFeatureManager() {
		this.activationStrategies = Collections.emptySet();
	}

	@Override
	public Set<ActivationStrategy> activationStrategies() {
		return activationStrategies;
	}

	@Override
	public FeatureRepository repository() {
		return featureRepository;
	}

	@Override
	public FeatureMetadataProvider metadataProvider() {
		return featureMetadataProvider;
	}

	@Override
	public void activate(Feature feature) {
		final FeatureMetadata metadata = metadataOrDefault(feature, null)
		.set(FeatureKeys.STATUS, String.valueOf(Status.ACTIVE.getCode()))
		;
		repository().updateMetadata(metadata, null);
	}

	@Override
	public void activate(Feature feature, String value) {
		final FeatureMetadata metadata = metadataOrDefault(feature, null)
		.set(FeatureKeys.STATUS, String.valueOf(Status.ACTIVE.getCode()))
		.set(FeatureKeys.VALUE, value)
		;
		repository().updateMetadata(metadata, null);
	}

	@Override
	public void userActivate(Feature feature, String user) {
		{
			final FeatureMetadata metadata = metadataOrDefault(feature, user)
			.set(FeatureKeys.STATUS, String.valueOf(Status.RESTRICTED.getCode()))
			;

			repository().updateMetadata(metadata, null);
		}
		{
			FeatureMetadata metadata = repository().getMetadata(feature, user);
			if (metadata == null) {
				metadata = new DefaultFeatureMetadata(feature);
			}
			metadata
			.set(FeatureKeys.STATUS, String.valueOf(Status.ACTIVE.getCode()))
			;

			repository().updateMetadata(metadata, user);
		}
	}

	@Override
	public void userActivate(Feature feature, String user, String value) {
		{
			final FeatureMetadata metadata = metadataOrDefault(feature, null)
			.set(FeatureKeys.STATUS, String.valueOf(Status.RESTRICTED.getCode()))
			;
			repository().updateMetadata(metadata, null);
		}
		{
			FeatureMetadata metadata = repository().getMetadata(feature, user);
			if (metadata == null) {
				metadata = new DefaultFeatureMetadata(feature);
			}
			metadata
			.set(FeatureKeys.STATUS, String.valueOf(Status.ACTIVE.getCode()))
			.set(FeatureKeys.VALUE, value);

			repository().updateMetadata(metadata, user);
		}
	}

	@Override
	public void deactivate(Feature feature) {
		userDeactivate(feature, null);
	}

	@Override
	public void userDeactivate(Feature feature, String user) {
		FeatureMetadata metadata = repository().getMetadata(feature, user);
		if (metadata == null) {
			metadata = new DefaultFeatureMetadata(feature);
		}
		metadata
		.set(FeatureKeys.STATUS, String.valueOf(Status.INACTIVE.getCode()))
		;
		repository().updateMetadata(metadata, user);
	}

	@Override
	public void updateMetadata(Feature feature, Map<String, String> parameters) {
		updateMetadata(feature, null, parameters);
	}

	@Override
	public void updateMetadata(Feature feature, String user, Map<String, String> parameters) {
		final FeatureMetadata metadata = metadata(feature, user);
		for (final String k : parameters.keySet()) {
			metadata.set(k, parameters.get(k));
		}
		repository().updateMetadata(metadata, user);
	}

	@Override
	public FeatureMetadata metadata(Feature feature) {
		final FeatureMetadata metadata = repository().getMetadata(feature, null);
		if(metadata != null){
			return metadata;
		}
		final FeatureMetadataProvider provider = metadataProvider();
		if(provider != null){
			return provider.getMetadata(feature);
		}
		return new DefaultFeatureMetadata(feature);
	}

	@Override
	public FeatureMetadata metadata(Feature feature, String user) {
		if(user == null){
			return metadata(feature);
		}
		final FeatureMetadata metadata = metadata(feature);
		switch (metadata.status()){
			case ACTIVE:
			case INACTIVE:
				return metadata;
			case RESTRICTED:
				return Optional
					.ofNullable(repository().getMetadata(feature, user))
					.orElse(
						new DefaultFeatureMetadata(feature)
							.set(FeatureKeys.STATUS, String.valueOf(Status.INACTIVE.getCode()))
					);
		}
		return metadata;
	}

	FeatureMetadata metadataOrDefault(Feature feature, String user) {
		return repository().getMetadataOrDefault(feature, user, new DefaultFeatureMetadata(feature));
	}

	@Override
	public boolean isActive(Feature feature) {
		return isActive(feature, null);
	}

	@Override
	public boolean isActive(Feature feature, String user) {

		final FeatureMetadata metadata = metadata(feature, user);
		final Collection<ActivationStrategy> strategies = getFeatureActivationStrategies(metadata(feature));

		if(strategies.isEmpty() || metadata.isActive()){
			return metadata.isActive();
		}

		for (final ActivationStrategy activationStrategy : strategies) {
			if(!activationStrategy.isActive(metadata)){
				return false;
			}
		}
		return true;
	}

	private Collection<ActivationStrategy> getFeatureActivationStrategies(FeatureMetadata metadata) {
		final Set<String> strategiesIds = JsonUtils.readValue(
			metadata.get(FeatureKeys.ACTIVATION_STRATEGIES, "[]"),
			new TypeReference<Set<String>>() {}
		);
		return activationStrategies()
			.stream()
			.filter(it -> strategiesIds.contains(String.valueOf(it.id())))
			.collect(Collectors.toCollection(LinkedHashSet::new))
		;
	}

	@Override
	public String value(Feature feature) {
		return value(feature , null);
	}

	@Override
	public String value(Feature feature, String user) {
		final FeatureMetadata metadata = metadata(feature, user);
		return metadata == null ? null : metadata.get(FeatureKeys.VALUE);
	}

	public DefaultFeatureManager featureRepository(FeatureRepository featureRepository) {
		this.featureRepository = featureRepository;
		return this;
	}

	public DefaultFeatureManager featureMetadataProvider(FeatureMetadataProvider featureMetadataProvider) {
		this.featureMetadataProvider = featureMetadataProvider;
		return this;
	}

	public DefaultFeatureManager activationStrategies(Set<ActivationStrategy> activationStrategies) {
		this.activationStrategies = activationStrategies;
		return this;
	}
}
