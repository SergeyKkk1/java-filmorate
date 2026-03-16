package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.yandex.practicum.filmorate.dto.validation.After;

import java.lang.annotation.Annotation;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class FilmDtoValidatorTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validFilm_hasNoViolations() {
        var dto = validFilmRqDto();
        assertThat(validator.validate(dto)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidNames")
    void nameValidation_invalidValues(String name, Set<Class<? extends Annotation>> expectedConstraints) {
        var dto = validFilmRqDto();
        dto.setName(name);

        var violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).allMatch(v -> v.getPropertyPath().toString().equals("name"));
        assertThat(constraintTypesFor(violations, "name")).isEqualTo(expectedConstraints);
    }

    @ParameterizedTest
    @MethodSource("validNames")
    void nameValidation_validValues(String name) {
        var dto = validFilmRqDto();
        dto.setName(name);

        assertThat(validator.validate(dto)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidDescriptions")
    void descriptionValidation_invalidValues(String description) {
        var dto = validFilmRqDto();
        dto.setDescription(description);

        var violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).allMatch(v -> v.getPropertyPath().toString().equals("description"));
        assertThat(constraintTypesFor(violations, "description")).isEqualTo(Set.of(Size.class));
    }

    @ParameterizedTest
    @MethodSource("validDescriptions")
    void descriptionValidation_validValues(String description) {
        var dto = validFilmRqDto();
        dto.setDescription(description);

        assertThat(validator.validate(dto)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidReleaseDates")
    void releaseDateValidation_invalidValues(LocalDate releaseDate, Set<Class<? extends Annotation>> expectedConstraints) {
        var dto = validFilmRqDto();
        dto.setReleaseDate(releaseDate);

        var violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).allMatch(v -> v.getPropertyPath().toString().equals("releaseDate"));
        assertThat(constraintTypesFor(violations, "releaseDate")).isEqualTo(expectedConstraints);
    }

    @ParameterizedTest
    @MethodSource("validReleaseDates")
    void releaseDateValidation_validValues(LocalDate releaseDate) {
        var dto = validFilmRqDto();
        dto.setReleaseDate(releaseDate);

        assertThat(validator.validate(dto)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidDurations")
    void durationValidation_invalidValues(Integer duration, Set<Class<? extends Annotation>> expectedConstraints) {
        var dto = validFilmRqDto();
        dto.setDuration(duration);

        var violations = validator.validate(dto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).allMatch(v -> v.getPropertyPath().toString().equals("duration"));
        assertThat(constraintTypesFor(violations, "duration")).isEqualTo(expectedConstraints);
    }

    @ParameterizedTest
    @MethodSource("validDurations")
    void durationValidation_validValues(Integer duration) {
        var dto = validFilmRqDto();
        dto.setDuration(duration);

        assertThat(validator.validate(dto)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("validContentRatings")
    void contentRatingValidation_validValues(Long contentRating) {
        var dto = validFilmRqDto();
        dto.setMpa(new IdDto().setId(contentRating));

        assertThat(validator.validate(dto)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("validIds")
    void id_hasNoValidationConstraints(Long id) {
        var dto = validFilmRqDto();
        dto.setId(id);

        assertThat(validator.validate(dto)).isEmpty();
    }

    private static FilmRqDto validFilmRqDto() {
        var dto = new FilmRqDto();
        dto.setId(1L);
        dto.setName("Inception");
        dto.setDescription("A valid description");
        dto.setReleaseDate(LocalDate.of(2010, 7, 16));
        dto.setDuration(148);
        dto.setMpa(new IdDto().setId(3L));
        dto.setGenres(Set.of(new IdDto().setId(1L), new IdDto().setId(4L)));
        return dto;
    }

    private static Set<Class<? extends Annotation>> constraintTypesFor(
            Set<jakarta.validation.ConstraintViolation<FilmRqDto>> violations,
            String property
    ) {
        return violations.stream()
                .filter(v -> v.getPropertyPath().toString().equals(property))
                .map(v -> v.getConstraintDescriptor().getAnnotation().annotationType())
                .collect(Collectors.toSet());
    }

    private static Stream<Arguments> invalidNames() {
        return Stream.of(
                Arguments.of(null, Set.of(NotBlank.class)),
                Arguments.of("", Set.of(NotBlank.class)),
                Arguments.of(" ", Set.of(NotBlank.class))
        );
    }

    private static Stream<Arguments> validNames() {
        return Stream.of(
                Arguments.of("Film")
        );
    }

    private static Stream<Arguments> invalidDescriptions() {
        return Stream.of(
                Arguments.of("a".repeat(201))
        );
    }

    private static Stream<Arguments> validDescriptions() {
        return Stream.of(
                Arguments.of((String) null),
                Arguments.of(""),
                Arguments.of("a".repeat(200))
        );
    }

    private static Stream<Arguments> invalidReleaseDates() {
        return Stream.of(
                Arguments.of(null, Set.of(NotNull.class)),
                Arguments.of(LocalDate.of(1895, 12, 27), Set.of(After.class)),
                Arguments.of(LocalDate.of(1800, 1, 1), Set.of(After.class))
        );
    }

    private static Stream<Arguments> validReleaseDates() {
        return Stream.of(
                Arguments.of(LocalDate.of(1895, 12, 28)),
                Arguments.of(LocalDate.of(2000, 1, 1))
        );
    }

    private static Stream<Arguments> invalidDurations() {
        return Stream.of(
                Arguments.of(null, Set.of(NotNull.class)),
                Arguments.of(0, Set.of(Positive.class)),
                Arguments.of(-1, Set.of(Positive.class)),
                Arguments.of(Integer.MIN_VALUE, Set.of(Positive.class))
        );
    }

    private static Stream<Arguments> validDurations() {
        return Stream.of(
                Arguments.of(1),
                Arguments.of(120)
        );
    }

    private static Stream<Arguments> validContentRatings() {
        return Stream.of(
                Arguments.of(1L),
                Arguments.of(5L)
        );
    }

    private static Stream<Arguments> validIds() {
        return Stream.of(
                Arguments.of((Long) null),
                Arguments.of(0L),
                Arguments.of(-1L),
                Arguments.of(1L)
        );
    }
}
