package fr.formation.donnees.modele;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Une page de clients (pagination par numéro de page)")
public record PageClients(List<Client> donnees, Pagination pagination, Liens liens) {

    @Schema(description = "Position dans le résultat")
    public record Pagination(
            @Schema(description = "Numéro de page, commence à 0", example = "0") int page,
            @Schema(example = "20") int taille,
            @Schema(example = "57") long totalElements,
            @Schema(example = "3") int totalPages) {
    }

    @Schema(description = "Liens de navigation, null quand il n'y a pas de page")
    public record Liens(
            @Schema(example = "/v1/clients?page=1&taille=20", nullable = true) String suivant,
            @Schema(nullable = true) String precedent) {
    }
}
