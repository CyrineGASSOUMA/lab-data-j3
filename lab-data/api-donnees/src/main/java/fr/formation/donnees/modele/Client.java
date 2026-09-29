package fr.formation.donnees.modele;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Client, avec son adresse et ses comptes")
public record Client(
        @Schema(description = "Identifiant technique", example = "42") Long id,
        @Schema(description = "Référence métier unique", example = "CLI-000042") String reference,
        @Schema(example = "Lefèvre") String nom,
        @Schema(example = "Émilie") String prenom,
        @Schema(example = "emilie.lefevre42@exemple.fr") String email,
        @Schema(description = "Facultatif : peut être null", example = "+33 6 12 34 56 78", nullable = true) String telephone,
        @Schema(example = "ACTIF") StatutClient statut,
        Adresse adresse,
        List<Compte> comptes,
        @Schema(description = "Création (ISO-8601 UTC)", example = "2024-01-15T09:30:00Z") java.time.Instant creeLe,
        @Schema(description = "Dernière modification (ISO-8601 UTC) : sert à l'extraction incrémentale",
                example = "2026-09-20T08:00:00Z") Instant modifieLe) {
}
