package fr.formation.donnees.controller;

import fr.formation.donnees.modele.Client;
import fr.formation.donnees.modele.PageClients;
import fr.formation.donnees.modele.StatutClient;
import fr.formation.donnees.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/clients")
@Tag(name = "Clients", description = "Clients avec leur adresse et leurs comptes (scope clients:lire)")
public class ClientController {

    private final ClientService service;

    public ClientController(ClientService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lister les clients, page par page",
            description = "Suivre liens.suivant (ou le header Link rel=next) jusqu'à ce qu'il soit null.")
    @ApiResponse(responseCode = "200", description = "Une page de clients")
    @ApiResponse(responseCode = "400", description = "Paramètre invalide")
    @ApiResponse(responseCode = "401", description = "Authentification absente ou invalide")
    @ApiResponse(responseCode = "403", description = "Scope clients:lire manquant")
    @ApiResponse(responseCode = "429", description = "Limite de débit atteinte : attendre Retry-After secondes")
    public ResponseEntity<PageClients> lister(
            @Parameter(description = "Numéro de page, commence à 0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre de clients par page, de 1 à 100") @RequestParam(defaultValue = "20") int taille,
            @Parameter(description = "Seulement les clients modifiés depuis cette date (ISO-8601 UTC)",
                    example = "2026-09-20T00:00:00Z") @RequestParam(required = false) String modifieDepuis,
            @Parameter(description = "Filtrer par statut") @RequestParam(required = false) StatutClient statut) {
        PageClients resultat = service.lister(page, taille, modifieDepuis, statut);
        ResponseEntity.BodyBuilder reponse = ResponseEntity.ok();
        if (resultat.liens().suivant() != null) {
            reponse.header(HttpHeaders.LINK, "<" + resultat.liens().suivant() + ">; rel=\"next\"");
        }
        return reponse.body(resultat);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lire un client")
    @ApiResponse(responseCode = "200", description = "Le client")
    @ApiResponse(responseCode = "404", description = "Client inexistant")
    public Client lire(@PathVariable long id) {
        return service.lire(id);
    }
}
