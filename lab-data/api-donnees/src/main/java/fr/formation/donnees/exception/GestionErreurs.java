package fr.formation.donnees.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Erreurs au format Problem Details (RFC 9457). */
@RestControllerAdvice
public class GestionErreurs {

    @ExceptionHandler(ClientIntrouvableException.class)
    public ProblemDetail introuvable(ClientIntrouvableException e) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        p.setTitle("Client introuvable");
        return p;
    }

    @ExceptionHandler(ParametreInvalideException.class)
    public ProblemDetail parametre(ParametreInvalideException e) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        p.setTitle("Paramètre invalide");
        return p;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail typeInvalide(MethodArgumentTypeMismatchException e) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Valeur invalide pour le paramètre " + e.getName() + " : " + e.getValue());
        p.setTitle("Paramètre invalide");
        return p;
    }
}
