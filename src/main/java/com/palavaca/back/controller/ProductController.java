package com.palavaca.back.controller;

import com.palavaca.back.dto.ProductDTO;
import com.palavaca.back.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Gestión del catálogo de productos e insumos")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Obtener catálogo de productos de una organización")
    public ResponseEntity<List<ProductDTO.Response>> getByOrganization(@RequestParam("organization_id") UUID organizationId) {
        return ResponseEntity.ok(productService.findByOrganizationId(organizationId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por ID")
    public ResponseEntity<ProductDTO.Response> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Registrar nuevo producto")
    public ResponseEntity<ProductDTO.Response> create(@Valid @RequestBody ProductDTO.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar datos de un producto")
    public ResponseEntity<ProductDTO.Response> update(
            @PathVariable UUID id,
            @Valid @RequestBody ProductDTO.UpdateRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un producto")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}