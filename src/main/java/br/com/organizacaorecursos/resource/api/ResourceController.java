package br.com.organizacaorecursos.resource.api;

import br.com.organizacaorecursos.resource.api.dto.CreateResourceRequest;
import br.com.organizacaorecursos.resource.api.dto.ResourceAvailabilityResponse;
import br.com.organizacaorecursos.resource.api.dto.ResourceResponse;
import br.com.organizacaorecursos.resource.api.dto.ResourceSearchRequest;
import br.com.organizacaorecursos.resource.api.dto.UpdateResourceRequest;
import br.com.organizacaorecursos.resource.application.ResourceMapper;
import br.com.organizacaorecursos.resource.application.ResourceService;
import br.com.organizacaorecursos.resource.domain.ResourceStatus;
import br.com.organizacaorecursos.resource.domain.ResourceType;
import br.com.organizacaorecursos.shared.infrastructure.web.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/resources")
public class ResourceController {

    private final ResourceService resourceService;
    private final ResourceMapper resourceMapper;

    public ResourceController(
            ResourceService resourceService,
            ResourceMapper resourceMapper
    ) {
        this.resourceService = resourceService;
        this.resourceMapper = resourceMapper;
    }

    @GetMapping
    public Page<ResourceResponse> search(
            @RequestParam(required = false) ResourceType type,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer minimumCapacity,
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) ResourceStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        ResourceSearchRequest request = new ResourceSearchRequest(
                type,
                location,
                minimumCapacity,
                skill,
                status
        );

        Pageable pageable = PageRequest.of(page, size);

        return resourceService.search(
                        request,
                        pageable
                )
                .map(resourceMapper::toResponse);
    }

    @GetMapping("/availability")
    public List<ResourceAvailabilityResponse> availability(
            @RequestParam(required = false) Instant startAt,
            @RequestParam(required = false) Instant endAt
    ) {

        return resourceService.availability(startAt, endAt)
                .stream()
                .map(resourceMapper::toAvailabilityResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ResourceResponse findById(@PathVariable UUID id) {
        return resourceMapper.toResponse(
                resourceService.findById(id)
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResourceResponse create(
            Authentication authentication,
            @Valid @RequestBody CreateResourceRequest request
    ) {
        return resourceMapper.toResponse(
                resourceService.create(CurrentUser.id(authentication), request)
        );
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResourceResponse update(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateResourceRequest request
    ) {
        return resourceMapper.toResponse(
                resourceService.update(id, CurrentUser.id(authentication), request)
        );
    }

    @PostMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
        public void deactivate(Authentication authentication, @PathVariable UUID id) {
                resourceService.deactivate(id, CurrentUser.id(authentication));
    }
}