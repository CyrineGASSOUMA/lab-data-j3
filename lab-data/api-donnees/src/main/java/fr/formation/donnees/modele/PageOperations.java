package fr.formation.donnees.modele;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Une page d'opérations (pagination par curseur)")
public record PageOperations(
        List<Operation> donnees,
        @Schema(description = "À renvoyer dans le paramètre curseur pour la page suivante ; null à la fin",
                example = "MTAw", nullable = true) String curseurSuivant,
        @Schema(description = "Reste-t-il des opérations ?", example = "true") boolean aSuite) {
}
