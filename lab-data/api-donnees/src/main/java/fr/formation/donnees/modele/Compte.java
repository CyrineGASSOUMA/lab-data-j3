package fr.formation.donnees.modele;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Compte bancaire (tableau imbriqué dans Client : relation 1 client → N comptes)")
public record Compte(
        @Schema(description = "Identifiant unique du compte", example = "FR76-0042-01") String numero,
        @Schema(example = "COURANT") TypeCompte type,
        @Schema(description = "Solde, 2 décimales", example = "1520.75") BigDecimal solde,
        @Schema(description = "Code devise ISO 4217", example = "EUR") String devise,
        @Schema(description = "Date d'ouverture (AAAA-MM-JJ)", example = "2019-03-14") LocalDate ouvertLe) {
}
