package com.example.epmc.services;

import com.example.epmc.models.Grant;
import com.example.epmc.models.PublicationStatus;
import com.example.epmc.services.interfaces.GristApiClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class GristApiClientImpl implements GristApiClient {

    @Override
    public CompletableFuture<List<Grant>> fetchGrantsAsync(String institution, int maxGrants) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(100); // Simulate network latency
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return getGrantsForInstitution(institution, maxGrants);
        });
    }

    private List<Grant> getGrantsForInstitution(String institution, int maxGrants) {
        List<Grant> allGrants = new ArrayList<>();

        // Mock data for Cambridge
        if (institution.equalsIgnoreCase("Cambridge")) {
            allGrants.add(new Grant("G001", "AI Research Initiative", "UKRI", "Cambridge", 
                    "https://grants.example.com/G001", PublicationStatus.FOUND, null));
            allGrants.add(new Grant("G002", "Climate Change Studies", "EU Horizon", "Cambridge",
                    "https://grants.example.com/G002", PublicationStatus.FOUND, null));
            allGrants.add(new Grant("G003", "Quantum Computing", "DARPA", "Cambridge",
                    "https://grants.example.com/G003", PublicationStatus.FOUND, null));
            allGrants.add(new Grant("G004", "Bioinformatics", "NIH", "Cambridge",
                    "https://grants.example.com/G004", PublicationStatus.FOUND, null));
            allGrants.add(new Grant("G005", "Materials Science", "NSF", "Cambridge",
                    "https://grants.example.com/G005", PublicationStatus.FOUND, null));
        }
        // Mock data for Manchester
        else if (institution.equalsIgnoreCase("Manchester")) {
            allGrants.add(new Grant("G006", "Drug Discovery", "Welcome Trust", "Manchester",
                    "https://grants.example.com/G006", PublicationStatus.FOUND, null));
            allGrants.add(new Grant("G007", "Renewable Energy", "EU Horizon", "Manchester",
                    "https://grants.example.com/G007", PublicationStatus.FOUND, null));
            allGrants.add(new Grant("G008", "Social Sciences", "ESRC", "Manchester",
                    "https://grants.example.com/G008", PublicationStatus.FOUND, null));
        }
        // Default: no grants for other institutions
        else {
            return new ArrayList<>();
        }

        return allGrants.stream().limit(maxGrants).toList();
    }
}
