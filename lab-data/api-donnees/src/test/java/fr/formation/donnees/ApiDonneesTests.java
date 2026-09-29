package fr.formation.donnees;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.formation.donnees.securite.LimiteurDebit;
import fr.formation.donnees.securite.ReglagesDemo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiDonneesTests {

    private static final String CLE = "cle-demo-etl-2026";

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper json;

    @Autowired
    ReglagesDemo reglages;

    @Autowired
    LimiteurDebit limiteur;

    @AfterEach
    void remiseAZero() {
        reglages.reinitialiser();
        limiteur.reinitialiser();
    }

    private static String basic(String id, String secret) {
        return "Basic " + Base64.getEncoder().encodeToString((id + ":" + secret).getBytes(StandardCharsets.UTF_8));
    }

    private String token(String id, String secret) throws Exception {
        String reponse = mvc.perform(post("/oauth2/token").header("Authorization", basic(id, secret))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED).param("grant_type", "client_credentials"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + json.readTree(reponse).get("access_token").asText();
    }

    @Test
    void swaggerPublic() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }

    @Test
    void sansAuthentification401() throws Exception {
        mvc.perform(get("/v1/clients")).andExpect(status().isUnauthorized());
    }

    @Test
    void mauvaiseCle401() throws Exception {
        mvc.perform(get("/v1/clients").header("X-API-Key", "fausse")).andExpect(status().isUnauthorized());
    }

    @Test
    void cleApiPremierePage() throws Exception {
        mvc.perform(get("/v1/clients").header("X-API-Key", CLE))
                .andExpect(status().isOk())
                .andExpect(header().string("X-RateLimit-Limit", "30"))
                .andExpect(header().string("Link", "</v1/clients?page=1&taille=20>; rel=\"next\""))
                .andExpect(jsonPath("$.donnees.length()").value(20))
                .andExpect(jsonPath("$.pagination.totalElements").value(57))
                .andExpect(jsonPath("$.pagination.totalPages").value(3))
                .andExpect(jsonPath("$.liens.suivant").value("/v1/clients?page=1&taille=20"));
    }

    @Test
    void dernierePageSansSuivant() throws Exception {
        mvc.perform(get("/v1/clients").param("page", "2").header("X-API-Key", CLE))
                .andExpect(jsonPath("$.donnees.length()").value(17))
                .andExpect(jsonPath("$.liens.suivant").value(nullValue()));
    }

    @Test
    void extractionIncrementale() throws Exception {
        mvc.perform(get("/v1/clients").param("modifieDepuis", "2026-09-20T00:00:00Z").header("X-API-Key", CLE))
                .andExpect(jsonPath("$.pagination.totalElements").value(8));
    }

    @Test
    void filtreStatut() throws Exception {
        mvc.perform(get("/v1/clients").param("statut", "INACTIF").header("X-API-Key", CLE))
                .andExpect(jsonPath("$.pagination.totalElements").value(5));
    }

    @Test
    void parametresInvalides400() throws Exception {
        mvc.perform(get("/v1/clients").param("taille", "500").header("X-API-Key", CLE))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/v1/clients").param("modifieDepuis", "20/09/2026").header("X-API-Key", CLE))
                .andExpect(status().isBadRequest());
    }

    @Test
    void clientInconnu404() throws Exception {
        mvc.perform(get("/v1/clients/999").header("X-API-Key", CLE)).andExpect(status().isNotFound());
    }

    @Test
    void clientCredentialsPuisAppel() throws Exception {
        mvc.perform(get("/v1/operations").header("Authorization", token("connecteur-etl", "secret-etl-2026")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.donnees.length()").value(100))
                .andExpect(jsonPath("$.aSuite").value(true));
    }

    @Test
    void scopeInsuffisant403() throws Exception {
        String bi = token("rapport-bi", "secret-bi-2026");
        mvc.perform(get("/v1/clients").header("Authorization", bi)).andExpect(status().isOk());
        mvc.perform(get("/v1/operations").header("Authorization", bi)).andExpect(status().isForbidden());
    }

    @Test
    void mauvaisSecret401EtMauvaisGrant400() throws Exception {
        mvc.perform(post("/oauth2/token").header("Authorization", basic("connecteur-etl", "faux"))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED).param("grant_type", "client_credentials"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("invalid_client"));
        mvc.perform(post("/oauth2/token").header("Authorization", basic("connecteur-etl", "secret-etl-2026"))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED).param("grant_type", "password"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("unsupported_grant_type"));
    }

    @Test
    void limiteDeDebit429() throws Exception {
        mvc.perform(post("/dev/reglages").param("requetesParFenetre", "2")).andExpect(status().isOk());
        mvc.perform(get("/v1/clients").header("X-API-Key", CLE)).andExpect(status().isOk());
        mvc.perform(get("/v1/clients").header("X-API-Key", CLE))
                .andExpect(status().isOk())
                .andExpect(header().string("X-RateLimit-Remaining", "0"));
        mvc.perform(get("/v1/clients").header("X-API-Key", CLE))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }

    @Test
    void paginationParCurseurJusquAuBout() throws Exception {
        String curseur = null;
        int total = 0;
        for (int i = 0; i < 10; i++) {
            var requete = get("/v1/operations").param("limite", "100").header("X-API-Key", CLE);
            if (curseur != null) {
                requete = requete.param("curseur", curseur);
            }
            String corps = mvc.perform(requete).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            var page = json.readTree(corps);
            total += page.get("donnees").size();
            if (!page.get("aSuite").asBoolean()) {
                break;
            }
            curseur = page.get("curseurSuivant").asText();
        }
        org.junit.jupiter.api.Assertions.assertEquals(342, total);
    }
}
