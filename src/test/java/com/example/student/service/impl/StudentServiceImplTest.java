package com.example.student.service.impl;

import com.example.student.dto.StudentRequest;
import com.example.student.dto.StudentResponse;
import com.example.student.dto.StudentUpdateRequest;
import com.example.student.exception.EntityNotFoundException;
import com.example.student.mapper.StudentMapper;
import com.example.student.model.Student;
import com.example.student.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StudentServiceImplTest {
    private StudentRepository repository;
    private StudentMapper mapper;
    private StudentServiceImpl service;

    @BeforeEach void setup() {
        repository = mock(StudentRepository.class);
        mapper = mock(StudentMapper.class);
        service = new StudentServiceImpl(repository, mapper);
    }

    @Test void saveMapsAndPersistsStudent() {
        StudentRequest request = new StudentRequest("Anna", "Ivanova", "123", "anna@example.com");
        Student entity = new Student();
        when(mapper.requestToStudent(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        service.save(request);
        verify(repository).save(entity);
        verify(mapper).studentToResponse(entity);
    }

    @Test void updateOnlyProvidedFields() {
        Student existing = new Student();
        existing.setEmail("old@example.com");
        existing.setPhoneNumber("111");
        when(repository.findById(2L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));
        service.update(2L, new StudentUpdateRequest("222", null));
        assertEquals("222", existing.getPhoneNumber());
        assertEquals("old@example.com", existing.getEmail());
        verify(repository).save(existing);
    }

    @Test void updateMissingStudentThrows() {
        when(repository.findById(123L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> service.update(123L, new StudentUpdateRequest("333", null)));
        verify(repository, never()).save(any(Student.class));
    }

    @Test void findMissingStudentThrows() {
        when(repository.findById(44L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.findById(44L));
    }
}
