package fr.formation.donnees.securite;

import java.util.Map;
import java.util.Set;

/** Les applications autorisées à obtenir un token (formation : en dur). */
public final class ClientsOAuth {

    public record ClientOAuth(String secret, Set<String> scopes) {
    }

    public static final Map<String, ClientOAuth> CLIENTS = Map.of(
            "connecteur-etl", new ClientOAuth("secret-etl-2026", Set.of("clients:lire", "operations:lire")),
            "rapport-bi", new ClientOAuth("secret-bi-2026", Set.of("clients:lire")));

    private ClientsOAuth() {
    }
}
