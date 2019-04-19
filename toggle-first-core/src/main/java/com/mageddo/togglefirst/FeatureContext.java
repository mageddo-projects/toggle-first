package com.mageddo.togglefirst;

import com.mageddo.togglefirst.repository.InMemoryFeatureRepository;
import com.mageddo.togglefirst.spring.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Responsible to find a {@link FeatureManager} instance at the classpath
 */
public final class FeatureContext {

	private FeatureContext() {
	}

	public static FeatureManager getFeatureManager(){

		final FeatureManager featureManager = getSpringFeatureManager();
		if (featureManager != null){
			return featureManager;
		}

		final FeatureManager serviceLoaderFeatureManager = getServiceLoaderFeatureManager();
		if(serviceLoaderFeatureManager != null){
			return serviceLoaderFeatureManager;
		}

		return new DefaultFeatureManager()
			.featureRepository(new InMemoryFeatureRepository())
			.featureMetadataProvider(new EnumFeatureMetadataProvider())
		;
	}

	static boolean existsOnClasspath(String name){
		try {
			Class.forName(name);
			return true;
		} catch (ClassNotFoundException ignored) {
			return false;
		}
	}

	/**
	 * Create a serviceloader file at the path <b>/META-INF/services/com.mageddo.togglefirst.FeatureManager</b> with a content like
	 * <code>
	 * com.mageddo.togglefirst.DefaultFeatureManager
	 * </code>
	 */
	private static FeatureManager getServiceLoaderFeatureManager() {
		final Iterator<FeatureManager> it = ServiceLoader.load(FeatureManager.class).iterator();
		if(it.hasNext()){
			return it.next();
		}
		return null;
	}

	/**
	 * To integrate with spring just create a bean of the type {@link FeatureManager}
	 */
	private static FeatureManager getSpringFeatureManager() {
		if(existsOnClasspath("org.springframework.context.ApplicationContext")){
			final ApplicationContext ctx = ApplicationContextProvider.getApplicationContext();
			if(ctx != null){
				return ctx.getBean(FeatureManager.class);
			}
		}
		return null;
	}

}
