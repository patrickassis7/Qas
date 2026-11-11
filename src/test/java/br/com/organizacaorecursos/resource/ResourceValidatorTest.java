package br.com.organizacaorecursos.resource;

import br.com.organizacaorecursos.resource.api.dto.CreateResourceRequest;
import br.com.organizacaorecursos.resource.application.ResourceValidator;
import br.com.organizacaorecursos.resource.domain.ResourceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourceValidatorTest {

    private final ResourceValidator validator = new ResourceValidator();

    @Test
    void acceptsValidRoom() {
        CreateResourceRequest request = new CreateResourceRequest(
                ResourceType.SALA,
                "Sala 101",
                "Bloco A",
                30,
                null,
                false,
                null
        );

        assertThatNoException()
                .isThrownBy(() -> validator.validate(request));
    }

    @Test
    void acceptsValidTeacher() {
        CreateResourceRequest request = new CreateResourceRequest(
                ResourceType.PROFESSOR,
                "João Silva",
                null,
                null,
                "Java,Spring",
                false,
                null
        );

        assertThatNoException()
                .isThrownBy(() -> validator.validate(request));
    }

    @Test
    void acceptsMaterial() {
        CreateResourceRequest request = new CreateResourceRequest(
                ResourceType.MATERIAL,
                "Projetor",
                null,
                null,
                null,
                false,
                null
        );

        assertThatNoException()
                .isThrownBy(() -> validator.validate(request));
    }

    @ParameterizedTest
    @MethodSource("invalidResources")
    void rejectsInvalidResources(CreateResourceRequest request) {
        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static Stream<CreateResourceRequest> invalidResources() {
        return Stream.of(
                new CreateResourceRequest(
                        ResourceType.SALA,
                        "Sala 101",
                        null,
                        30,
                        null,
                        false,
                        null
                ),
                new CreateResourceRequest(
                        ResourceType.SALA,
                        "Sala 101",
                        "Bloco A",
                        null,
                        null,
                        false,
                        null
                ),
                new CreateResourceRequest(
                        ResourceType.SALA,
                        "Sala 101",
                        "Bloco A",
                        0,
                        null,
                        false,
                        null
                ),
                new CreateResourceRequest(
                        ResourceType.SALA,
                        "Sala 101",
                        "Bloco A",
                        -1,
                        null,
                        false,
                        null
                ),
                new CreateResourceRequest(
                        ResourceType.PROFESSOR,
                        "João Silva",
                        null,
                        null,
                        null,
                        false,
                        null
                ),
                new CreateResourceRequest(
                        ResourceType.PROFESSOR,
                        "João Silva",
                        null,
                        null,
                        "",
                        false,
                        null
                )
        );
    }
}