package fr.formation.donnees.modele;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Adresse postale (objet imbriqué dans Client)")
public record Adresse(
        @Schema(example = "12 rue des Lilas") String rue,
        @Schema(example = "75011") String codePostal,
        @Schema(example = "Paris") String ville,
        @Schema(description = "Code pays ISO 3166-1 alpha-2", example = "FR") String pays) {
}
