package com.example.epmc;

import com.example.epmc.exceptions.ValidationException;
import com.example.epmc.models.Grant;
import com.example.epmc.models.PortfolioRequest;
import com.example.epmc.services.GrantValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Grant Validator Tests")
class GrantValidatorTest {

    private GrantValidator validator;

    @BeforeEach
    void setUp() {
        validator = new GrantValidator();
    }

    // ============ Happy Path Tests ============

    @Test
    @DisplayName("Should accept valid request with default limits")
    void shouldAcceptValidRequestWithDefaults() {
        PortfolioRequest request = new PortfolioRequest("Cambridge", 10, 5);
        assertDoesNotThrow(() -> validator.validateRequest(request));
    }

    @Test
    @DisplayName("Should accept valid request with custom limits")
    void shouldAcceptValidRequestWithCustomLimits() {
        PortfolioRequest request = new PortfolioRequest("Manchester", 5, 3);
        assertDoesNotThrow(() -> validator.validateRequest(request));
    }

    @Test
    @DisplayName("Should accept valid institutional name variations")
    void shouldAcceptValidInstitutionNames() {
        String[] validNames = {"Cambridge", "Oxford", "MIT", "Stanford", "A"};
        for (String name : validNames) {
            PortfolioRequest request = new PortfolioRequest(name, 10, 5);
            assertDoesNotThrow(() -> validator.validateRequest(request));
        }
    }

    // ============ Institution Validation Tests ============

    @Test
    @DisplayName("Should reject empty institution name")
    void shouldRejectEmptyInstitution() {
        PortfolioRequest request = new PortfolioRequest("", 10, 5);
        assertThrows(ValidationException.class, () -> validator.validateRequest(request));
    }

    @Test
    @DisplayName("Should reject null institution name")
    void shouldRejectNullInstitution() {
        PortfolioRequest request = new PortfolioRequest(null, 10, 5);
        assertThrows(ValidationException.class, () -> validator.validateRequest(request));
    }

    @Test
    @DisplayName("Should reject institution name exceeding 255 characters")
    void shouldRejectLongInstitutionName() {
        String longName = "A".repeat(256);
        PortfolioRequest request = new PortfolioRequest(longName, 10, 5);
        assertThrows(ValidationException.class, () -> validator.validateRequest(request));
    }

    @Test
    @DisplayName("Should accept institution name at max length (255 chars)")
    void shouldAcceptMaxLengthInstitution() {
        String maxName = "A".repeat(255);
        PortfolioRequest request = new PortfolioRequest(maxName, 10, 5);
        assertDoesNotThrow(() -> validator.validateRequest(request));
    }

    // ============ Limit Validation Tests ============

    @Test
    @DisplayName("Should reject maxGrants exceeding 10")
    void shouldRejectMaxGrantsExceeded() {
        PortfolioRequest request = new PortfolioRequest("Cambridge", 11, 5);
        assertThrows(ValidationException.class, () -> validator.validateRequest(request));
    }

    @Test
    @DisplayName("Should reject maxPublications exceeding 5")
    void shouldRejectMaxPublicationsExceeded() {
        PortfolioRequest request = new PortfolioRequest("Cambridge", 10, 6);
        assertThrows(ValidationException.class, () -> validator.validateRequest(request));
    }

    @Test
    @DisplayName("Should reject zero or negative maxGrants")
    void shouldRejectNegativeOrZeroGrants() {
        PortfolioRequest request1 = new PortfolioRequest("Cambridge", 0, 5);
        PortfolioRequest request2 = new PortfolioRequest("Cambridge", -1, 5);
        assertThrows(ValidationException.class, () -> validator.validateRequest(request1));
        assertThrows(ValidationException.class, () -> validator.validateRequest(request2));
    }

    @Test
    @DisplayName("Should reject zero or negative maxPublications")
    void shouldRejectNegativeOrZeroPublications() {
        PortfolioRequest request1 = new PortfolioRequest("Cambridge", 10, 0);
        PortfolioRequest request2 = new PortfolioRequest("Cambridge", 10, -1);
        assertThrows(ValidationException.class, () -> validator.validateRequest(request1));
        assertThrows(ValidationException.class, () -> validator.validateRequest(request2));
    }

