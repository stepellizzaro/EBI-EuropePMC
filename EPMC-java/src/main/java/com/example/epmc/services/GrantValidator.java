package com.example.epmc.services;

import com.example.epmc.exceptions.ValidationException;
import com.example.epmc.models.Grant;
import com.example.epmc.models.PortfolioRequest;
import org.springframework.stereotype.Service;

@Service
public class GrantValidator {

    private static final int MAX_INSTITUTION_LENGTH = 255;
    private static final int MAX_GRANTS = 10;
    private static final int MAX_PUBLICATIONS = 5;

    public void validateRequest(PortfolioRequest request) {
        if (request.getInstitution() == null || request.getInstitution().trim().isEmpty()) {
            throw new ValidationException("Institution name is required");
        }

        if (request.getInstitution().length() > MAX_INSTITUTION_LENGTH) {
            throw new ValidationException("Institution name cannot exceed " + MAX_INSTITUTION_LENGTH + " characters");
        }

        if (request.getMaxGrants() > MAX_GRANTS) {
            throw new ValidationException("maxGrants cannot exceed " + MAX_GRANTS);
        }

        if (request.getMaxPublications() > MAX_PUBLICATIONS) {
            throw new ValidationException("maxPublications cannot exceed " + MAX_PUBLICATIONS);
        }

        if (request.getMaxGrants() < 1) {
            throw new ValidationException("maxGrants must be at least 1");
        }

        if (request.getMaxPublications() < 1) {
            throw new ValidationException("maxPublications must be at least 1");
        }
    }

    public boolean isValidGrant(Grant grant) {
        return grant != null
                && grant.getId() != null && !grant.getId().trim().isEmpty()
                && grant.getTitle() != null && !grant.getTitle().trim().isEmpty()
                && grant.getFunder() != null && !grant.getFunder().trim().isEmpty();
    }
}
