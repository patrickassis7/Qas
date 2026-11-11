package br.com.organizacaorecursos.shared.api;

public class InvalidPaginationException extends RuntimeException {

    private final boolean invalidPage;
    private final boolean invalidSize;

    public InvalidPaginationException(int page, int size) {
        super("Parâmetros de paginação inválidos.");
        this.invalidPage = page < 0;
        this.invalidSize = size < PaginationValidator.MIN_PAGE_SIZE
                || size > PaginationValidator.MAX_PAGE_SIZE;
    }

    public boolean isInvalidPage() {
        return invalidPage;
    }

    public boolean isInvalidSize() {
        return invalidSize;
    }
}
