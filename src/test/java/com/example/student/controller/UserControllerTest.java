package com.example.student.controller;

import com.example.student.dto.user.UserRequest;
import com.example.student.dto.user.UserResponse;
import com.example.student.dto.user.UserResponseList;
import com.example.student.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void testGetAllUsers() {
        UserResponseList mockList = Mockito.mock(UserResponseList.class);
        when(userService.findAll()).thenReturn(mockList);

        UserResponseList result = userController.getAllUsers();

        assertNotNull(result);
        verify(userService).findAll();
    }

    @Test
    void testUpdateUser() {
        UserRequest request = Mockito.mock(UserRequest.class);
        UserResponse response = Mockito.mock(UserResponse.class);
        when(userService.updateUser(request)).thenReturn(response);

        UserResponse result = userController.updateUser(request);

        assertNotNull(result);
        verify(userService).updateUser(request);
    }

    @Test
    void testSaveUser() {
        UserRequest request = Mockito.mock(UserRequest.class);
        UserResponse response = Mockito.mock(UserResponse.class);
        when(userService.save(request)).thenReturn(response);

        UserResponse result = userController.save(request);

        assertNotNull(result);
        verify(userService).save(request);
    }

    @Test
    void testDeleteById() {
        userController.deleteById(1L);
        verify(userService).deleteById(1L);
    }
}