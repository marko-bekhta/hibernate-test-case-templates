package org.hibernate.search.bugs;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.search.engine.search.predicate.SearchPredicate;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.DocumentId;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

import org.junit.jupiter.api.Test;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

public class YourIT extends SearchTestBase {

	@Override
	public Class<?>[] getAnnotatedClasses() {
		return new Class<?>[] { Case.class, Patent.class };
	}

	@Test
	public void testYourBug() {
		try ( Session s = getSessionFactory().openSession() ) {
			Case case1 = new Case( 1L, "case 1" );
			Case case2 = new Case( 2L, "case 2" );
			Patent patent1 = new Patent( 3L, "case 10", "patent1" );

			Transaction tx = s.beginTransaction();
			s.persist( case1 );
			s.persist( case2 );
			s.persist( patent1 );
			tx.commit();
		}

		try ( Session session = getSessionFactory().openSession() ) {
			SearchSession searchSession = Search.session( session );

			List<Long> hits = searchSession.search( Case.class )
					.select( f -> f.id( Long.class ) )
					.where( f -> f.match().field( "name" ).matching( "case" ) )
					.fetchHits( 20 );

			assertThat( hits )
					.hasSize( 3 )
					.containsOnly( 1L, 2L, 3L );

			SearchPredicate predicate = searchSession.scope( Case.class )
					.predicate().match().field( "name" ).matching( "case" ).toPredicate();

			hits = searchSession.search( Case.class )
					.select( f -> f.id( Long.class ) )
					.where( predicate )
					.fetchHits( 20 );

			assertThat( hits )
					.hasSize( 3 )
					.containsOnly( 1L, 2L, 3L );
		}
	}

	@Entity
	@Indexed
	@Inheritance(strategy = InheritanceType.JOINED)
	@Table(name = "t_case")
	public static class Case {

		@Id
		@DocumentId
		public Long id;

		@FullTextField
		public String name;

		public Case() {
		}

		public Case(Long id, String name) {
			this.id = id;
			this.name = name;
		}
	}

	@Entity
	@Indexed
	public static class Patent extends Case {

		@FullTextField
		String string;

		public Patent() {
		}

		public Patent(Long id, String name, String string) {
			super( id, name );
			this.string = string;
		}
	}

}
