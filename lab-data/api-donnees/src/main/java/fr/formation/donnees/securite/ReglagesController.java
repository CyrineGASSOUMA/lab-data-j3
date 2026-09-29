package fr.formation.donnees.securite;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Formation uniquement : réduire la limite de débit ou la durée des tokens pour les démonstrations. */
@RestController
@RequestMapping("/dev/reglages")
@Tag(name = "Réglages de démonstration (formation)")
@SecurityRequirements
public class ReglagesController {

    private final ReglagesDemo reglages;
    private final LimiteurDebit limiteur;

    public ReglagesController(ReglagesDemo reglages, LimiteurDebit limiteur) {
        this.reglages = reglages;
        this.limiteur = limiteur;
    }

    @GetMapping
    @Operation(summary = "Réglages actuels")
    public ReglagesDemo.Etat etat() {
        return reglages.etat();
    }

    @PostMapping
    @Operation(summary = "Modifier : requêtes par fenêtre, taille de la fenêtre (s), durée des tokens (s)")
    public ReglagesDemo.Etat modifier(@RequestParam(required = false) Integer requetesParFenetre,
                                      @RequestParam(required = false) Integer fenetreSecondes,
                                      @RequestParam(required = false) Integer dureeTokenSecondes) {
        limiteur.reinitialiser();
        return reglages.modifier(requetesParFenetre, fenetreSecondes, dureeTokenSecondes);
    }

    @DeleteMapping
    @Operation(summary = "Revenir aux valeurs par défaut (30 requêtes / 10 s, tokens de 300 s)")
    public ReglagesDemo.Etat reinitialiser() {
        limiteur.reinitialiser();
        return reglages.reinitialiser();
    }
}
