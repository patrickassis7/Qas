package br.com.organizacaorecursos.auth.application;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Credenciais inválidas.");
    }
}
