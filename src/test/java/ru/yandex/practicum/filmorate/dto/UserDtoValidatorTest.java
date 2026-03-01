package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.annotation.Annotation;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class UserDtoValidatorTest {
    private static final LocalDate TODAY = LocalDate.of(2024, 1, 1);
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.byDefaultProvider()
                .configure()
                .clockProvider(() -> FIXED_CLOCK)
                .buildValidatorFactory()
                .getValidator();
    }

    @Test
    void validUser_hasNoViolations() {
        var dto = validUserDto();
        assertThat(validator.validate(dto)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidEmails")
    void emailValidation_invalidValues(String email, Set<Class<? extends Annotation>> expectedConstraints) {
        var dto = validUserDto();
        dto.setEmail(email);

        var violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).allMatch(v -> v.getPropertyPath().toString().equals("email"));
        assertThat(constraintTypesFor(violations, "email")).isEqualTo(expectedConstraints);
    }

    @ParameterizedTest
    @MethodSource("validEmails")
    void emailValidation_validValues(String email) {
        var dto = validUserDto();
        dto.setEmail(email);

        assertThat(validator.validate(dto)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidLogins")
    void loginValidation_invalidValues(String login, Set<Class<? extends Annotation>> expectedConstraints) {
        var dto = validUserDto();
        dto.setLogin(login);

        var violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).allMatch(v -> v.getPropertyPath().toString().equals("login"));
        assertThat(constraintTypesFor(violations, "login")).isEqualTo(expectedConstraints);
    }

    @ParameterizedTest
    @MethodSource("validLogins")
    void loginValidation_validValues(String login) {
        var dto = validUserDto();
        dto.setLogin(login);

        assertThat(validator.validate(dto)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidBirthdays")
    void birthdayValidation_invalidValues(LocalDate birthday) {
        var dto = validUserDto();
        dto.setBirthday(birthday);

        var violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).allMatch(v -> v.getPropertyPath().toString().equals("birthday"));
        assertThat(constraintTypesFor(violations, "birthday")).isEqualTo(Set.of(PastOrPresent.class));
    }

    @ParameterizedTest
    @MethodSource("validBirthdays")
    void birthdayValidation_validValues(LocalDate birthday) {
        var dto = validUserDto();
        dto.setBirthday(birthday);

        assertThat(validator.validate(dto)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("validNames")
    void name_hasNoValidationConstraints(String name) {
        var dto = validUserDto();
        dto.setName(name);

        assertThat(validator.validate(dto)).isEmpty();
    }

    private static UserDto validUserDto() {
        var dto = new UserDto();
        dto.setEmail("a@b.com");
        dto.setLogin("john");
        dto.setName("John");
        dto.setBirthday(LocalDate.of(2000, 1, 1));
        return dto;
    }

    private static Set<Class<? extends Annotation>> constraintTypesFor(
            Set<jakarta.validation.ConstraintViolation<UserDto>> violations,
            String property
    ) {
        return violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals(property))
                .map(v -> v.getConstraintDescriptor().getAnnotation().annotationType())
                .collect(Collectors.toSet());
    }

    private static Stream<Arguments> invalidEmails() {
        return Stream.of(
                Arguments.of(null, Set.of(NotEmpty.class)),
                Arguments.of("", Set.of(NotEmpty.class)),
                Arguments.of("not-an-email", Set.of(Email.class)),
                Arguments.of("a b@c.com", Set.of(Email.class))
        );
    }

    private static Stream<Arguments> validEmails() {
        return Stream.of(
                Arguments.of("a@b.com"),
                Arguments.of("user.name+tag@domain.co")
        );
    }

    private static Stream<Arguments> invalidLogins() {
        return Stream.of(
                Arguments.of(null, Set.of(NotEmpty.class)),
                Arguments.of("", Set.of(NotEmpty.class, Pattern.class)),
                Arguments.of(" ", Set.of(Pattern.class)),
                Arguments.of("john doe", Set.of(Pattern.class)),
                Arguments.of("john\tdoe", Set.of(Pattern.class)),
                Arguments.of("john\ndoe", Set.of(Pattern.class))
        );
    }

    private static Stream<Arguments> validLogins() {
        return Stream.of(
                Arguments.of("john"),
                Arguments.of("john_doe"),
                Arguments.of("john.doe+123")
        );
    }

    private static Stream<Arguments> invalidBirthdays() {
        return Stream.of(
                Arguments.of(TODAY.plusDays(1))
        );
    }

    private static Stream<Arguments> validBirthdays() {
        return Stream.of(
                Arguments.of((LocalDate) null),
                Arguments.of(TODAY.minusDays(1)),
                Arguments.of(TODAY),
                Arguments.of(LocalDate.of(1900, 1, 1))
        );
    }

    private static Stream<Arguments> validNames() {
        return Stream.of(
                Arguments.of((String) null),
                Arguments.of(""),
                Arguments.of(" "),
                Arguments.of("John Doe")
        );
    }
}
