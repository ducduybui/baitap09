package vn.iotstar.controller;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import vn.iotstar.dto.UserDTO;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.service.UserService;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RoleRepository roleRepository;

    @GetMapping
    public String list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        model.addAttribute(
                "users",
                userService.findAll(
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

        return "users/list";
    }

    @GetMapping("/create")
    public String create(Model model) {

        UserDTO dto = new UserDTO();

        dto.setEnabled(true);
        dto.setRoleName("USER");

        prepareForm(
                model,
                dto,
                "create"
        );

        return "users/form";
    }

    @PostMapping("/create")
    public String create(
            @Valid @ModelAttribute("userDTO") UserDTO dto,
            BindingResult result,
            @RequestParam(required = false) String password,
            Model model,
            RedirectAttributes redirect) {

        if (result.hasErrors()) {

            prepareForm(
                    model,
                    dto,
                    "create"
            );

            return "users/form";
        }

        try {

            userService.create(
                    dto,
                    password
            );

            redirect.addFlashAttribute(
                    "success",
                    "Tạo user thành công."
            );

            return "redirect:/users";

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            prepareForm(
                    model,
                    dto,
                    "create"
            );

            return "users/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(
            @PathVariable Long id,
            Model model) {

        prepareForm(
                model,
                userService.findById(id),
                "edit"
        );

        return "users/form";
    }

    @PostMapping("/edit/{id}")
    public String edit(
            @PathVariable Long id,
            @Valid @ModelAttribute("userDTO") UserDTO dto,
            BindingResult result,
            @RequestParam(required = false) String password,
            Model model,
            RedirectAttributes redirect) {

        if (result.hasErrors()) {

            prepareForm(
                    model,
                    dto,
                    "edit"
            );

            return "users/form";
        }

        try {

            userService.update(
                    id,
                    dto,
                    password
            );

            redirect.addFlashAttribute(
                    "success",
                    "Cập nhật user thành công."
            );

            return "redirect:/users";

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            prepareForm(
                    model,
                    dto,
                    "edit"
            );

            return "users/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes redirect) {

        try {

            userService.delete(id);

            redirect.addFlashAttribute(
                    "success",
                    "Xóa user thành công."
            );

        } catch (IllegalArgumentException e) {

            redirect.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/users";
    }

    private void prepareForm(
            Model model,
            UserDTO dto,
            String mode) {

        model.addAttribute(
                "userDTO",
                dto
        );

        model.addAttribute(
                "mode",
                mode
        );

        model.addAttribute(
                "roles",
                roleRepository.findAll()
        );
    }
}