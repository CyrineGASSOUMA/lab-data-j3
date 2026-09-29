package fr.formation.donnees.service;

import fr.formation.donnees.donnees.JeuDeDonnees;
import fr.formation.donnees.exception.ClientIntrouvableException;
import fr.formation.donnees.exception.ParametreInvalideException;
import fr.formation.donnees.modele.Client;
import fr.formation.donnees.modele.PageClients;
import fr.formation.donnees.modele.StatutClient;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class ClientService {

    public static final int TAILLE_MAX = 100;

    private final JeuDeDonnees donnees;

    public ClientService(JeuDeDonnees donnees) {
        this.donnees = donnees;
    }

    public PageClients lister(int page, int taille, String modifieDepuis, StatutClient statut) {
        if (page < 0) {
            throw new ParametreInvalideException("page doit être supérieur ou égal à 0");
        }
        if (taille < 1 || taille > TAILLE_MAX) {
            throw new ParametreInvalideException("taille doit être compris entre 1 et " + TAILLE_MAX);
        }
        Instant depuis = lireDate(modifieDepuis);

        List<Client> filtres = donnees.clients().stream()
                .filter(c -> depuis == null || !c.modifieLe().isBefore(depuis))
                .filter(c -> statut == null || c.statut() == statut)
                .toList();

        int total = filtres.size();
        int totalPages = (int) Math.ceil(total / (double) taille);
        int debut = Math.min(page * taille, total);
        int fin = Math.min(debut + taille, total);

        String suivant = fin < total ? lien(page + 1, taille, modifieDepuis, statut) : null;
        String precedent = page > 0 && debut <= total ? lien(page - 1, taille, modifieDepuis, statut) : null;
        return new PageClients(filtres.subList(debut, fin),
                new PageClients.Pagination(page, taille, total, totalPages),
                new PageClients.Liens(suivant, precedent));
    }

    public Client lire(long id) {
        return donnees.clients().stream()
                .filter(c -> c.id() == id)
                .findFirst()
                .orElseThrow(() -> new ClientIntrouvableException(id));
    }

    private static String lien(int page, int taille, String modifieDepuis, StatutClient statut) {
        UriComponentsBuilder lien = UriComponentsBuilder.fromPath("/v1/clients")
                .queryParam("page", page)
                .queryParam("taille", taille);
        if (modifieDepuis != null && !modifieDepuis.isBlank()) {
            lien.queryParam("modifieDepuis", modifieDepuis);
        }
        if (statut != null) {
            lien.queryParam("statut", statut);
        }
        return lien.build().toUriString();
    }

    private static Instant lireDate(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(valeur);
        } catch (DateTimeParseException e) {
            throw new ParametreInvalideException(
                    "modifieDepuis doit être au format ISO-8601 UTC, par exemple 2026-09-20T00:00:00Z");
        }
    }
}
