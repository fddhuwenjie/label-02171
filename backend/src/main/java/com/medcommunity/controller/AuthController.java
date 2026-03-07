package com.medcommunity.controller;

import com.medcommunity.common.Result;
import com.medcommunity.dto.LoginRequest;
import com.medcommunity.dto.LoginResponse;
import com.medcommunity.entity.SysUser;
import com.medcommunity.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }

    @GetMapping("/info")
    public Result<SysUser> info(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        SysUser user = authService.getUserInfo(userId);
        return Result.success(user);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}