    @Test
    @DisplayName("Should accept boundary values (10 grants, 5 publications)")
    void shouldAcceptBoundaryValues() {
        PortfolioRequest request = new PortfolioRequest("Cambridge", 10, 5);
        assertDoesNotThrow(() -> validator.validateRequest(request));
    }

    // ============ Grant Validation Tests ============

    @Test
    @DisplayName("Should mark grant as invalid if ID is missing")
    void shouldMarkGrantInvalidWithoutId() {
        Grant grant = new Grant(null, "Title", "Funder", "Institution", "link", null, null);
        assertFalse(validator.isValidGrant(grant));
    }

    @Test
    @DisplayName("Should mark grant as invalid if title is missing")
    void shouldMarkGrantInvalidWithoutTitle() {
        Grant grant = new Grant("ID", null, "Funder", "Institution", "link", null, null);
        assertFalse(validator.isValidGrant(grant));
    }

    @Test
    @DisplayName("Should mark grant as invalid if funder is missing")
    void shouldMarkGrantInvalidWithoutFunder() {
        Grant grant = new Grant("ID", "Title", null, "Institution", "link", null, null);
        assertFalse(validator.isValidGrant(grant));
    }

    @Test
    @DisplayName("Should mark grant as valid with required fields")
    void shouldMarkGrantValidWithRequiredFields() {
        Grant grant = new Grant("ID", "Title", "Funder", "Institution", "link", null, null);
        assertTrue(validator.isValidGrant(grant));
    }

    @Test
    @DisplayName("Should mark grant as valid even with missing optional fields")
    void shouldMarkGrantValidWithMissingOptionalFields() {
        Grant grant = new Grant("ID", "Title", "Funder", null, null, null, null);
        assertTrue(validator.isValidGrant(grant));
    }

    @Test
    @DisplayName("Should reject null grant")
    void shouldRejectNullGrant() {
        assertFalse(validator.isValidGrant(null));
    }

    @Test
    @DisplayName("Should reject grant with empty required fields")
    void shouldRejectGrantWithEmptyFields() {
        Grant grant1 = new Grant("", "Title", "Funder", "Institution", "link", null, null);
        Grant grant2 = new Grant("ID", "", "Funder", "Institution", "link", null, null);
        Grant grant3 = new Grant("ID", "Title", "", "Institution", "link", null, null);
        
        assertFalse(validator.isValidGrant(grant1));
        assertFalse(validator.isValidGrant(grant2));
        assertFalse(validator.isValidGrant(grant3));
    }

    // ============ Edge Case Tests ============

    @Test
    @DisplayName("Should handle whitespace-only institution name")
    void shouldRejectWhitespaceOnlyInstitution() {
        PortfolioRequest request = new PortfolioRequest("   ", 10, 5);
        assertThrows(ValidationException.class, () -> validator.validateRequest(request));
    }

    @Test
    @DisplayName("Should handle maximum boundary values exactly")
    void shouldAcceptExactMaxValues() {
        PortfolioRequest request = new PortfolioRequest("Cambridge", 10, 5);
        assertDoesNotThrow(() -> validator.validateRequest(request));
    }

    @Test
    @DisplayName("Should handle minimum valid values")
    void shouldAcceptMinimumValidValues() {
        PortfolioRequest request = new PortfolioRequest("X", 1, 1);
        assertDoesNotThrow(() -> validator.validateRequest(request));
    }

    @Test
    @DisplayName("Should handle special characters in institution name")
    void shouldAcceptSpecialCharacters() {
        PortfolioRequest request = new PortfolioRequest("Cambridge-University (UK)", 10, 5);
        assertDoesNotThrow(() -> validator.validateRequest(request));
    }

    @Test
    @DisplayName("Should handle unicode characters in institution name")
    void shouldAcceptUnicodeCharacters() {
        PortfolioRequest request = new PortfolioRequest("Universität München", 10, 5);
        assertDoesNotThrow(() -> validator.validateRequest(request));
    }
}
