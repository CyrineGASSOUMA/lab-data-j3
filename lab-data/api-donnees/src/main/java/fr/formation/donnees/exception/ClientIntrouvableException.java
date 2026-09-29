package fr.formation.donnees.exception;

public class ClientIntrouvableException extends RuntimeException {

    public ClientIntrouvableException(long id) {
        super("Le client " + id + " n'existe pas");
    }
}
