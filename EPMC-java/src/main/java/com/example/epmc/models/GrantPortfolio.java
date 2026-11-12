package com.example.epmc.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrantPortfolio {
    private Institution institution;
    private List<GrantWithPublications> grants;
    private PortfolioSummary summary;
}
