package fr.formation.donnees.donnees;

import fr.formation.donnees.modele.Adresse;
import fr.formation.donnees.modele.Client;
import fr.formation.donnees.modele.Compte;
import fr.formation.donnees.modele.Operation;
import fr.formation.donnees.modele.StatutClient;
import fr.formation.donnees.modele.TypeCompte;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Jeu de données déterministe : les mêmes données à chaque démarrage.
 * 57 clients, 114 comptes, 342 opérations. Le client n°i a été modifié le 1er août 2026 + i jours.
 */
@Component
public class JeuDeDonnees {

    public static final int NOMBRE_CLIENTS = 57;
    private static final Instant BASE = Instant.parse("2026-08-01T08:00:00Z");

    private static final String[] NOMS = {"Martin", "Bernard", "Lefèvre", "N'Diaye", "Da Silva", "Nguyen", "Dubois",
            "Moreau", "Laurent", "Garcia", "Roux", "Fournier", "Mercier", "Benali", "Girard", "Bonnet", "Faure",
            "Chevalier", "Lambert", "Rousseau"};
    private static final String[] PRENOMS = {"Émilie", "Karim", "Chloé", "Mamadou", "Inès", "Thomas", "Léa", "Hugo",
            "Aïcha", "Lucas", "Manon", "Yanis", "Camille", "Noé", "Sarah", "Louis", "Zoé", "Adam", "Jade", "Théo"};
    private static final String[][] VILLES = {{"Paris", "75011"}, {"Lyon", "69003"}, {"Marseille", "13008"},
            {"Lille", "59000"}, {"Nantes", "44000"}, {"Bordeaux", "33000"}, {"Toulouse", "31000"},
            {"Strasbourg", "67000"}};
    private static final String[] RUES = {"rue des Lilas", "avenue Jean Jaurès", "boulevard Voltaire",
            "rue de la République", "place du Marché", "allée des Tilleuls"};
    private static final String[] LIBELLES = {"Carte - Supermarché", "Virement salaire", "Prélèvement électricité",
            "Carte - Restaurant", "Virement reçu", "Retrait DAB"};
    private static final String[] CATEGORIES = {"ALIMENTATION", "REVENUS", "LOGEMENT", "LOISIRS", "REVENUS", "ESPECES"};
    private static final TypeCompte[] TYPES = {TypeCompte.COURANT, TypeCompte.EPARGNE, TypeCompte.TITRES};

    private final List<Client> clients = new ArrayList<>();
    private final List<Operation> operations = new ArrayList<>();

    public JeuDeDonnees() {
        int numeroOperation = 1;
        for (int i = 1; i <= NOMBRE_CLIENTS; i++) {
            String nom = NOMS[i % NOMS.length];
            String prenom = PRENOMS[(i * 7) % PRENOMS.length];
            String[] ville = i % 11 == 0 ? new String[]{"Bruxelles", "1000"} : VILLES[i % VILLES.length];
            Adresse adresse = new Adresse((i * 3 % 90 + 1) + " " + RUES[i % RUES.length], ville[1], ville[0],
                    i % 11 == 0 ? "BE" : "FR");
            StatutClient statut = i % 10 == 0 ? StatutClient.INACTIF
                    : i % 7 == 0 ? StatutClient.PROSPECT : StatutClient.ACTIF;
            String telephone = i % 5 == 0 ? null : String.format("+33 6 %02d %02d %02d %02d",
                    i % 100, (i * 3) % 100, (i * 7) % 100, (i * 11) % 100);

            List<Compte> comptes = new ArrayList<>();
            int nombreComptes = 1 + (i % 3);
            for (int k = 1; k <= nombreComptes; k++) {
                String numero = String.format("FR76-%04d-%02d", i, k);
                comptes.add(new Compte(numero, TYPES[(k - 1) % TYPES.length],
                        BigDecimal.valueOf((i * 137_311L + k * 91_129L) % 2_500_000L, 2), "EUR",
                        LocalDate.of(2015, 1, 1).plusDays(i * 37L + k * 101L)));
                for (int j = 0; j < 3; j++) {
                    int indice = (i + j + k) % LIBELLES.length;
                    boolean sansCategorie = j == 2 && i % 4 == 0;
                    operations.add(new Operation(String.format("OP-%06d", numeroOperation++), numero,
                            LocalDate.of(2026, 9, 1).plusDays((i + j * 3L + k) % 29),
                            BigDecimal.valueOf((i * 9_781L + j * 5_303L + k * 3_119L) % 50_000L + 100L, 2),
                            j % 2 == 0 ? "DEBIT" : "CREDIT", LIBELLES[indice],
                            sansCategorie ? null : CATEGORIES[indice]));
                }
            }
            clients.add(new Client((long) i, String.format("CLI-%06d", i), nom, prenom,
                    (ascii(prenom) + "." + ascii(nom) + i + "@exemple.fr").toLowerCase(), telephone, statut,
                    adresse, List.copyOf(comptes),
                    Instant.parse("2024-01-01T09:00:00Z").plus(i * 5L, ChronoUnit.DAYS),
                    BASE.plus(i, ChronoUnit.DAYS)));
        }
    }

    public List<Client> clients() {
        return clients;
    }

    public List<Operation> operations() {
        return operations;
    }

    private static String ascii(String texte) {
        return Normalizer.normalize(texte, Normalizer.Form.NFD).replaceAll("\\p{M}", "").replaceAll("[^A-Za-z]", "");
    }
}
