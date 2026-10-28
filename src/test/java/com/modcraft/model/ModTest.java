package com.modcraft.model;

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

class ModTest {

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

    private static Map<String, String> violations(Mod mod) {
        Set<ConstraintViolation<Mod>> violations = validator.validate(mod);
        return violations.stream().collect(Collectors.toMap(
            v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage));
    }

    @Test
    void validate_allFieldsPresent_hasNoViolations() {
        Mod mod = new Mod("OptiFine", "1.0.0", "sp614x", "Performance mod", "http://example.com/optifine.jar", "1.20.1");
        assertThat(violations(mod)).isEmpty();
    }

    @Test
    void validate_authorDescriptionAndFileUrlAreOptional() {
        assertThat(violations(new Mod("OptiFine", "1.0.0", null, null, null, "1.20.1"))).isEmpty();
        assertThat(violations(new Mod("OptiFine", "1.0.0", "", "", "", "1.20.1"))).isEmpty();
    }

    @Test
    void validate_nullRequiredFields_areRejected() {
        assertThat(violations(new Mod(null, null, "sp614x", "desc", "http://example.com/a.jar", null)))
            .containsExactlyInAnyOrderEntriesOf(Map.of(
                "name", "must not be blank",
                "version", "must not be blank",
                "minecraftVersion", "must not be blank"));
    }

    @Test
    void validate_emptyRequiredFields_areRejected() {
        assertThat(violations(new Mod("", "", "sp614x", "desc", null, "")))
            .containsOnlyKeys("name", "version", "minecraftVersion");
    }

    @Test
    void validate_whitespaceOnlyRequiredFields_areRejected() {
        assertThat(violations(new Mod(" ", "\t", "sp614x", "desc", null, "  ")))
            .containsOnlyKeys("name", "version", "minecraftVersion");
    }

    @Test
    void validate_fileUrlIsNotCheckedForUrlFormat() {
        assertThat(violations(new Mod("OptiFine", "1.0.0", null, null, "not a url", "1.20.1"))).isEmpty();
    }

    @Test
    void validate_idIsNotConstrained() {
        Mod mod = new Mod("OptiFine", "1.0.0", null, null, null, "1.20.1");
        mod.setId(42L);
        assertThat(violations(mod)).isEmpty();
    }
}
