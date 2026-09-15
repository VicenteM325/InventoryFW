package com.vm325.inventory_back.services;

import com.vm325.inventory_back.dtos.UserCreateDto;
import com.vm325.inventory_back.dtos.UserResponseDto;
import com.vm325.inventory_back.dtos.UserUpdateDto;
import com.vm325.inventory_back.entities.Role;
import com.vm325.inventory_back.entities.User;
import com.vm325.inventory_back.enums.RoleList;
import com.vm325.inventory_back.repositories.RoleRepository;
import com.vm325.inventory_back.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserResponseDto> findAll() {
        return userRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDto create(UserCreateDto dto) {
        if (userRepository.existsByUserName(dto.getUserName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un usuario con el nombre de usuario " + dto.getUserName());
        }

        User user = new User();
        user.setName(dto.getName());
        user.setUserName(dto.getUserName());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(resolveRole(dto.getRoleName()));
        user.setActive(true);

        return toDto(userRepository.save(user));
    }

    @Override
    public UserResponseDto update(UUID id, UserUpdateDto dto) {
        User user = getUserOrThrow(id);
        user.setName(dto.getName());
        user.setRole(resolveRole(dto.getRoleName()));
        return toDto(userRepository.save(user));
    }

    @Override
    public UserResponseDto setActive(UUID id, boolean active) {
        User user = getUserOrThrow(id);
        user.setActive(active);
        return toDto(userRepository.save(user));
    }

    private Role resolveRole(String roleName) {
        RoleList roleList;
        try {
            roleList = RoleList.valueOf(roleName.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol inválido: " + roleName);
        }
        return roleRepository.findByName(roleList)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Rol no configurado en el sistema: " + roleName));
    }

    private User getUserOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Usuario no encontrado con id " + id));
    }

    private UserResponseDto toDto(User user) {
        return UserResponseDto.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .userName(user.getUserName())
                .roleName(user.getRole().getName().toString())
                .active(user.isActive())
                .build();
    }
}
