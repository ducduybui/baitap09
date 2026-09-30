package vn.iotstar.controller;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import vn.iotstar.dto.ProductDTO;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.ProductService;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public String list(
            @RequestParam(
                    defaultValue = ""
            ) String keyword,

            @RequestParam(
                    defaultValue = "0"
            ) int page,

            @RequestParam(
                    defaultValue = "10"
            ) int size,

            Model model) {

        model.addAttribute(
                "products",
                productService.findAll(
                        keyword,
                        page,
                        size
                )
        );

        model.addAttribute(
                "keyword",
                keyword
        );

        model.addAttribute(
                "size",
                size
        );

        model.addAttribute(
                "page",
                page
        );

        return "products/list";
    }

    @GetMapping("/create")
    public String create(Model model) {

        model.addAttribute(
                "productDTO",
                new ProductDTO()
        );

        model.addAttribute(
                "mode",
                "create"
        );

        return "products/form";
    }

    @PostMapping("/create")
    public String create(
            @Valid
            @ModelAttribute("productDTO")
            ProductDTO dto,

            BindingResult result,

            @RequestParam(
                    required = false
            )
            MultipartFile image,

            Authentication authentication,

            Model model,

            RedirectAttributes redirect) {

        if (result.hasErrors()) {

            model.addAttribute(
                    "mode",
                    "create"
            );

            return "products/form";
        }

        try {

            Long userId =
                    getCurrentUserId(
                            authentication
                    );

            productService.create(
                    dto,
                    image,
                    userId
            );

            redirect.addFlashAttribute(
                    "success",
                    "Thêm sản phẩm thành công."
            );

            return "redirect:/products";

        } catch (RuntimeException e) {

            model.addAttribute(
                    "mode",
                    "create"
            );

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            return "products/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(
            @PathVariable Long id,
            Model model) {

        model.addAttribute(
                "productDTO",
                productService.findById(id)
        );

        model.addAttribute(
                "mode",
                "edit"
        );

        return "products/form";
    }

    @PostMapping("/edit/{id}")
    public String edit(
            @PathVariable Long id,

            @Valid
            @ModelAttribute("productDTO")
            ProductDTO dto,

            BindingResult result,

            @RequestParam(
                    required = false
            )
            MultipartFile image,

            Model model,

            RedirectAttributes redirect) {

        if (result.hasErrors()) {

            model.addAttribute(
                    "mode",
                    "edit"
            );

            return "products/form";
        }

        try {

            productService.update(
                    id,
                    dto,
                    image
            );

            redirect.addFlashAttribute(
                    "success",
                    "Cập nhật sản phẩm thành công."
            );

            return "redirect:/products";

        } catch (RuntimeException e) {

            model.addAttribute(
                    "mode",
                    "edit"
            );

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            return "products/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes redirect) {

        try {

            productService.delete(id);

            redirect.addFlashAttribute(
                    "success",
                    "Xóa sản phẩm thành công."
            );

        } catch (RuntimeException e) {

            redirect.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/products";
    }

    private Long getCurrentUserId(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "Chưa đăng nhập."
            );
        }

        Object principal =
                authentication.getPrincipal();

        if (principal
                instanceof CustomUserDetails details) {

            return details.getId();
        }

        throw new IllegalStateException(
                "Không xác định được tài khoản đăng nhập."
        );
    }
}