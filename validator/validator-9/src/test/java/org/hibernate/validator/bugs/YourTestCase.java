package org.hibernate.validator.bugs;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.validator.testutil.TestForIssue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

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
	void testYourBug() throws NoSuchMethodException {
		EventsControllerApi instance = new EventsControllerApi() {
			@Override
			public List<ResourceDto> getEventsByResources(
					String organizationId,
					List<@NotNull @Valid ResourceIdRequestDto> resourceIds) {
				return List.of();
			}
		};

		Set<ConstraintViolation<EventsControllerApi>> constraintViolations = validator.forExecutables()
				.validateParameters(
						instance,
						EventsControllerApi.class.getMethod( "getEventsByResources", String.class, List.class ),
						new Object[] {
								"some-org-id",
								List.of( new ResourceIdRequestDto( "d1", null, UUID.randomUUID() ) )
						}
				);
		assertThat( constraintViolations )
				.containsOnlyViolations( violationOf( NotBlank.class ).withPropertyPath(
						pathWith()
								.method( "getEventsByResources" )
								.parameter( "arg1", 1 )
								.property( "type", true, null, 0, List.class, 0 ) ) );
	}

	public interface EventsControllerApi {

		List<ResourceDto> getEventsByResources(
				String organizationId,
				List<@NotNull @Valid ResourceIdRequestDto> resourceIds);
	}

	public record ResourceDto(int id) {
	}

	public record ResourceIdRequestDto(
			@NotBlank
			String domain,
			@NotBlank
			String type,
			@NotNull UUID id) {
	}
}
