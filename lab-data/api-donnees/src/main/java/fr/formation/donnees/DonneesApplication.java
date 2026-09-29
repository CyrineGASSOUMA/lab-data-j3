package fr.formation.donnees;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(title = "API Référentiel Clients", version = "1.0",
                description = """
                        Source de données clients, comptes et opérations, destinée aux outils d'intégration (ETL).

                        **Authentification** (au choix) : clé d'API dans le header `X-API-Key`, ou token OAuth2 \
                        obtenu par le flux *client credentials* sur `POST /oauth2/token`.

                        **Pagination** : `/v1/clients` par numéro de page (`page`, `taille`, lien `suivant`) ; \
                        `/v1/operations` par curseur (`curseur`, `limite`, `curseurSuivant`).

                        **Extraction incrémentale** : paramètre `modifieDepuis` (ISO-8601 UTC).

                        **Limite de débit** : 30 requêtes par fenêtre de 10 secondes et par client. \
                        Au-delà : `429 Too Many Requests` avec le header `Retry-After` (secondes). \
                        Headers informatifs : `X-RateLimit-Limit`, `X-RateLimit-Remaining`.

                        **Erreurs** : format Problem Details (RFC 9457)."""),
        servers = @Server(url = "/"),
        security = {@SecurityRequirement(name = "cleApi"), @SecurityRequirement(name = "oauth2")})
@SecurityScheme(name = "cleApi", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER,
        paramName = "X-API-Key", description = "Clé d'API de démonstration : cle-demo-etl-2026")
@SecurityScheme(name = "oauth2", type = SecuritySchemeType.OAUTH2,
        description = "Clients de démonstration : connecteur-etl / secret-etl-2026 (tous les scopes), "
                + "rapport-bi / secret-bi-2026 (clients:lire uniquement)",
        flows = @OAuthFlows(clientCredentials = @OAuthFlow(tokenUrl = "/oauth2/token", scopes = {
                @OAuthScope(name = "clients:lire", description = "Lire les clients et leurs comptes"),
                @OAuthScope(name = "operations:lire", description = "Lire les opérations")})))
public class DonneesApplication {

    public static void main(String[] args) {
        SpringApplication.run(DonneesApplication.class, args);
    }
}
