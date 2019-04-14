package com.mageddo.featureswitch.repository;

import com.mageddo.featureswitch.Feature;
import com.mageddo.featureswitch.FeatureMetadata;

/**
 * Represent the storage of the features, here you can save and find feature metadata
 */
public interface FeatureRepository {

	/**
	 *
	 * @param feature
	 * @param user
	 * @return feature metadata from storage
	 */
	FeatureMetadata getMetadata(Feature feature, String user);

	/**
	 * Store feature metadata
	 * @param featureMetadata
	 * @param user
	 * @return how many records were affected
	 */
	int updateMetadata(FeatureMetadata featureMetadata, String user);

	default FeatureMetadata getMetadataOrDefault(Feature feature, String user, FeatureMetadata metadata){
		final FeatureMetadata f = getMetadata(feature, user);
		if(f != null){
			return f;
		}
		return metadata;
	}
}
