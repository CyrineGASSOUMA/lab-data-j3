package fr.formation.donnees.securite;

import org.springframework.stereotype.Component;

/** Réglages modifiables pendant la formation, via /dev/reglages. */
@Component
public class ReglagesDemo {

    public static final int REQUETES_DEFAUT = 30;
    public static final int FENETRE_DEFAUT = 10;
    public static final int DUREE_TOKEN_DEFAUT = 300;

    private volatile int requetesParFenetre = REQUETES_DEFAUT;
    private volatile int fenetreSecondes = FENETRE_DEFAUT;
    private volatile int dureeTokenSecondes = DUREE_TOKEN_DEFAUT;

    public record Etat(int requetesParFenetre, int fenetreSecondes, int dureeTokenSecondes) {
    }

    public synchronized Etat modifier(Integer requetes, Integer fenetre, Integer dureeToken) {
        if (requetes != null && requetes > 0) {
            requetesParFenetre = requetes;
        }
        if (fenetre != null && fenetre > 0) {
            fenetreSecondes = fenetre;
        }
        if (dureeToken != null && dureeToken > 0) {
            dureeTokenSecondes = dureeToken;
        }
        return etat();
    }

    public synchronized Etat reinitialiser() {
        requetesParFenetre = REQUETES_DEFAUT;
        fenetreSecondes = FENETRE_DEFAUT;
        dureeTokenSecondes = DUREE_TOKEN_DEFAUT;
        return etat();
    }

    public Etat etat() {
        return new Etat(requetesParFenetre, fenetreSecondes, dureeTokenSecondes);
    }

    public int requetesParFenetre() {
        return requetesParFenetre;
    }

    public int fenetreSecondes() {
        return fenetreSecondes;
    }

    public int dureeTokenSecondes() {
        return dureeTokenSecondes;
    }
}
