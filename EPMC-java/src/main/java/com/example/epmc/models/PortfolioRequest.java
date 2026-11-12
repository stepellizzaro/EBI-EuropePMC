package com.example.epmc.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioRequest {
    private String institution;
    private int maxGrants = 10;
    private int maxPublications = 5;
}
