package com.example.epmc.services;

import com.example.epmc.config.PortfolioConfiguration;
import com.example.epmc.exceptions.ExternalApiException;
import com.example.epmc.exceptions.RequestTimeoutException;
import com.example.epmc.models.*;
import com.example.epmc.services.interfaces.ArticlesApiClient;
import com.example.epmc.services.interfaces.GristApiClient;
import com.example.epmc.services.interfaces.PortfolioService;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class PortfolioServiceImpl implements PortfolioService {
    private static final Logger logger = LoggerFactory.getLogger(PortfolioServiceImpl.class);

    private final GristApiClient gristApiClient;
    private final ArticlesApiClient articlesApiClient;
    private final GrantValidator grantValidator;
    private final PortfolioBuilder portfolioBuilder;
    private final PortfolioConfiguration config;
    private final Semaphore concurrencyLimiter;

    public PortfolioServiceImpl(GristApiClient gristApiClient, ArticlesApiClient articlesApiClient,
                               GrantValidator grantValidator, PortfolioBuilder portfolioBuilder,
                               PortfolioConfiguration config) {
        this.gristApiClient = gristApiClient;
        this.articlesApiClient = articlesApiClient;
        this.grantValidator = grantValidator;
        this.portfolioBuilder = portfolioBuilder;
        this.config = config;
        this.concurrencyLimiter = new Semaphore(config.getMaxConcurrentLookups());
    }

    @Override
    public CompletableFuture<GrantPortfolio> getPortfolioAsync(PortfolioRequest request) {
        logger.info("Processing portfolio request for institution: {}", request.getInstitution());

        grantValidator.validateRequest(request);

        Institution institution = new Institution();
        institution.setName(request.getInstitution());

        long startTime = System.currentTimeMillis();

        return gristApiClient.fetchGrantsAsync(request.getInstitution(), request.getMaxGrants())
                .thenCompose(grants -> {
                    logger.info("Fetched {} grants for institution: {}", grants.size(), request.getInstitution());

                    List<Grant> validGrants = grants.stream()
                            .filter(grantValidator::isValidGrant)
                            .collect(Collectors.toList());

                    logger.info("Found {} valid grants (filtered from {})", validGrants.size(), grants.size());

                    return fetchPublicationsConcurrently(validGrants, request.getMaxPublications())
                            .thenApply(publicationsMap -> {
                                long elapsedTime = System.currentTimeMillis() - startTime;
                                logger.info("Portfolio processing completed in {} ms", elapsedTime);

                                if (elapsedTime > config.getOverallTimeoutSeconds() * 1000) {
                                    logger.warn("Portfolio processing exceeded overall timeout threshold");
                                    throw new RequestTimeoutException("Portfolio request processing exceeded timeout");
                                }

                                return portfolioBuilder.buildPortfolio(institution, validGrants, publicationsMap);
                            });
                })
                .exceptionally(throwable -> {
                    logger.error("Error processing portfolio request", throwable);
                    if (throwable instanceof ValidationException) {
                        throw (ValidationException) throwable;
                    } else if (throwable instanceof RequestTimeoutException) {
                        throw (RequestTimeoutException) throwable;
                    } else if (throwable instanceof ExternalApiException) {
                        throw (ExternalApiException) throwable;
                    } else if (throwable.getCause() instanceof TimeoutException) {
                        throw new RequestTimeoutException("Request timeout while fetching data", throwable);
                    } else {
                        throw new ExternalApiException("Error processing portfolio request: " + throwable.getMessage(), throwable);
                    }
                });
    }

    private CompletableFuture<Map<String, List<Publication>>> fetchPublicationsConcurrently(
            List<Grant> grants, int maxPublications) {

        logger.debug("Starting concurrent publication fetch for {} grants", grants.size());

        List<CompletableFuture<Void>> futures = grants.stream()
                .map(grant -> fetchPublicationsWithSemaphore(grant, maxPublications))
                .collect(Collectors.toList());

        Map<String, List<Publication>> publicationsMap = new ConcurrentHashMap<>();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    logger.debug("All publication fetches completed");
                    return populatePublicationsMap(grants, maxPublications, publicationsMap);
                });
    }

    private CompletableFuture<Void> fetchPublicationsWithSemaphore(Grant grant, int maxPublications) {
        return CompletableFuture.runAsync(() -> {
            try {
                concurrencyLimiter.acquire();
                logger.debug("Acquired semaphore permit for grant: {}", grant.getId());

                try {
                    articlesApiClient.fetchPublicationsAsync(grant.getId(), maxPublications)
                            .toCompletableFuture()
                            .join();
                } finally {
                    concurrencyLimiter.release();
                    logger.debug("Released semaphore permit for grant: {}", grant.getId());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Interrupted while waiting for semaphore", e);
                throw new RequestTimeoutException("Interrupted while fetching publications", e);
            }
        });
    }

    private Map<String, List<Publication>> populatePublicationsMap(List<Grant> grants, int maxPublications,
                                                                     Map<String, List<Publication>> publicationsMap) {
        for (Grant grant : grants) {
            try {
                List<Publication> publications = articlesApiClient.fetchPublicationsAsync(grant.getId(), maxPublications)
                        .toCompletableFuture()
                        .get(config.getPerCallTimeoutSeconds(), TimeUnit.SECONDS);
                publicationsMap.put(grant.getId(), publications != null ? publications : new ArrayList<>());
                logger.debug("Fetched {} publications for grant: {}",
                        publications != null ? publications.size() : 0, grant.getId());
            } catch (TimeoutException e) {
                logger.warn("Timeout fetching publications for grant: {}", grant.getId());
                publicationsMap.put(grant.getId(), new ArrayList<>());
            } catch (Exception e) {
                logger.warn("Error fetching publications for grant: {} - continuing with empty list", grant.getId(), e);
                publicationsMap.put(grant.getId(), new ArrayList<>());
            }
        }
        return publicationsMap;
    }
}
