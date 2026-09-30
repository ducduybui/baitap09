package vn.iotstar.service.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.iotstar.dto.UserDTO;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.mapper.UserMapper;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> findAll(String keyword, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Direction.DESC, "id")
        );

        String value = keyword == null ? "" : keyword.trim();

        return userRepository
                .findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(
                        value, value, value, pageable)
                .map(user -> {
                    UserDTO dto = mapper.toDTO(user);
                    dto.setProductCount(userRepository.countProductsByUserId(user.getId()));
                    return dto;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO findById(Long id) {
        return mapper.toDTO(userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại.")));
    }

    @Override
    @Transactional
    public UserDTO create(UserDTO dto, String rawPassword) {
        if (userRepository.existsByUsernameIgnoreCase(dto.getUsername())) {
            throw new IllegalArgumentException("Username đã tồn tại.");
        }
        if (userRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại.");
        }

        Role role = resolveRole(dto.getRoleId(), dto.getRoleName());

        String password = (rawPassword == null || rawPassword.isBlank())
                ? "123456"
                : rawPassword;

        User user = User.builder()
                .username(dto.getUsername().trim())
                .email(dto.getEmail().trim().toLowerCase())
                .fullName(dto.getFullName().trim())
                .password(passwordEncoder.encode(password))
                .images(dto.getImages())
                .enabled(dto.isEnabled())
                .role(role)
                .build();

        return mapper.toDTO(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDTO update(Long id, UserDTO dto, String rawPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại."));

        if (userRepository.findByUsernameIgnoreCase(dto.getUsername())
                .filter(other -> !other.getId().equals(id)).isPresent()) {
            throw new IllegalArgumentException("Username đã tồn tại.");
        }

        if (userRepository.findByEmailIgnoreCase(dto.getEmail())
                .filter(other -> !other.getId().equals(id)).isPresent()) {
            throw new IllegalArgumentException("Email đã tồn tại.");
        }

        user.setUsername(dto.getUsername().trim());
        user.setEmail(dto.getEmail().trim().toLowerCase());
        user.setFullName(dto.getFullName().trim());
        user.setImages(dto.getImages());
        user.setEnabled(dto.isEnabled());
        user.setRole(resolveRole(dto.getRoleId(), dto.getRoleName()));

        if (rawPassword != null && !rawPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(rawPassword));
        }

        return mapper.toDTO(userRepository.save(user));
    }

    private Role resolveRole(Long roleId, String roleName) {
        if (roleId != null) {
            return roleRepository.findById(roleId)
                    .orElseThrow(() -> new IllegalArgumentException("Role không tồn tại."));
        }

        String name = (roleName == null || roleName.isBlank()) ? "USER" : roleName;
        return roleRepository.findByNameIgnoreCase(name.replace("ROLE_", ""))
                .orElseThrow(() -> new IllegalArgumentException("Role không tồn tại."));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại."));
        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUsers() {
        return userRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countProducts(Long userId) {
        return userRepository.countProductsByUserId(userId);
    }
}
