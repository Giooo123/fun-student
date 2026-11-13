package com.example.student.service.impl;

import com.example.student.dto.user.UserRequest;
import com.example.student.dto.user.UserResponse;
import com.example.student.exception.EmailAlreadyExistsException;
import com.example.student.exception.EntityNotFoundException;
import com.example.student.mapper.UserMapper;
import com.example.student.model.Role;
import com.example.student.model.User;
import com.example.student.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {
    private UserRepository repository;
    private UserMapper mapper;
    private PasswordEncoder encoder;
    private UserServiceImpl service;

    @BeforeEach void setup() {
        repository = mock(UserRepository.class);
        mapper = mock(UserMapper.class);
        encoder = mock(PasswordEncoder.class);
        service = new UserServiceImpl(repository, mapper, encoder);
    }

    @Test void saveEncodesPasswordAndPersistsUser() {
        UserRequest request = new UserRequest("user@example.com", "plain-pass", Set.of(Role.TEACHER));
        when(repository.existsByEmail("user@example.com")).thenReturn(false);
        when(encoder.encode("plain-pass")).thenReturn("hashed-pass");
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toResponse(any(User.class))).thenReturn(new UserResponse(1, "user@example.com", Set.of(Role.TEACHER)));
        UserResponse response = service.save(request);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repository).save(captor.capture());
        assertEquals("hashed-pass", captor.getValue().getPassword());
        assertEquals("user@example.com", response.getEmail());
    }

    @Test void saveRejectsDuplicateEmail() {
        when(repository.existsByEmail("duplicate@example.com")).thenReturn(true);
        assertThrows(EmailAlreadyExistsException.class,
                () -> service.save(new UserRequest("duplicate@example.com", "pass", Set.of(Role.ADMIN))));
        verify(repository, never()).save(any(User.class));
    }

    @Test void updateChangesEmailRolesAndPassword() {
        User existing = new User();
        existing.setEmail("old@example.com");
        existing.setPassword("old-hash");
        existing.setRoles(Set.of(Role.TEACHER));
        when(repository.findById(5L)).thenReturn(Optional.of(existing));
        when(repository.existsByEmail("new@example.com")).thenReturn(false);
        when(encoder.encode("new-password")).thenReturn("new-hash");
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toResponse(any(User.class))).thenReturn(new UserResponse(5L, "new@example.com", Set.of(Role.ADMIN)));
        UserResponse response = service.updateUser(5L,
                new UserRequest("new@example.com", "new-password", Set.of(Role.ADMIN)));
        assertEquals("new@example.com", existing.getEmail());
        assertEquals("new-hash", existing.getPassword());
        assertEquals(Set.of(Role.ADMIN), existing.getRoles());
        assertEquals("new@example.com", response.getEmail());
    }

    @Test void updateMissingUserThrows() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> service.updateUser(99L, new UserRequest("x@y.com", "pass", Set.of(Role.ADMIN))));
    }

    @Test void updateRejectsEmailUsedBySomeoneElse() {
        User existing = new User();
        existing.setEmail("old@example.com");
        when(repository.findById(7L)).thenReturn(Optional.of(existing));
        when(repository.existsByEmail("taken@example.com")).thenReturn(true);
        assertThrows(EmailAlreadyExistsException.class,
                () -> service.updateUser(7L, new UserRequest("taken@example.com", null, null)));
        verify(repository, never()).save(any(User.class));
    }
}
