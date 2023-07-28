package org.hibernate.bugs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class OracleBatchProcessingError {

	public static Connection connection() throws Exception {
		Class.forName( "oracle.jdbc.driver.OracleDriver" );
		Connection connection = DriverManager.getConnection(
				"jdbc:oracle:thin:@localhost:1521:FREE", "SYSTEM", "quarkus" );
		connection.setAutoCommit( false );
		return connection;
	}

	public static void main(String[] args) throws Exception {
		Connection connection = connection();
		createTables( connection );
		doBatchInsert( connection );
	}

	public static void createTables(Connection connection) throws SQLException {
		// Drop first:
		try {
			connection.createStatement().executeUpdate( "drop table MyEntity cascade constraints" );
			connection.createStatement().executeUpdate( "drop sequence MyEntity_SEQ" );
		}
		catch (SQLException e) {
			// ignore the exception if table is not there and operation fails.
		}

		connection.createStatement().executeUpdate( "create sequence MyEntity_SEQ start with 1 increment by 50" );
		connection.createStatement().executeUpdate(
				"create table MyEntity (someBool number(1,0) check (someBool in (0,1)), id number(19,0) not null, someString varchar2(255 char), primary key (id))" );
	}

	public static void doBatchInsert(Connection connection) throws SQLException {
		String[] strings = new String[] { null, "Nêin" };
		Boolean[] booleans = new Boolean[] { null, Boolean.TRUE };

		String insert = "insert into MyEntity (someBool,someString,id) VALUES (?,?,?)";
		PreparedStatement statement = connection.prepareStatement( insert );
		for ( int i = 0; i < strings.length; i++ ) {
			int id = i + 1;
			statement.setInt( 3, id );
			if ( strings[i] == null ) {
				statement.setNull( 2, 12 );
			}
			else {
				statement.setString( 2, strings[i] );
			}
			if ( booleans[i] == null ) {
				statement.setNull( 1, -7 );
			}
			else {
				statement.setBoolean( 1, booleans[i] );
			}
			statement.addBatch();
		}
		statement.executeBatch();
		connection.commit();
	}
}
