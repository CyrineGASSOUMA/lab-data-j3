package fr.formation.donnees.securite;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Serveur d'autorisation minimal : flux OAuth2 client credentials (RFC 6749, section 4.4).
 * Authentification du client par header Basic (recommandé) ou par client_id / client_secret dans le formulaire.
 */
@RestController
@Tag(name = "Authentification")
public class TokenController {

    private final JwtEncoder encodeur;
    private final ReglagesDemo reglages;

    public TokenController(SecretKey cleSignature, ReglagesDemo reglages) {
        this.encodeur = new NimbusJwtEncoder(new ImmutableSecret<>(cleSignature));
        this.reglages = reglages;
    }

    @PostMapping(value = "/oauth2/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @SecurityRequirements
    @Operation(summary = "Obtenir un token (client credentials)",
            description = "Formulaire : grant_type=client_credentials, scope facultatif. "
                    + "Client : header Authorization: Basic base64(client_id:client_secret).")
    public ResponseEntity<Map<String, Object>> token(
            @RequestParam(name = "grant_type", required = false) String grantType,
            @RequestParam(name = "scope", required = false) String scope,
            @RequestParam(name = "client_id", required = false) String clientIdFormulaire,
            @RequestParam(name = "client_secret", required = false) String secretFormulaire,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorization) {

        String[] identifiants = lireBasic(authorization);
        String clientId = identifiants != null ? identifiants[0] : clientIdFormulaire;
        String secretRecu = identifiants != null ? identifiants[1] : secretFormulaire;

        ClientsOAuth.ClientOAuth client = clientId == null ? null : ClientsOAuth.CLIENTS.get(clientId);
        if (client == null || !client.secret().equals(secretRecu)) {
            return erreur(HttpStatus.UNAUTHORIZED, "invalid_client", "Identifiants du client incorrects");
        }
        if (!"client_credentials".equals(grantType)) {
            return erreur(HttpStatus.BAD_REQUEST, "unsupported_grant_type", "Seul client_credentials est accepté");
        }
        Set<String> scopes = new TreeSet<>(client.scopes());
        if (scope != null && !scope.isBlank()) {
            Set<String> demandes = new TreeSet<>(Arrays.asList(scope.trim().split("\\s+")));
            if (!client.scopes().containsAll(demandes)) {
                return erreur(HttpStatus.BAD_REQUEST, "invalid_scope", "Scope non autorisé pour ce client");
            }
            scopes = demandes;
        }

        int duree = reglages.dureeTokenSecondes();
        Instant maintenant = Instant.now();
        String scopeAccorde = String.join(" ", scopes);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("api-donnees-formation")
                .subject(clientId)
                .issuedAt(maintenant)
                .expiresAt(maintenant.plus(duree, ChronoUnit.SECONDS))
                .claim("scope", scopeAccorde)
                .build();
        String token = encodeur.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();

        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("access_token", token);
        corps.put("token_type", "Bearer");
        corps.put("expires_in", duree);
        corps.put("scope", scopeAccorde);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(corps);
    }

    private static String[] lireBasic(String authorization) {
        if (authorization == null || !authorization.regionMatches(true, 0, "Basic ", 0, 6)) {
            return null;
        }
        try {
            String decode = new String(Base64.getDecoder().decode(authorization.substring(6).trim()),
                    StandardCharsets.UTF_8);
            int separateur = decode.indexOf(':');
            return separateur < 0 ? null : new String[]{decode.substring(0, separateur), decode.substring(separateur + 1)};
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Erreur au format OAuth2 (RFC 6749, section 5.2), pas Problem Details. */
    private static ResponseEntity<Map<String, Object>> erreur(HttpStatus statut, String code, String description) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("error", code);
        corps.put("error_description", description);
        ResponseEntity.BodyBuilder reponse = ResponseEntity.status(statut).cacheControl(CacheControl.noStore());
        if (statut == HttpStatus.UNAUTHORIZED) {
            reponse.header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"api-donnees\"");
        }
        return reponse.body(corps);
    }
}
