package fr.formation.donnees.securite;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/** Limite de débit simple : N requêtes par fenêtre fixe de X secondes, par client. */
@Component
public class LimiteurDebit {

    public record Decision(boolean autorise, int limite, int restant, long secondesAvantReset) {
    }

    private record Fenetre(long debutMs, int compteur) {
    }

    private final Map<String, Fenetre> fenetres = new HashMap<>();
    private final ReglagesDemo reglages;

    public LimiteurDebit(ReglagesDemo reglages) {
        this.reglages = reglages;
    }

    public synchronized Decision consommer(String client) {
        long maintenant = System.currentTimeMillis();
        long dureeMs = reglages.fenetreSecondes() * 1000L;
        int limite = reglages.requetesParFenetre();
        Fenetre fenetre = fenetres.get(client);
        if (fenetre == null || maintenant - fenetre.debutMs() >= dureeMs) {
            fenetre = new Fenetre(maintenant, 0);
        }
        long secondesAvantReset = Math.max(1, (fenetre.debutMs() + dureeMs - maintenant + 999) / 1000);
        if (fenetre.compteur() >= limite) {
            fenetres.put(client, fenetre);
            return new Decision(false, limite, 0, secondesAvantReset);
        }
        Fenetre suivante = new Fenetre(fenetre.debutMs(), fenetre.compteur() + 1);
        fenetres.put(client, suivante);
        return new Decision(true, limite, limite - suivante.compteur(), secondesAvantReset);
    }

    public synchronized void reinitialiser() {
        fenetres.clear();
    }
}
