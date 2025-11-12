package com.vm325.inventory_back.controllers;

import com.vm325.inventory_back.dtos.ApiMessage;
import com.vm325.inventory_back.dtos.LoginUserDto;
import com.vm325.inventory_back.entities.User;
import com.vm325.inventory_back.services.AuthService;
import com.vm325.inventory_back.services.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @Autowired
    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiMessage> login(@Valid @RequestBody LoginUserDto loginUserDto, BindingResult bindingResult, HttpServletResponse response){
        if (bindingResult.hasErrors()){
            return ResponseEntity.badRequest().body(new ApiMessage("Revise sus credenciales"));
        }
        try {
            String roleName = authService.authenticate(loginUserDto.getUserName(), loginUserDto.getPassword(), response);
            return ResponseEntity.ok(new ApiMessage(roleName));
        } catch (Exception e){
            return ResponseEntity.badRequest().body(new ApiMessage(e.getMessage()));
        }
    }


    @GetMapping("/check-auth")
    public ResponseEntity<String> checkAuth(){
        return ResponseEntity.ok().body("Autenticado");
    }

    @GetMapping("/user/details")
    public ResponseEntity<User> getAuthenticateUser(){
        User user = userService.getUserDetails();
        return ResponseEntity.ok(user);
    }
}