package fr.formation.donnees.securite;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Configuration
public class SecuriteConfig {

    @Value("${donnees.cle-api}")
    private String cleApi;

    @Value("${donnees.jwt.secret}")
    private String secret;

    @Bean
    SecurityFilterChain securite(HttpSecurity http, LimiteurDebit limiteur) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/oauth2/token", "/dev/**", "/swagger-ui.html", "/swagger-ui/**",
                                "/v3/api-docs/**", "/error").permitAll()
                        .requestMatchers("/v1/operations/**", "/v1/operations").hasAuthority("SCOPE_operations:lire")
                        .requestMatchers("/v1/clients/**", "/v1/clients").hasAuthority("SCOPE_clients:lire")
                        .anyRequest().authenticated())
                .addFilterBefore(new CleApiFilter(cleApi), BearerTokenAuthenticationFilter.class)
                .addFilterAfter(new LimiteDebitFilter(limiteur), BearerTokenAuthenticationFilter.class)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    SecretKey cleSignature() {
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey cleSignature) {
        NimbusJwtDecoder decodeur = NimbusJwtDecoder.withSecretKey(cleSignature).macAlgorithm(MacAlgorithm.HS256).build();
        decodeur.setJwtValidator(new JwtTimestampValidator(Duration.ZERO));   // expiration stricte, sans tolérance
        return decodeur;
    }
}
