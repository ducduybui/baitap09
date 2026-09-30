package vn.iotstar.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @GetMapping
    public String dashboard(Model model) {
        List<User> users = userRepository.findAll();

        model.addAttribute("totalUsers", users.size());
        model.addAttribute("enabledUsers", users.stream().filter(User::isEnabled).count());
        model.addAttribute("disabledUsers", users.stream().filter(u -> !u.isEnabled()).count());
        model.addAttribute("adminUsers", users.stream().filter(u -> u.getRole() != null && "ADMIN".equalsIgnoreCase(u.getRole().getName())).count());
        model.addAttribute("normalUsers", users.stream().filter(u -> u.getRole() != null && "USER".equalsIgnoreCase(u.getRole().getName())).count());
        model.addAttribute("users", users);

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String users() {
        return "redirect:/users";
    }

    @GetMapping("/users/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user."));
        model.addAttribute("user", user);
        model.addAttribute("roles", roleRepository.findAll());
        return "admin/edit-user";
    }

    @PostMapping("/users/edit/{id}")
    public String update(
            @PathVariable Long id,
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String fullName,
            @RequestParam Long roleId,
            @RequestParam(defaultValue = "false") boolean enabled,
            RedirectAttributes redirect) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user."));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy role."));

        user.setUsername(username.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setFullName(fullName.trim());
        user.setRole(role);
        user.setEnabled(enabled);
        userRepository.save(user);

        redirect.addFlashAttribute("success", "Cập nhật user thành công.");
        return "redirect:/users";
    }

    @PostMapping("/users/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        userRepository.deleteById(id);
        redirect.addFlashAttribute("success", "Xóa user thành công.");
        return "redirect:/users";
    }
}
