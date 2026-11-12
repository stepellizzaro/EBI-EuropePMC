package com.example.epmc.services.interfaces;

import com.example.epmc.models.Publication;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ArticlesApiClient {
    CompletableFuture<List<Publication>> fetchPublicationsAsync(String grantId, int maxPublications);
}
