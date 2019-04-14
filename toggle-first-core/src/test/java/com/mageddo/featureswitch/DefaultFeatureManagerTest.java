package com.mageddo.featureswitch;

import com.mageddo.featureswitch.activationstrategy.ActivationStrategy;
import com.mageddo.featureswitch.activationstrategy.GradualActivationStrategy;
import com.mageddo.featureswitch.activationstrategy.NopActivationStrategy;
import com.mageddo.featureswitch.repository.InMemoryFeatureRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static com.mageddo.common.jackson.JsonUtils.writeValueAsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DefaultFeatureManagerTest {

	@Test
	public void mustMergeFeatureMetadata(){

		// arrange
		final BasicFeature feature = new BasicFeature("MY_FEATURE");
		final DefaultFeatureManager featureManager = new DefaultFeatureManager()
		.featureRepository(new InMemoryFeatureRepository())
		;
		final String expectedValue = "ABCD";
		final Map<String, String> newProperties = new LinkedHashMap<>();
		newProperties.put("K1", "val1");
		newProperties.put("K2", "val2");
		newProperties.put("status", "2");

		// act
		featureManager.activate(feature, expectedValue);
		featureManager.updateMetadata(feature, newProperties);

		// assert
		final FeatureMetadata metadata = featureManager.metadata(feature);
		assertEquals(false, featureManager.isActive(feature));
		assertEquals(Status.RESTRICTED, metadata.status());
		assertEquals(expectedValue, metadata.get("value"));
		assertEquals("val1", metadata.get("K1"));
		assertEquals("val2", metadata.get("K2"));

		assertEquals(null, featureManager.metadata(feature, "user1").get("K2"));

	}

	@Test
	public void mustCheckEnabledActivationStrategies(){
		// arrange
		final var feature = new BasicFeature("MY_FEATURE");
		final var activeActivationStrategy = spy(new NopActivationStrategy());

		final var featureManager = new DefaultFeatureManager()
			.featureRepository(new InMemoryFeatureRepository())
			.activationStrategies(Set.of(activeActivationStrategy, new GradualActivationStrategy()))
		;

		final var metadata = featureManager.metadata(feature);
		metadata.set(FeatureKeys.ACTIVATION_STRATEGIES, writeValueAsString(Set.of(activeActivationStrategy.id())));
		featureManager.updateMetadata(metadata.feature(), metadata.parameters());

		// act
		final boolean active = featureManager.isActive(feature);

		// assert
		assertEquals(false, active);
		verify(activeActivationStrategy).isActive(any());

		final String databaseStatus = featureManager
			.repository()
			.getMetadata(feature, null)
			.get(FeatureKeys.STATUS);
		assertNull(databaseStatus);
	}

	@Test
	public void mustNotCheckActivationStrategiesWhenThereIsNoneEnabled(){
		// arrange
		final var feature = new BasicFeature("MY_FEATURE");
		final var featureManager = new DefaultFeatureManager()
			.featureRepository(new InMemoryFeatureRepository())
			.activationStrategies(Set.of(spy(new GradualActivationStrategy()), spy(new NopActivationStrategy())))
		;

		final var metadata = featureManager.metadata(feature);
		metadata.set(FeatureKeys.ACTIVATION_STRATEGIES, "[]");
		featureManager.updateMetadata(metadata.feature(), metadata.parameters());

		// act
		final boolean active = featureManager.isActive(feature);

		// assert
		assertEquals(false, active);
		for (ActivationStrategy activationStrategy : featureManager.activationStrategies()) {
			verify(activationStrategy, never()).isActive(any());
		}
	}


	@Test
	public void mustNotCheckActivationFeatureWhenFeatureIsAlreadyActive(){
		// arrange
		final var feature = new BasicFeature("MY_FEATURE");
		final var activeActivationStrategy = spy(new NopActivationStrategy());

		final var featureManager = new DefaultFeatureManager()
			.featureRepository(new InMemoryFeatureRepository())
			.activationStrategies(Set.of(activeActivationStrategy))
			;

		final var metadata = featureManager.metadata(feature);
		metadata.set(FeatureKeys.ACTIVATION_STRATEGIES, writeValueAsString(Set.of(activeActivationStrategy.id())));
		featureManager.updateMetadata(metadata.feature(), metadata.parameters());
		featureManager.activate(feature);

		// act
		final boolean active = featureManager.isActive(feature);

		// assert
		assertEquals(true, active);
		verify(activeActivationStrategy, never()).isActive(any());
	}

	@Test
	public void mustNotCheckActivationFeatureWhenFeatureIsAlreadyActiveForTheUser(){
		// arrange
		final var userId = "123";
		final var feature = new BasicFeature("MY_FEATURE");
		final var activeActivationStrategy = spy(new NopActivationStrategy());

		final var featureManager = new DefaultFeatureManager()
			.featureRepository(new InMemoryFeatureRepository())
			.activationStrategies(Set.of(activeActivationStrategy))
			;

		final var metadata = featureManager.metadata(feature);
		metadata.set(FeatureKeys.ACTIVATION_STRATEGIES, writeValueAsString(Set.of(activeActivationStrategy.id())));
		featureManager.updateMetadata(metadata.feature(), metadata.parameters());
		featureManager.userActivate(feature, userId);

		// act
		final boolean active = featureManager.isActive(feature, userId);
		final boolean active2 = featureManager.isActive(feature, "321");

		// assert
		assertEquals(true, active);
		assertEquals(false, active2);
		verify(activeActivationStrategy).isActive(any());
	}

	@Test
	public void mustCheckStrategyThenUpdateRepository(){
		// arrange
		final var feature = new BasicFeature("MY_FEATURE");
		final var activeActivationStrategy = spy(new NopActivationStrategy());

		final var featureManager = new DefaultFeatureManager()
			.featureRepository(new InMemoryFeatureRepository())
			.activationStrategies(Set.of(activeActivationStrategy, new GradualActivationStrategy()))
		;

		doReturn(true).when(activeActivationStrategy).isActive(any());

		final var metadata = featureManager.metadata(feature);
		metadata.set(FeatureKeys.ACTIVATION_STRATEGIES, writeValueAsString(Set.of(activeActivationStrategy.id())));
		featureManager.updateMetadata(metadata.feature(), metadata.parameters());

		// act
		final boolean active = featureManager.isActive(feature);

		// assert
		assertEquals(true, active);
		verify(activeActivationStrategy).isActive(any());

		final String databaseStatus = featureManager
			.repository()
			.getMetadata(feature, null)
			.get(FeatureKeys.STATUS);
		assertEquals(Status.ACTIVE.getCodeAsString(), databaseStatus);
	}


}
