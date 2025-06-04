package org.hibernate.validator.bugs;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.List;
import java.util.Set;

import org.hibernate.validator.testutil.TestForIssue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;

class YourTestCase {

	private static Validator validator;

	@BeforeAll
	public static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	@TestForIssue(jiraKey = "HV-NNNNN")
		// Please fill in the JIRA key of your issue
	void testYourBug() {
		YourAnnotatedBean yourEntity1 = new YourAnnotatedBean( null, "example" );

		Data data = new Data();
		data.list = List.of( List.of(), List.of( yourEntity1 ), List.of() );
		data.deeperList = List.of( List.of(), List.of( List.of( yourEntity1 ) ), List.of( List.of() ) );


		Set<ConstraintViolation<Data>> constraintViolations = validator.validate( data );
		assertThat( constraintViolations )
				.containsOnlyViolations(
						violationOf( NotNull.class )
								.withMessage( "must not be null" )
								.withPropertyPath( pathWith().property( "list" )
										.containerElement( "<list element>", true, null, 1, List.class, 0 )
										.property( "id", true, null, 0, List.class, 0 ) ),
						violationOf( NotNull.class )
								.withMessage( "must not be null" )
								.withPropertyPath( pathWith().property( "deeperList" )
										.containerElement( "<list element>", true, null, 1, List.class, 0 )
										.containerElement( "<list element>", true, null, 0, List.class, 0 )
										.property( "id", true, null, 0, List.class, 0 ) )
				);
	}


	public static class Data {
		List<List<@Valid YourAnnotatedBean>> list;
		List<List<List<@Valid YourAnnotatedBean>>> deeperList;
	}

	public static class YourAnnotatedBean {

		@NotNull
		private Long id;

		private String name;

		protected YourAnnotatedBean() {
		}

		public YourAnnotatedBean(Long id, String name) {
			this.id = id;
			this.name = name;
		}

		public Long getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}
}
