package vn.iotstar.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.iotstar.entity.Product;

public interface ProductRepository
        extends JpaRepository<Product, Long> {

    Page<Product>
    findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name,
            String description,
            Pageable pageable
    );

    Page<Product> findByUserId(
            Long userId,
            Pageable pageable
    );

    long countByUserId(Long userId);
}