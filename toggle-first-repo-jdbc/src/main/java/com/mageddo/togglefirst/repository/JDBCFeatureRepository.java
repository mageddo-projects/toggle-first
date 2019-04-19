package com.mageddo.togglefirst.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mageddo.togglefirst.*;
import com.mageddo.commons.StringUtils;
import com.mageddo.togglefirst.flyway.MigrationUtils;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;

public class JDBCFeatureRepository implements FeatureRepository {

	private final DataSource dataSource;
	private final ObjectMapper mapper;

	public JDBCFeatureRepository(final DataSource dataSource) {
		this(dataSource, new ObjectMapper());
	}

	public JDBCFeatureRepository(final DataSource dataSource, ObjectMapper mapper) {
		this.dataSource = dataSource;
		this.mapper = mapper;
		MigrationUtils.migrate(dataSource);
	}

	public JDBCFeatureRepository(final DataSource dataSource, Flyway flyway, ObjectMapper mapper) {
		this.dataSource = dataSource;
		this.mapper = mapper;
		MigrationUtils.migrate(flyway);
	}

	@Override
	public FeatureMetadata getMetadata(Feature feature, String user) {
		try (
			final Connection con = dataSource.getConnection()
		) {
			if(StringUtils.isBlank(user)){
				return featureMetadata(con, feature);
			}
			return featureMetadata(con, feature, user);
		} catch (SQLException e) {
			throw new SQLToggleFirstException("Failed to recover feature metadata", e);
		}
	}

	@Override
	public int updateMetadata(FeatureMetadata featureMetadata, String user) {
		try (
			final Connection con = dataSource.getConnection()
		) {
			if(StringUtils.isBlank(user)){
				if(featureMetadata(con, featureMetadata.feature()) == null){
					return insertFeature(con, featureMetadata);
				}
				return updateFeature(con, featureMetadata);
			}
			if(featureMetadata(con, featureMetadata.feature()) == null){
				insertFeature(
					con,
					new DefaultFeatureMetadata(featureMetadata.feature())
						.set(FeatureKeys.STATUS, String.valueOf(Status.RESTRICTED.getCode()))
				);
			}
			if(featureMetadata(con, featureMetadata.feature(), user) == null){
				return insertFeature(con, featureMetadata, user);
			}
			return updateFeature(con, featureMetadata, user);
		} catch (SQLException e) {
			throw new SQLToggleFirstException("Failed to update feature metadata", e);
		}
	}

	int insertFeature(Connection con, FeatureMetadata featureMetadata) throws SQLException {
		final StringBuilder sql = new StringBuilder()
			.append("INSERT INTO PARAMETER ( \n")
			.append("	NAM_PARAMETER, VAL_PARAMETER, DAT_CREATION, DAT_UPDATE \n")
			.append(") VALUES ( \n")
			.append("	?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP \n")
			.append(") \n")
			;
		try (final PreparedStatement stm = con.prepareStatement(sql.toString())) {
			stm.setString(1, featureMetadata.feature().name());
			stm.setString(2, mapper.writeValueAsString(featureMetadata.parameters()));
			return stm.executeUpdate();
		} catch (IOException e){
			throw new UncheckedIOException(e);
		}
	}

	int insertFeature(Connection con, FeatureMetadata featureMetadata, String user) throws SQLException {
		final StringBuilder sql = new StringBuilder()
			.append("INSERT INTO USER_PARAMETER ( \n")
			.append("	IDT_PARAMETER, COD_USER, VAL_PARAMETER, \n")
			.append("	DAT_CREATION, DAT_UPDATE \n")
			.append(") VALUES ( \n")
			.append("	(SELECT IDT_PARAMETER FROM PARAMETER WHERE NAM_PARAMETER = ?), ?, ?, \n")
			.append("	CURRENT_TIMESTAMP, CURRENT_TIMESTAMP \n")
			.append(") \n")
		;
		try (final PreparedStatement stm = con.prepareStatement(sql.toString())) {
			stm.setString(1, featureMetadata.feature().name());
			stm.setString(2, user);
			stm.setString(3, mapper.writeValueAsString(featureMetadata.parameters()));
			return stm.executeUpdate();
		} catch (IOException e){
			throw new UncheckedIOException(e);
		}
	}


	int updateFeature(Connection con, FeatureMetadata featureMetadata) throws SQLException {
		final StringBuilder sql = new StringBuilder()
			.append("UPDATE PARAMETER SET \n")
			.append("	VAL_PARAMETER = ? \n")
			.append("WHERE NAM_PARAMETER = ? \n");
		try (final PreparedStatement stm = con.prepareStatement(sql.toString())) {
			stm.setString(1, mapper.writeValueAsString(featureMetadata.parameters()));
			stm.setString(2, featureMetadata.feature().name());
			return stm.executeUpdate();
		} catch (IOException e){
			throw new UncheckedIOException(e);
		}
	}

	int updateFeature(Connection con, FeatureMetadata featureMetadata, String user) throws SQLException {
		final StringBuilder sql = new StringBuilder()
			.append("UPDATE USER_PARAMETER SET \n")
			.append("	VAL_PARAMETER = ? \n")
			.append("WHERE IDT_PARAMETER = (SELECT IDT_PARAMETER FROM PARAMETER WHERE NAM_PARAMETER = ?) \n")
			.append("AND COD_USER = ? \n");
		try (final PreparedStatement stm = con.prepareStatement(sql.toString())) {
			stm.setString(1, mapper.writeValueAsString(featureMetadata.parameters()));
			stm.setString(2, featureMetadata.feature().name());
			stm.setString(3, user);
			return stm.executeUpdate();
		} catch (IOException e){
			throw new UncheckedIOException(e);
		}
	}

	FeatureMetadata featureMetadata(Connection con, Feature feature, String user) throws SQLException {
		final StringBuilder sql = new StringBuilder()
			.append("SELECT \n")
			.append("	UP.VAL_PARAMETER \n")
			.append("FROM PARAMETER P \n")
			.append("INNER JOIN USER_PARAMETER UP ON UP.IDT_PARAMETER = P.IDT_PARAMETER \n")
			.append("WHERE P.NAM_PARAMETER = ? \n")
			.append("AND UP.COD_USER = ? \n");
		try (final PreparedStatement stm = con.prepareStatement(sql.toString())) {
			stm.setString(1, feature.name());
			stm.setString(2, user);
			return mapToMetadata(feature, stm.executeQuery());
		}
	}

	FeatureMetadata featureMetadata(Connection con, Feature feature) throws SQLException {
		try (
			final PreparedStatement stm = con.prepareStatement("SELECT VAL_PARAMETER FROM PARAMETER WHERE NAM_PARAMETER = ?")
		) {
			stm.setString(1, feature.name());
			return mapToMetadata(feature, stm.executeQuery());
		}
	}

	FeatureMetadata mapToMetadata(Feature feature, ResultSet rs) throws SQLException {
		try {
			if(!rs.next()){
				return null;
			}
			return new DefaultFeatureMetadata(
				feature,
				mapper.readValue(rs.getString("VAL_PARAMETER"), LinkedHashMap.class)
			);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
