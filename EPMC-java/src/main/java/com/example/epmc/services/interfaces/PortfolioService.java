package com.example.epmc.services.interfaces;

import com.example.epmc.models.GrantPortfolio;
import com.example.epmc.models.PortfolioRequest;

import java.util.concurrent.CompletableFuture;

public interface PortfolioService {
    CompletableFuture<GrantPortfolio> getPortfolioAsync(PortfolioRequest request);
}
