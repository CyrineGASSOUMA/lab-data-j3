package fr.formation.donnees.securite;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Applique la limite de débit aux appels authentifiés sur /v1/**, et publie les headers X-RateLimit-*. */
public class LimiteDebitFilter extends OncePerRequestFilter {

    private final LimiteurDebit limiteur;

    public LimiteDebitFilter(LimiteurDebit limiteur) {
        this.limiteur = limiteur;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/v1/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication authentification = SecurityContextHolder.getContext().getAuthentication();
        if (authentification == null || authentification instanceof AnonymousAuthenticationToken
                || !authentification.isAuthenticated()) {
            chain.doFilter(request, response);    // sera refusé en 401 plus loin
            return;
        }
        LimiteurDebit.Decision decision = limiteur.consommer(authentification.getName());
        response.setHeader("X-RateLimit-Limit", String.valueOf(decision.limite()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(decision.restant()));
        if (!decision.autorise()) {
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(decision.secondesAvantReset()));
            response.setContentType("application/problem+json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"type\":\"about:blank\",\"title\":\"Trop de requêtes\",\"status\":429,"
                    + "\"detail\":\"Limite de " + decision.limite() + " requêtes atteinte. Réessayez dans "
                    + decision.secondesAvantReset() + " s.\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
