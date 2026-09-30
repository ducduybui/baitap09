package vn.iotstar.service.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;

import vn.iotstar.dto.ProductDTO;

import vn.iotstar.entity.Product;
import vn.iotstar.entity.User;

import vn.iotstar.mapper.ProductMapper;

import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.UserRepository;

import vn.iotstar.service.CloudinaryService;
import vn.iotstar.service.CloudinaryUploadResult;
import vn.iotstar.service.ProductService;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl
        implements ProductService {

    private final ProductRepository productRepository;

    private final UserRepository userRepository;

    private final ProductMapper mapper;

    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findAll(
            String keyword,
            int page,
            int size) {

        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);

        Pageable pageable =
                PageRequest.of(
                        safePage,
                        safeSize,
                        Sort.by(
                                Sort.Direction.DESC,
                                "id"
                        )
                );

        String value =
                keyword == null
                        ? ""
                        : keyword.trim();

        return productRepository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        value,
                        value,
                        pageable
                )
                .map(mapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO findById(Long id) {

        Product product =
                productRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Product không tồn tại."
                                )
                        );

        return mapper.toDTO(product);
    }

    @Override
    @Transactional
    public ProductDTO create(
            ProductDTO dto,
            MultipartFile image,
            Long userId) {

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User không tồn tại."
                                )
                        );

        Product product =
                mapper.toEntity(dto);

        product.setUser(user);

        if (image != null
                && !image.isEmpty()) {

            CloudinaryUploadResult result =
                    cloudinaryService.upload(image);

            product.setImageUrl(
                    result.url()
                            + "|"
                            + result.publicId()
            );
        }

        return mapper.toDTO(
                productRepository.save(product)
        );
    }

    @Override
    @Transactional
    public ProductDTO update(
            Long id,
            ProductDTO dto,
            MultipartFile image) {

        Product product =
                productRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Product không tồn tại."
                                )
                        );

        String name = dto.getName();

        if (name == null
                || name.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Tên sản phẩm không được để trống."
            );
        }

        product.setName(
                name.trim()
        );

        product.setDescription(
                dto.getDescription()
        );

        product.setPrice(
                dto.getPrice()
        );

        if (image != null
                && !image.isEmpty()) {

            deleteStoredImage(
                    product.getImageUrl()
            );

            CloudinaryUploadResult result =
                    cloudinaryService.upload(image);

            product.setImageUrl(
                    result.url()
                            + "|"
                            + result.publicId()
            );
        }

        return mapper.toDTO(
                productRepository.save(product)
        );
    }

    @Override
    @Transactional
    public void delete(Long id) {

        Product product =
                productRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Product không tồn tại."
                                )
                        );

        deleteStoredImage(
                product.getImageUrl()
        );

        productRepository.delete(product);
    }

    private void deleteStoredImage(
            String imageValue) {

        if (imageValue == null
                || imageValue.isBlank()
                || !imageValue.contains("|")) {

            return;
        }

        String publicId =
                imageValue.substring(
                        imageValue.indexOf('|') + 1
                );

        if (!publicId.isBlank()) {
            cloudinaryService.delete(
                    publicId
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countProducts() {

        return productRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByUser(Long userId) {

        return productRepository.countByUserId(
                userId
        );
    }
}