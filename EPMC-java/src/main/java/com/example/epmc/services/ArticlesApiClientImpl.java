package com.example.epmc.services;

import com.example.epmc.models.Publication;
import com.example.epmc.models.PublicationStatus;
import com.example.epmc.services.interfaces.ArticlesApiClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class ArticlesApiClientImpl implements ArticlesApiClient {

    private static final Map<String, List<Publication>> PUBLICATIONS_BY_GRANT = Map.ofEntries(
            Map.entry("G001", List.of(
                    new Publication("P001", "Deep Learning for Scientific Discovery", List.of("Smith, J.", "Jones, A."),
                            "Nature Machine Intelligence", "https://doi.org/10.1038/s42256-024-12345", "2024-03"),
                    new Publication("P002", "Transformers in Computational Biology", List.of("Brown, R.", "Davis, L."),
                            "Science Advances", "https://doi.org/10.1126/sciadv.abcxyz", "2024-02"),
                    new Publication("P003", "Graph Neural Networks for Drug Design", List.of("Wilson, K."),
                            "Journal of Chemical Information and Modeling", "https://doi.org/10.1021/acs.jcim.24", "2024-01")
            )),
            Map.entry("G002", List.of(
                    new Publication("P004", "Carbon Sequestration in Forests", List.of("Green, P.", "White, M."),
                            "Environmental Science & Technology", "https://doi.org/10.1021/acs.est.24", "2024-03"),
                    new Publication("P005", "Climate Adaptation Strategies", List.of("Black, S."),
                            "Climatic Change", "https://doi.org/10.1007/s10584-024-12345", "2024-01")
            )),
            Map.entry("G003", List.of(
                    new Publication("P006", "Quantum Error Correction", List.of("Hall, T.", "Taylor, N."),
                            "Physical Review Letters", "https://doi.org/10.1103/PhysRevLett.132.12345", "2024-02"),
                    new Publication("P007", "Quantum Algorithms", List.of("Clark, E."),
                            "Quantum Information Processing", "https://doi.org/10.1007/s11128-024-12345", "2024-03")
            )),
            Map.entry("G006", List.of(
                    new Publication("P008", "Protein Folding Prediction", List.of("Martin, J.", "Garcia, C."),
                            "Nature Methods", "https://doi.org/10.1038/s41592-024-12345", "2024-03"),
                    new Publication("P009", "AI-Assisted Drug Discovery", List.of("Lopez, D."),
                            "Drug Discovery Today", "https://doi.org/10.1016/j.drudis.2024.01.001", "2024-01")
            )),
            Map.entry("G007", List.of(
                    new Publication("P010", "Solar Panel Efficiency", List.of("Anderson, B.", "Lee, H."),
                            "Progress in Photovoltaics", "https://doi.org/10.1002/pip.12345", "2024-02")
            ))
    );

    @Override
    public CompletableFuture<List<Publication>> fetchPublicationsAsync(String grantId, int maxPublications) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(50); // Simulate network latency
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            List<Publication> publications = PUBLICATIONS_BY_GRANT.getOrDefault(grantId, new ArrayList<>());
            return publications.stream().limit(maxPublications).toList();
        });
    }
}
