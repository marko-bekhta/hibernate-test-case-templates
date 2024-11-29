/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.bugs;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.spi.SessionImplementor;

import org.hibernate.testing.bytecode.enhancement.extension.BytecodeEnhanced;
import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.hibernate.testing.orm.junit.Setting;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

/**
 * This template demonstrates how to develop a test case for Hibernate ORM, using its built-in unit test framework.
 * <p>
 * What's even better?  Fork hibernate-orm itself, add your test case directly to a module's unit tests, then
 * submit it as a PR!
 */
@DomainModel(
		annotatedClasses = {
				// Add your entities here, e.g.:
				QuarkusLikeORMUnitTestCase.MyEntity.class,
				QuarkusLikeORMUnitTestCase.MyOtherEntity.class
		}
)
@ServiceRegistry(
		// Add in any settings that are specific to your test.  See resources/hibernate.properties for the defaults.
		settings = {
				// For your own convenience to see generated queries:
				@Setting(name = AvailableSettings.SHOW_SQL, value = "true"),
				@Setting(name = AvailableSettings.FORMAT_SQL, value = "true"),
				// @Setting( name = AvailableSettings.GENERATE_STATISTICS, value = "true" ),

				// Other settings that will make your test case run under similar configuration that Quarkus is using by default:
				@Setting(name = AvailableSettings.PREFERRED_POOLED_OPTIMIZER, value = "pooled-lo"),
				@Setting(name = AvailableSettings.DEFAULT_BATCH_FETCH_SIZE, value = "16"),
				@Setting(name = AvailableSettings.BATCH_FETCH_STYLE, value = "PADDED"),
				@Setting(name = AvailableSettings.QUERY_PLAN_CACHE_MAX_SIZE, value = "2048"),
				@Setting(name = AvailableSettings.DEFAULT_NULL_ORDERING, value = "none"),
				@Setting(name = AvailableSettings.IN_CLAUSE_PARAMETER_PADDING, value = "true"),
				@Setting(name = AvailableSettings.SEQUENCE_INCREMENT_SIZE_MISMATCH_STRATEGY, value = "none"),

				// Add your own settings that are a part of your quarkus configuration:
				// @Setting( name = AvailableSettings.SOME_CONFIGURATION_PROPERTY, value = "SOME_VALUE" ),
		}
)
@SessionFactory
@BytecodeEnhanced
class QuarkusLikeORMUnitTestCase {

	@Test
	void failsAsSessionIsClosed(SessionFactoryScope scope) throws Exception {
		Long id = scope.fromTransaction( session -> {
			MyEntity e = new MyEntity();
			List<MyOtherEntity> list = new ArrayList<>();

			MyOtherEntity e2 = new MyOtherEntity();
			list.add( e2 );
			e.setOtherEntities( list );
			e2.entity = e;

			session.persist( e );
			session.persist( e2 );
			return e.id;
		} );

		MyEntity myEntity = scope.fromTransaction( session -> session.find( MyEntity.class, id ) );
		System.out.println( "=============================\nwill lazy load ?" );
		// fails as session is closed before the thing is lazy-loaded
		try {
			assertThat( myEntity.getOtherEntities() ).hasSize( 1 );
		}
		finally {
			System.out.println( "=============================\ndone" );
		}
		// ^ fails with:
		// org.hibernate.LazyInitializationException: failed to lazily initialize a collection of role: org.hibernate.bugs.QuarkusLikeORMUnitTestCase$MyEntity.otherEntities: could not initialize proxy - no Session

		// also this logs:
		//
 		//=============================
		//will lazy load ?
		//=============================
		//done
	}

	@Test
	void passesAsSessionNotClosedBeforeAccessingLazyCollection(SessionFactoryScope scope) throws Exception {
		Long id = scope.fromTransaction( session -> {
			MyEntity e = new MyEntity();
			List<MyOtherEntity> list = new ArrayList<>();

			MyOtherEntity e2 = new MyOtherEntity();
			list.add( e2 );
			e.setOtherEntities( list );
			e2.entity = e;

			session.persist( e );
			session.persist( e2 );
			return e.id;
		} );

		SessionImplementor session = scope.getSessionFactory().openSession();
		session.beginTransaction();
		MyEntity entity = session.find( MyEntity.class, id );
		session.getTransaction().commit();

		System.out.println( "=============================\nwill lazy load ?" );
		// passes as session is not closed before the thing is lazy-loaded
		try {
			assertThat( entity.getOtherEntities() ).hasSize( 1 );
		}
		finally {
			System.out.println( "=============================\ndone" );
		}
		session.close();

		// also this logs:
		//
		//=============================
		//will lazy load ?
		//Hibernate:
		//    select
		//        oe1_0."entity_id",
		//        oe1_0.id
		//    from
		//        "QuarkusLikeORMUnitTestCase$MyOtherEntity" oe1_0
		//    where
		//        oe1_0."entity_id"=?
		//=============================
		//done
	}

	@Entity
	public static class MyEntity {
		@Id
		@GeneratedValue
		public Long id;

		@OneToMany(mappedBy = "entity", fetch = FetchType.LAZY)
		public List<MyOtherEntity> otherEntities;

		public List<MyOtherEntity> getOtherEntities() {
			return otherEntities;
		}

		public void setOtherEntities(List<MyOtherEntity> otherEntities) {
			this.otherEntities = otherEntities;
		}

	}

	@Entity
	public static class MyOtherEntity {
		@Id
		@GeneratedValue
		public Long id;

		@ManyToOne
		public MyEntity entity;
	}
}
