package fr.formation.donnees.modele;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Opération sur un compte")
public record Operation(
        @Schema(example = "OP-000123") String id,
        @Schema(description = "Numéro du compte concerné", example = "FR76-0042-01") String numeroCompte,
        @Schema(example = "2026-09-12") LocalDate dateOperation,
        @Schema(description = "Montant positif, 2 décimales", example = "84.90") BigDecimal montant,
        @Schema(description = "DEBIT ou CREDIT", example = "DEBIT") String sens,
        @Schema(example = "Carte - Supermarché") String libelle,
        @Schema(description = "Facultatif : peut être null", example = "ALIMENTATION", nullable = true) String categorie) {
}
