<iframe 
	src="https://www.draw.io/?lightbox=1&highlight=0000ff&edit=_blank&layers=1&nav=1&title=toggle-first-architecture.drawio#Uhttps%3A%2F%2Fdrive.google.com%2Fuc%3Fid%3D1ri4A7a6Ze788m9iErIf1gYs3fWc7lPDz%26export%3Ddownload"
	width="100%" height="480" frameborder="0" allowfullscreen
></iframe>

### Using With Spring

Register feature manager for database persistence

```java
@Bean
public FeatureManager featureManager(DataSource dataSource){
	return new DefaultFeatureManager()
		.featureMetadataProvider(new EnumFeatureMetadataProvider())
		.featureRepository(new JDBCFeatureRepository(dataSource))
	;
}
```

Create your Feature enum

```java
public enum Parameter implements InteractiveFeature {

	@FeatureDefaults(status = Status.ACTIVE, value = "Congrats!")
	FREE_COINS

	;

	@Override
	public FeatureManager manager() {
		return FeatureContext.getFeatureManager();
	}
}
```

Using

```java
Parameter.FREE_COINS.isActive();
```

### Registering JMX

```java
FeatureSwitchJMX.register();
```
