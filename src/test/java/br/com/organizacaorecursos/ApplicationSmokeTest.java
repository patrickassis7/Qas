package br.com.organizacaorecursos;

import br.com.organizacaorecursos.integration.PostgresIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApplicationSmokeTest extends PostgresIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextStartsWithPostgresTestcontainer() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void openApiDocumentationIsAvailable() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
