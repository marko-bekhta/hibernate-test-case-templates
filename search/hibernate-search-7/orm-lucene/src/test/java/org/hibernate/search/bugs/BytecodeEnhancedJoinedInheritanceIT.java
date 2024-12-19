package org.hibernate.search.bugs;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;

import org.hibernate.testing.bytecode.enhancement.extension.BytecodeEnhanced;
import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.hibernate.testing.orm.junit.Setting;
import org.junit.jupiter.api.Test;

@DomainModel(
		annotatedClasses = {
				Order.class,
				LocalNumberPortOrder.class
		}
)
@ServiceRegistry(
		settings = {
				// For your own convenience to see generated queries:
				@Setting(name = AvailableSettings.SHOW_SQL, value = "true"),
				@Setting(name = AvailableSettings.FORMAT_SQL, value = "true"),
		}
)
@SessionFactory
@BytecodeEnhanced(
		runNotEnhancedAsWell = true,
		testEnhancedClasses = {
				Order.class,
				LocalNumberPortOrder.class
		}
)
class BytecodeEnhancedJoinedInheritanceIT {

	@Test
	void test(SessionFactoryScope scope) {
		scope.inTransaction( session -> {
			LocalNumberPortOrder e = new LocalNumberPortOrder();
			e.setId( 1L );
			e.setTags( "tags" );
			e.setProviderOrderIdentifier( "providerOrder" );

			session.persist( e );
		} );

		scope.inTransaction( session -> {
			SearchSession searchSession = Search.session( session );

			List<LocalNumberPortOrder> hits = searchSession.search( LocalNumberPortOrder.class )
					.where( f -> f.match().field( "localNumberPortOrder_providerOrderIdentifier" ).matching( "providerOrder" ) )
					.fetchHits( 20 );

			assertThat( hits )
					.hasSize( 1 )
					.element( 0 ).extracting( LocalNumberPortOrder::getId )
					.isEqualTo( 1L );
		} );
	}
}
