package com.example.student.controller;

import com.example.student.dto.*;
import com.example.student.service.StudentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentControllerTest {

    @Mock
    private StudentService studentService;

    @InjectMocks
    private StudentController studentController;

    @Test
    @DisplayName("Тест получения списка студентов (findAll)")
    void testFindAll() {
        StudentResponseList mockResponse = Mockito.mock(StudentResponseList.class);
        when(studentService.findAll()).thenReturn(mockResponse);

        StudentResponseList result = studentController.findAll();

        assertNotNull(result);
        verify(studentService, times(1)).findAll();
    }

    @Test
    @DisplayName("Тест получения студента по ID (findStudentById)")
    void testFindStudentById() {
        Long studentId = 1L;
        StudentFullResponse mockResponse = new StudentFullResponse();
        when(studentService.findById(studentId)).thenReturn(mockResponse);

        StudentFullResponse result = studentController.findStudentById(studentId);

        assertNotNull(result);
        verify(studentService, times(1)).findById(studentId);
    }

    @Test
    @DisplayName("Тест создания студента (createStudent)")
    void testCreateStudent() {
        StudentRequest request = new StudentRequest("Ivan", "Ivanov", "+79991112233", "ivan@test.com");
        StudentResponse mockResponse = new StudentResponse();
        when(studentService.save(request)).thenReturn(mockResponse);

        StudentResponse result = studentController.createStudent(request);

        assertNotNull(result);
        verify(studentService, times(1)).save(request);
    }

    @Test
    @DisplayName("Тест обновления студента (updateStudent)")
    void testUpdateStudent() {
        Long studentId = 1L;
        StudentUpdateRequest request = new StudentUpdateRequest("+79993332211", "new@test.com");
        StudentResponse mockResponse = new StudentResponse();
        when(studentService.update(studentId, request)).thenReturn(mockResponse);

        StudentResponse result = studentController.updateStudent(studentId, request);

        assertNotNull(result);
        verify(studentService, times(1)).update(studentId, request);
    }

    @Test
    @DisplayName("Тест удаления студента (deleteStudentById)")
    void testDeleteStudent() {
        Long studentId = 1L;
        doNothing().when(studentService).deleteById(studentId);

        studentController.deleteStudentById(studentId);

        verify(studentService, times(1)).deleteById(studentId);
    }
}