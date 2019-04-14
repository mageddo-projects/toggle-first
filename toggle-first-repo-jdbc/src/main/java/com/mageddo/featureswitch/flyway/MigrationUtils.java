package com.mageddo.featureswitch.flyway;

import com.mageddo.featureswitch.SQLToggleFirstException;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public final class MigrationUtils {

	private static final Logger LOG = LoggerFactory.getLogger(MigrationUtils.class);

	private MigrationUtils() {
	}

	public static void migrate(final Flyway flyway){
		flyway.repair();
		flyway.migrate();
	}

	public static void migrate(DataSource dataSource) {
		migrate(getFlyway(dataSource));
	}

	private static Flyway getFlyway(DataSource dataSource) {
		try (Connection connection = dataSource.getConnection()){
			return Flyway
				.configure()
				.locations(getLocation(getDBName(connection)))
				.dataSource(dataSource)
				.schemas(connection.getSchema())
				.load();
		} catch (SQLException e) {
			throw new SQLToggleFirstException("Cannot retrieve migration info", e);
		}
	}

	private static String getLocation(String dbName) {
		final Map<String, String> locations = new HashMap<>();
		locations.put("hsqldb", "classpath:/toggle-first/migrations/hsqldb");
		return locations.get(dbName);
	}

	private static String getDBName(Connection connection) throws SQLException {
		final String url = connection.getMetaData().getURL();
		final int firstIndexOf = url.indexOf(":") + 1;
		if(firstIndexOf <= 0){
			LOG.warn("status=could-not-get-database-name, url={}", url);
			return "unknown";
		}
		return url.substring(firstIndexOf, url.indexOf(":", firstIndexOf)).toLowerCase();
	}
}
