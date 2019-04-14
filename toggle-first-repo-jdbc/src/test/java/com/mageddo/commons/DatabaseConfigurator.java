package com.mageddo.commons;

import com.mageddo.featureswitch.SQLToggleFirstException;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class DatabaseConfigurator extends ExternalResource {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final DataSource dataSource;

	public DatabaseConfigurator(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	protected void before() {}

	@Override
	protected void after() {}

	public void update(String sql){
		try(Connection connection = dataSource.getConnection()){
			try(PreparedStatement ps = connection.prepareStatement(sql)){
				ps.executeUpdate();
			}
		} catch (SQLException e) {
			throw new SQLToggleFirstException(e.getMessage(), e);
		}
	}
	public void execute(String file){
		throw new UnsupportedOperationException();
	}

}
