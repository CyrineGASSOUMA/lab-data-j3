package fr.formation.donnees.controller;

import fr.formation.donnees.modele.PageOperations;
import fr.formation.donnees.service.OperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/operations")
@Tag(name = "Opérations", description = "Opérations sur les comptes (scope operations:lire)")
public class OperationController {

    private final OperationService service;

    public OperationController(OperationService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lister les opérations par curseur",
            description = "Premier appel sans curseur, puis renvoyer curseurSuivant tant que aSuite vaut true.")
    @ApiResponse(responseCode = "200", description = "Une page d'opérations")
    @ApiResponse(responseCode = "403", description = "Scope operations:lire manquant")
    @ApiResponse(responseCode = "429", description = "Limite de débit atteinte")
    public PageOperations lister(
            @Parameter(description = "Valeur de curseurSuivant reçue à l'appel précédent")
            @RequestParam(required = false) String curseur,
            @Parameter(description = "Nombre d'opérations, de 1 à 200") @RequestParam(defaultValue = "100") int limite) {
        return service.lister(curseur, limite);
    }
}
