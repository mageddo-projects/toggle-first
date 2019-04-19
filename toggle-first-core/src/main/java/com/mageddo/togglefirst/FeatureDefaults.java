package com.mageddo.togglefirst;

import java.lang.annotation.*;

/**
 * Store default values for the feature, you can annotate a EnumBasedFeature for example
 *
 * @see EnumFeatureMetadataProvider
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FeatureDefaults {
	Status status() default Status.INACTIVE;
	String value() default "";
}
