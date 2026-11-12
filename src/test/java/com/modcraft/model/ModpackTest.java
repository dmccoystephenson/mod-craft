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

class ModpackTest {

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

    private static Map<String, String> violations(Modpack modpack) {
        Set<ConstraintViolation<Modpack>> violations = validator.validate(modpack);
        return violations.stream().collect(Collectors.toMap(
            v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage));
    }

    @Test
    void validate_allFieldsPresent_hasNoViolations() {
        assertThat(violations(new Modpack("Performance Pack", "Speed mods", "1.20.1"))).isEmpty();
    }

    @Test
    void validate_descriptionIsOptional() {
        assertThat(violations(new Modpack("Performance Pack", null, "1.20.1"))).isEmpty();
        assertThat(violations(new Modpack("Performance Pack", "", "1.20.1"))).isEmpty();
    }

    @Test
    void validate_nullRequiredFields_areRejected() {
        assertThat(violations(new Modpack(null, "Speed mods", null)))
            .containsExactlyInAnyOrderEntriesOf(Map.of(
                "name", "must not be blank",
                "minecraftVersion", "must not be blank"));
    }

    @Test
    void validate_emptyRequiredFields_areRejected() {
        assertThat(violations(new Modpack("", "Speed mods", "")))
            .containsOnlyKeys("name", "minecraftVersion");
    }

    @Test
    void validate_whitespaceOnlyRequiredFields_areRejected() {
        assertThat(violations(new Modpack(" ", "Speed mods", "\t")))
            .containsOnlyKeys("name", "minecraftVersion");
    }

    @Test
    void validate_noArgConstructor_rejectsBothRequiredFields() {
        assertThat(violations(new Modpack())).containsOnlyKeys("name", "minecraftVersion");
    }

    // Unlike Mod, the Modpack entity carries no @Size constraints: the 255-character
    // limit on modpack fields is enforced on the ModpackRequest DTO that the
    // controller validates before it builds the entity.
    @Test
    void validate_fieldLengthIsNotConstrainedOnEntity() {
        String tooLong = "x".repeat(256);
        assertThat(violations(new Modpack(tooLong, tooLong, tooLong))).isEmpty();
    }

    @Test
    void validate_modsAreNotValidatedThroughTheModpack() {
        Modpack modpack = new Modpack("Performance Pack", null, "1.20.1");
        modpack.getMods().add(new Mod(null, null, null, null, null, null));
        assertThat(violations(modpack)).isEmpty();
    }

    @Test
    void validate_idIsNotConstrained() {
        Modpack modpack = new Modpack("Performance Pack", null, "1.20.1");
        modpack.setId(42L);
        assertThat(violations(modpack)).isEmpty();
    }

    @Test
    void constructor_leavesIdNullAndModsEmpty() {
        Modpack modpack = new Modpack("Performance Pack", "Speed mods", "1.20.1");
        assertThat(modpack.getId()).isNull();
        assertThat(modpack.getName()).isEqualTo("Performance Pack");
        assertThat(modpack.getDescription()).isEqualTo("Speed mods");
        assertThat(modpack.getMinecraftVersion()).isEqualTo("1.20.1");
        assertThat(modpack.getMods()).isEmpty();
    }

    @Test
    void getMods_defaultListIsMutableAndNotSharedBetweenInstances() {
        Modpack first = new Modpack();
        Modpack second = new Modpack();
        Mod mod = new Mod("OptiFine", "1.0.0", null, null, null, "1.20.1");

        first.getMods().add(mod);

        assertThat(first.getMods()).containsExactly(mod);
        assertThat(second.getMods()).isEmpty();
    }
}
