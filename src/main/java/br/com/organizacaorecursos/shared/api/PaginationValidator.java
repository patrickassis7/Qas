package br.com.organizacaorecursos.shared.api;

public final class PaginationValidator {

    public static final int MIN_PAGE_SIZE = 1;
    public static final int MAX_PAGE_SIZE = 100;

    private PaginationValidator() {
    }

    public static void validate(int page, int size) {
        if (page < 0 || size < MIN_PAGE_SIZE || size > MAX_PAGE_SIZE) {
            throw new InvalidPaginationException(page, size);
        }
    }
}
