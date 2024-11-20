package org.hibernate.search.bugs;

import java.io.Closeable;
import java.time.Duration;

import org.testcontainers.containers.PostgreSQLContainer;

class DatabaseContainer implements Closeable {

	private PostgreSQLContainer<?> postgresqlContainer;

	public PostgreSQLContainer<?> setUp() {
		postgresqlContainer = new PostgreSQLContainer<>( "postgres:17.0" )
				.withExposedPorts( 5432 )
				.withStartupTimeout( Duration.ofMinutes( 5 ) )
				.withReuse( true )
				.withDatabaseName( "hibernate_orm_test" )
				.withUsername( "hibernate_orm_test" )
				.withPassword( "hibernate_orm_test" );
		postgresqlContainer.start();
		return postgresqlContainer;
	}

	@Override
	public void close() {
		try ( PostgreSQLContainer<?> container = this.postgresqlContainer ) {
			// Nothing to do: we just want resources to get closed.
		}
	}
}
