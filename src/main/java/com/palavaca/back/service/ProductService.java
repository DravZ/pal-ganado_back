package com.palavaca.back.service;

import com.palavaca.back.dto.ProductDTO;
import com.palavaca.back.model.Organization;
import com.palavaca.back.model.Product;
import com.palavaca.back.repository.OrganizationRepository;
import com.palavaca.back.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final OrganizationRepository organizationRepository;

    @Transactional(readOnly = true)
    public List<ProductDTO.Response> findByOrganizationId(UUID organizationId) {
        return productRepository.findByOrganizationId(organizationId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductDTO.Response findById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + id));
        return mapToResponse(product);
    }

    @Transactional
    public ProductDTO.Response create(ProductDTO.CreateRequest request) {
        Organization org = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new EntityNotFoundException("Organización no encontrada con ID: " + request.getOrganizationId()));

        Product product = Product.builder()
                .organization(org)
                .name(request.getName())
                .category(request.getCategory())
                .unitOfMeasure(request.getUnitOfMeasure())
                .description(request.getDescription())
                .build();

        return mapToResponse(productRepository.save(product));
    }

    @Transactional
    public ProductDTO.Response update(UUID id, ProductDTO.UpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + id));

        if (request.getName() != null) product.setName(request.getName());
        if (request.getCategory() != null) product.setCategory(request.getCategory());
        if (request.getUnitOfMeasure() != null) product.setUnitOfMeasure(request.getUnitOfMeasure());
        if (request.getDescription() != null) product.setDescription(request.getDescription());

        return mapToResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Producto no encontrado con ID: " + id);
        }
        productRepository.deleteById(id);
    }

    private ProductDTO.Response mapToResponse(Product p) {
        return ProductDTO.Response.builder()
                .id(p.getId())
                .organizationId(p.getOrganization().getId())
                .name(p.getName())
                .category(p.getCategory())
                .unitOfMeasure(p.getUnitOfMeasure())
                .description(p.getDescription())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}