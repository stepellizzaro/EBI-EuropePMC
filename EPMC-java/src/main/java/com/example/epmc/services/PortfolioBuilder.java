package com.example.epmc.services;

import com.example.epmc.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PortfolioBuilder {
    private static final Logger logger = LoggerFactory.getLogger(PortfolioBuilder.class);

    public GrantPortfolio buildPortfolio(Institution institution, List<Grant> grants, 
                                         Map<String, List<Publication>> publicationsMap) {
        logger.debug("Building portfolio for institution: {}", institution.getName());
        
        List<GrantWithPublications> grantsWithPublications = grants.stream()
                .map(grant -> buildGrantWithPublications(grant, publicationsMap))
                .collect(Collectors.toList());

        PortfolioSummary summary = calculateSummary(institution, grants, grantsWithPublications);

        GrantPortfolio portfolio = new GrantPortfolio();
        portfolio.setInstitution(institution);
        portfolio.setGrants(grantsWithPublications);
        portfolio.setSummary(summary);

        logger.info("Portfolio built successfully with {} grants", grantsWithPublications.size());
        return portfolio;
    }

    private GrantWithPublications buildGrantWithPublications(Grant grant, 
                                                             Map<String, List<Publication>> publicationsMap) {
        GrantWithPublications gwp = new GrantWithPublications();
        gwp.setGrant(grant);
        
        List<Publication> publications = publicationsMap.getOrDefault(grant.getId(), new ArrayList<>());
        gwp.setPublications(publications);
        
        return gwp;
    }

    private PortfolioSummary calculateSummary(Institution institution, List<Grant> grants, 
                                              List<GrantWithPublications> grantsWithPublications) {
        PortfolioSummary summary = new PortfolioSummary();
        
        summary.setTotalGrants(grants.size());
        
        int grantsWithPubs = (int) grantsWithPublications.stream()
                .filter(gwp -> gwp.getPublications() != null && !gwp.getPublications().isEmpty())
                .count();
        summary.setGrantsWithPublications(grantsWithPubs);
        
        int totalPublications = grantsWithPublications.stream()
                .mapToInt(gwp -> gwp.getPublications() != null ? gwp.getPublications().size() : 0)
                .sum();
        summary.setTotalPublications(totalPublications);
        
        Map<String, Integer> funderCounts = new HashMap<>();
        for (Grant grant : grants) {
            if (grant.getFunder() != null && !grant.getFunder().trim().isEmpty()) {
                funderCounts.merge(grant.getFunder(), 1, Integer::sum);
            }
        }
        summary.setFunderCounts(funderCounts);
        
        Map<PublicationStatus, Integer> statusCounts = new HashMap<>();
        for (Grant grant : grants) {
            if (grant.getPublicationStatus() != null) {
                statusCounts.merge(grant.getPublicationStatus(), 1, Integer::sum);
            }
        }
        summary.setPublicationStatusCounts(statusCounts);
        
        return summary;
    }
}
