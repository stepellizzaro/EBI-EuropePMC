package com.example.epmc.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSummary {
    private int totalGrants;
    private int grantsWithPublications;
    private int totalPublications;
    private Map<String, Integer> funderCounts = new HashMap<>();
    private Map<PublicationStatus, Integer> publicationStatusCounts = new HashMap<>();
}
