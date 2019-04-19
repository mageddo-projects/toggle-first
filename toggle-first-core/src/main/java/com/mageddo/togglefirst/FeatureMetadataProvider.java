package com.mageddo.togglefirst;

import com.mageddo.togglefirst.repository.FeatureRepository;

/**
 * Used to retrieve feature metadata when it is not found at the {@link FeatureRepository}
 */
public interface FeatureMetadataProvider {
	FeatureMetadata getMetadata(Feature feature);
}
