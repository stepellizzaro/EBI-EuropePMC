package com.example.epmc.controllers;

import com.example.epmc.models.GrantPortfolio;
import com.example.epmc.models.PortfolioRequest;
import com.example.epmc.services.interfaces.PortfolioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {
    private static final Logger logger = LoggerFactory.getLogger(PortfolioController.class);

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/institution-portfolio")
    public CompletableFuture<ResponseEntity<GrantPortfolio>> getInstitutionPortfolio(
            @RequestParam String institution,
            @RequestParam(defaultValue = "10") int maxGrants,
            @RequestParam(defaultValue = "5") int maxPublications) {
        
        logger.info("Received portfolio request - institution: {}, maxGrants: {}, maxPublications: {}", 
                institution, maxGrants, maxPublications);

        PortfolioRequest request = new PortfolioRequest();
        request.setInstitution(institution);
        request.setMaxGrants(maxGrants);
        request.setMaxPublications(maxPublications);

        return portfolioService.getPortfolioAsync(request)
                .thenApply(ResponseEntity::ok)
                .exceptionally(throwable -> {
                    logger.error("Error processing portfolio request", throwable);
                    throw (RuntimeException) throwable;
                });
    }
}
