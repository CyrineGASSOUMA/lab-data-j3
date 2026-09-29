package fr.formation.donnees.securite;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/** Authentification par clé d'API : header X-API-Key. La clé donne tous les scopes de lecture. */
public class CleApiFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-API-Key";

    private final String cleAttendue;

    public CleApiFilter(String cleAttendue) {
        this.cleAttendue = cleAttendue;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String cle = request.getHeader(HEADER);
        if (cle != null) {
            if (!cle.equals(cleAttendue)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/problem+json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"type\":\"about:blank\",\"title\":\"Clé d'API invalide\","
                        + "\"status\":401,\"detail\":\"La valeur du header X-API-Key n'est pas reconnue\"}");
                return;
            }
            SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                    "cle-api", null, List.of(new SimpleGrantedAuthority("SCOPE_clients:lire"),
                            new SimpleGrantedAuthority("SCOPE_operations:lire"))));
        }
        chain.doFilter(request, response);
    }
}
