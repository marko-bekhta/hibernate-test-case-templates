package org.hibernate.search.bugs;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;

import org.junit.jupiter.api.Test;

public class YourIT extends SearchTestBase {

	@Override
	public Class<?>[] getAnnotatedClasses() {
		return new Class<?>[] { YourAnnotatedEntity.class };
	}

	@Test
	public void testYourBug() throws InterruptedException {
		try ( Session s = getSessionFactory().openSession() ) {
			// so that we do not index while we create entities:
			Search.mapping( s.getSessionFactory() ).indexingPlanFilter( ctx -> ctx.exclude( YourAnnotatedEntity.class ) );

			long id = 0L;
			for ( int i = 0; i < 10_000; i++ ) {
				Transaction tx = s.beginTransaction();
				for ( int j = 0; j < 1000; j++ ) {
					id++;
					YourAnnotatedEntity entity = new YourAnnotatedEntity( id, "name " + id );
					s.persist( entity );
				}
				System.err.println( "iteration : " + i );
				s.flush();
				s.clear();
				tx.commit();
			}
		}

		try ( Session session = getSessionFactory().openSession() ) {
			SearchSession searchSession = Search.session( session );

			searchSession.massIndexer( YourAnnotatedEntity.class )
					.threadsToLoadObjects( 2 )
					.batchSizeToLoadObjects( 100 )
					.startAndWait();

			List<Long> hits = searchSession.search( YourAnnotatedEntity.class )
					.select( f -> f.id( Long.class ) )
					.where( f -> f.match().field( "name" ).matching( "100" ) )
					.fetchHits( 20 );

			assertThat( hits )
					.hasSize( 1 )
					.contains( 100L );
		}
	}

}
