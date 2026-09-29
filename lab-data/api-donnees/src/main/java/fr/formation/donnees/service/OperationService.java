package fr.formation.donnees.service;

import fr.formation.donnees.donnees.JeuDeDonnees;
import fr.formation.donnees.exception.ParametreInvalideException;
import fr.formation.donnees.modele.Operation;
import fr.formation.donnees.modele.PageOperations;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/** Pagination par curseur : le client renvoie un jeton opaque au lieu d'un numéro de page. */
@Service
public class OperationService {

    public static final int LIMITE_MAX = 200;

    private final JeuDeDonnees donnees;

    public OperationService(JeuDeDonnees donnees) {
        this.donnees = donnees;
    }

    public PageOperations lister(String curseur, int limite) {
        if (limite < 1 || limite > LIMITE_MAX) {
            throw new ParametreInvalideException("limite doit être comprise entre 1 et " + LIMITE_MAX);
        }
        List<Operation> toutes = donnees.operations();
        int debut = decoder(curseur);
        if (debut > toutes.size()) {
            throw new ParametreInvalideException("curseur invalide");
        }
        int fin = Math.min(debut + limite, toutes.size());
        boolean aSuite = fin < toutes.size();
        return new PageOperations(toutes.subList(debut, fin), aSuite ? encoder(fin) : null, aSuite);
    }

    private static String encoder(int position) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(String.valueOf(position).getBytes(StandardCharsets.UTF_8));
    }

    private static int decoder(String curseur) {
        if (curseur == null || curseur.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(new String(Base64.getUrlDecoder().decode(curseur), StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            throw new ParametreInvalideException("curseur invalide : renvoyez la valeur de curseurSuivant telle quelle");
        }
    }
}
