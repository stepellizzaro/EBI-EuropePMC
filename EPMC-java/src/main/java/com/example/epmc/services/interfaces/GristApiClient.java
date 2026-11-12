package com.example.epmc.services.interfaces;

import com.example.epmc.models.Grant;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface GristApiClient {
    CompletableFuture<List<Grant>> fetchGrantsAsync(String institution, int maxGrants);
}
