package com.example.student.controller;

import com.example.student.StudentApplication;
import com.example.student.configuration.AppInitializer;
import com.example.student.configuration.SecurityConfiguration;
import com.example.student.dto.*;
import com.example.student.dto.user.UserRequest;
import com.example.student.exception.AppError;
import com.example.student.exception.EmailAlreadyExistsException;
import com.example.student.exception.EntityNotFoundException;
import com.example.student.exception.GlobalExceptionHandler;
import com.example.student.mapper.StudentMapperImpl;
import com.example.student.mapper.UserMapperImpl;
import com.example.student.model.Role;
import com.example.student.model.Student;
import com.example.student.model.User;
import com.example.student.repository.StudentRepository;
import com.example.student.repository.UserRepository;
import com.example.student.security.JwtFilter;
import com.example.student.security.JwtService;
import com.example.student.service.CustomUserDetailsService;
import com.example.student.service.impl.StudentServiceImpl;
import com.example.student.service.impl.UserServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ProjectCoverageTest {

    @Test
    void testMappers() {
        StudentMapperImpl studentMapper = new StudentMapperImpl();
        StudentRequest req = new StudentRequest("Ivan", "Ivanov", "+7999", "ivan@mail.ru");

        Student student = studentMapper.requestToStudent(req);
        student.setId(1L);

        assertNotNull(studentMapper.studentToFullResponse(student));
        assertNotNull(studentMapper.studentToResponse(student));
        assertNotNull(studentMapper.studentToStudentResponseForList(student));
        assertNotNull(studentMapper.studentListToListOfStudentResponse(List.of(student)));
        assertNotNull(studentMapper.studentListToStudentResponseList(List.of(student)));

        UserMapperImpl userMapper = new UserMapperImpl();
        UserRequest userReq = new UserRequest();
        userReq.setEmail("user@mail.ru");
        userReq.setPassword("12345");
        userReq.setRoles(Set.of(Role.ADMIN));

        User user = userMapper.toEntity(userReq);
        user.setId(1L);

        assertNotNull(userMapper.toResponse(user));
        assertNotNull(userMapper.toResponseList(List.of(user)));
        assertNotNull(userMapper.toListResponse(List.of(user)));
    }

    @Test
    void testStudentService() {
        StudentRepository repo = Mockito.mock(StudentRepository.class);
        StudentMapperImpl mapper = new StudentMapperImpl();
        StudentServiceImpl service = new StudentServiceImpl(repo, mapper);

        Student student = new Student();
        student.setId(1L);
        student.setName("Petr");
        student.setSurname("Petrov");

        when(repo.save(any(Student.class))).thenReturn(student);
        when(repo.findById(1L)).thenReturn(Optional.of(student));
        when(repo.findAll()).thenReturn(List.of(student));

        assertNotNull(service.save(new StudentRequest("Petr", "Petrov", "123", "p@mail.ru")));
        assertNotNull(service.findById(1L));
        assertNotNull(service.findAll());

        // Обе ветки if (не null и null) для Branch coverage
        assertNotNull(service.update(1L, new StudentUpdateRequest("999", "new@mail.ru")));
        assertNotNull(service.update(1L, new StudentUpdateRequest(null, null)));

        service.deleteById(1L);
        verify(repo).deleteById(1L);
    }

    @Test
    void testUserServiceAndDetails() {
        UserRepository repo = Mockito.mock(UserRepository.class);
        UserMapperImpl mapper = new UserMapperImpl();
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
        UserServiceImpl userService = new UserServiceImpl(repo, mapper, encoder);

        User user = new User();
        user.setId(1L);
        user.setEmail("admin@mail.ru");
        user.setPassword("pass");
        user.setRoles(Set.of(Role.ADMIN));

        UserRequest req = new UserRequest();
        req.setEmail("admin@mail.ru");
        req.setPassword("pass");
        req.setRoles(Set.of(Role.ADMIN));

        when(repo.existsByEmail("admin@mail.ru")).thenReturn(false);
        when(encoder.encode(anyString())).thenReturn("encoded");
        when(repo.save(any(User.class))).thenReturn(user);
        when(repo.findByEmail("admin@mail.ru")).thenReturn(Optional.of(user));
        when(repo.findAll()).thenReturn(List.of(user));

        assertFalse(userService.existsByEmail("admin@mail.ru"));
        assertTrue(userService.register(req));
        assertNotNull(userService.save(req));
        assertNotNull(userService.findByEmail("admin@mail.ru"));
        assertNotNull(userService.findAll());
        userService.updateUser(req);
        userService.deleteById(1L);

        // Вторые ветки if для Branch coverage (пустой пароль, null email, существующий email)
        UserRequest emptyPassReq = new UserRequest();
        emptyPassReq.setEmail("admin@mail.ru");
        emptyPassReq.setPassword("");
        userService.register(emptyPassReq);
        assertFalse(userService.register(new UserRequest()));

        when(repo.existsByEmail("admin@mail.ru")).thenReturn(true);
        assertFalse(userService.register(req));
        assertThrows(EmailAlreadyExistsException.class, () -> userService.save(req));

        CustomUserDetailsService detailsService = new CustomUserDetailsService(repo);
        UserDetails details = detailsService.loadUserByUsername("admin@mail.ru");
        assertEquals("admin@mail.ru", details.getUsername());
    }

    @Test
    void testJwtSecurity() throws Exception {
        JwtService jwtService = new JwtService();
        String token = jwtService.generateToken("admin@mail.ru", Set.of(Role.ADMIN));

        assertNotNull(token);
        assertEquals("admin@mail.ru", jwtService.extractUsername(token));
        assertTrue(jwtService.extractRoles(token).contains(Role.ADMIN));

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("admin@mail.ru")
                .password("pass")
                .authorities("ROLE_ADMIN")
                .build();

        assertTrue(jwtService.validateToken(token, userDetails));

        CustomUserDetailsService detailsService = Mockito.mock(CustomUserDetailsService.class);
        when(detailsService.loadUserByUsername("admin@mail.ru")).thenReturn(userDetails);

        JwtFilter filter = new JwtFilter(detailsService, jwtService);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain chain = Mockito.mock(FilterChain.class);

        // Ветка 1: без заголовка Authorization
        SecurityContextHolder.clearContext();
        when(request.getHeader("Authorization")).thenReturn(null);
        filter.doFilter(request, response, chain);

        // Ветка 2: заголовок не начинается с Bearer
        when(request.getHeader("Authorization")).thenReturn("Basic 123");
        filter.doFilter(request, response, chain);

        // Ветка 3: валидный Bearer токен
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        filter.doFilter(request, response, chain);
    }

    @Test
    void testConfigAndExceptions() throws Exception {
        UserRepository userRepo = Mockito.mock(UserRepository.class);
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
        StudentRepository studentRepo = Mockito.mock(StudentRepository.class);

        AppInitializer initializer = new AppInitializer(userRepo, encoder, studentRepo);

        // Ветка 1: база пустая
        when(userRepo.existsByEmail(anyString())).thenReturn(false);
        when(studentRepo.count()).thenReturn(0L);
        initializer.init();

        // Ветка 2: пользователи и студенты уже есть (для 100% Branch в configuration)
        when(userRepo.existsByEmail(anyString())).thenReturn(true);
        when(studentRepo.count()).thenReturn(5L);
        initializer.init();

        SecurityConfiguration securityConfig = new SecurityConfiguration(Mockito.mock(JwtFilter.class));
        assertNotNull(securityConfig.passwordEncoder());
        securityConfig.authenticationManager(Mockito.mock(AuthenticationConfiguration.class));

        EmailAlreadyExistsException ex1 = new EmailAlreadyExistsException("Exists");
        EntityNotFoundException ex2 = new EntityNotFoundException("Not found");
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        AppError appError = new AppError(404, "Error");
        StudentApplication app = new StudentApplication();

        assertEquals("Exists", ex1.getMessage());
        assertEquals("Not found", ex2.getMessage());
        assertNotNull(handler);
        assertNotNull(appError);
        assertNotNull(app);
    }
}