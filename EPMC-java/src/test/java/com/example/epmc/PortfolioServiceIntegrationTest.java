package com.example.epmc;

import com.example.epmc.config.PortfolioConfiguration;
import com.example.epmc.models.PublicationStatus;
import com.example.epmc.services.*;
import com.example.epmc.services.interfaces.ArticlesApiClient;
import com.example.epmc.services.interfaces.GristApiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Portfolio Service Integration Tests")
class PortfolioServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ============ Happy Path Test ============

    @Test
    @DisplayName("Should fetch portfolio with grants and publications")
    void shouldFetchPortfolioSuccessfully() throws Exception {
        long startTime = System.currentTimeMillis();

        MvcResult pending = mockMvc.perform(get("/api/portfolio/institution-portfolio")
                .param("institution", "Cambridge")
                .param("maxGrants", "5")
                .param("maxPublications", "3"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(pending))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.institution.name").value("Cambridge"))
                .andExpect(jsonPath("$.grants").isArray())
                .andExpect(jsonPath("$.grants[0].grant.id").exists())
                .andExpect(jsonPath("$.grants[0].grant.title").exists())
                .andExpect(jsonPath("$.grants[0].publications").isArray())
                .andExpect(jsonPath("$.summary.totalGrants").isNumber())
                .andExpect(jsonPath("$.summary.totalPublications").isNumber());

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        assert duration < 30000 : "Request took " + duration + "ms, expected < 30s";
    }

    // ============ Empty Results Test ============

    @Test
    @DisplayName("Should return empty portfolio when no grants found")
    void shouldHandleEmptyResults() throws Exception {
        MvcResult pending = mockMvc.perform(get("/api/portfolio/institution-portfolio")
                .param("institution", "NonExistentUniversity")
                .param("maxGrants", "5")
                .param("maxPublications", "3"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(pending))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.institution.name").value("NonExistentUniversity"))
                .andExpect(jsonPath("$.grants").isArray())
                .andExpect(jsonPath("$.grants", hasSize(0)))
                .andExpect(jsonPath("$.summary.totalGrants").value(0));
    }

    // ============ Validation Error Tests ============

    @Test
    @DisplayName("Should return 400 for empty institution name")
    void shouldReturn400ForEmptyInstitution() throws Exception {
        mockMvc.perform(get("/api/portfolio/institution-portfolio")
                .param("institution", "")
                .param("maxGrants", "5")
                .param("maxPublications", "3"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Should return 400 when maxGrants exceeds 10")
    void shouldReturn400ForExceededGrants() throws Exception {
        mockMvc.perform(get("/api/portfolio/institution-portfolio")
                .param("institution", "Cambridge")
                .param("maxGrants", "11")
                .param("maxPublications", "3"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Should return 400 when maxPublications exceeds 5")
    void shouldReturn400ForExceededPublications() throws Exception {
        mockMvc.perform(get("/api/portfolio/institution-portfolio")
                .param("institution", "Cambridge")
                .param("maxGrants", "5")
                .param("maxPublications", "6"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    // ============ Default Parameters Test ============

    @Test
    @DisplayName("Should use default limits when not specified")
    void shouldUseDefaultLimits() throws Exception {
        MvcResult pending = mockMvc.perform(get("/api/portfolio/institution-portfolio")
                .param("institution", "Cambridge"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(pending))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.institution.name").value("Cambridge"))
                .andExpect(jsonPath("$.grants").isArray());
    }

    // ============ Summary Calculation Test ============

    @Test
    @DisplayName("Should calculate correct summary metrics")
    void shouldCalculateSummaryCorrectly() throws Exception {
        MvcResult pending = mockMvc.perform(get("/api/portfolio/institution-portfolio")
                .param("institution", "Cambridge")
                .param("maxGrants", "5")
                .param("maxPublications", "3"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(pending))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.totalGrants").isNumber())
                .andExpect(jsonPath("$.summary.grantsWithPublications").isNumber())
                .andExpect(jsonPath("$.summary.totalPublications").isNumber())
                .andExpect(jsonPath("$.summary.funderCounts").isMap())
                .andExpect(jsonPath("$.summary.publicationStatusCounts").isMap());
    }

    // ============ Response Structure Test ============

    @Test
    @DisplayName("Should include all required fields in response")
    void shouldIncludeAllRequiredFields() throws Exception {
        MvcResult pending = mockMvc.perform(get("/api/portfolio/institution-portfolio")
                .param("institution", "Cambridge")
                .param("maxGrants", "5")
                .param("maxPublications", "3"))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk())
                .andReturn();

        mockMvc.perform(asyncDispatch(pending))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.institution.name").value("Cambridge"))
                .andExpect(jsonPath("$.grants").isArray())
                .andExpect(jsonPath("$.grants[0].grant.id").exists())
                .andExpect(jsonPath("$.grants[0].grant.title").exists())
                .andExpect(jsonPath("$.grants[0].publications").isArray())
                .andExpect(jsonPath("$.summary.totalGrants").isNumber())
                .andExpect(jsonPath("$.summary.totalPublications").isNumber());


    }

    // ============ Grant Status Test ============

    @Test
    @DisplayName("Should include publication status in each grant")
    void shouldIncludePublicationStatus() throws Exception {
        MvcResult pending = mockMvc.perform(get("/api/portfolio/institution-portfolio")
                .param("institution", "Cambridge")
                .param("maxGrants", "1")
                .param("maxPublications", "3"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(pending))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grants[0].grant.publicationStatus").exists())
                .andExpect(jsonPath("$.grants[0].grant.publicationStatus",
                        anyOf(is("FOUND"), is("NOT_FOUND"), is("ERROR"), is("TIMEOUT"))));
    }
}
