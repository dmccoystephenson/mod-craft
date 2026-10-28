package com.modcraft.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ModpackRequestTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    private static Map<String, String> violations(ModpackRequest request) {
        Set<ConstraintViolation<ModpackRequest>> violations = validator.validate(request);
        return violations.stream().collect(Collectors.toMap(
            v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage));
    }

    @Test
    void validate_allFieldsPresent_hasNoViolations() {
        assertThat(violations(new ModpackRequest("Test Pack", "A test modpack", "1.20.1"))).isEmpty();
    }

    @Test
    void validate_descriptionIsOptional() {
        assertThat(violations(new ModpackRequest("Test Pack", null, "1.20.1"))).isEmpty();
        assertThat(violations(new ModpackRequest("Test Pack", "", "1.20.1"))).isEmpty();
    }

    @Test
    void validate_nullRequiredFields_areRejected() {
        assertThat(violations(new ModpackRequest(null, "desc", null))).containsExactlyInAnyOrderEntriesOf(Map.of(
            "name", "must not be blank",
            "minecraftVersion", "must not be blank"));
    }

    @Test
    void validate_emptyRequiredFields_areRejected() {
        assertThat(violations(new ModpackRequest("", "desc", ""))).containsOnlyKeys("name", "minecraftVersion");
    }

    @Test
    void validate_whitespaceOnlyRequiredFields_areRejected() {
        assertThat(violations(new ModpackRequest("   ", "desc", "\t"))).containsOnlyKeys("name", "minecraftVersion");
    }

    @Test
    void validate_noArgConstructor_rejectsBothRequiredFields() {
        assertThat(violations(new ModpackRequest())).containsOnlyKeys("name", "minecraftVersion");
    }
}
