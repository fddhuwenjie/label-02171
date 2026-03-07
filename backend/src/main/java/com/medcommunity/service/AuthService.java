package com.medcommunity.service;

import com.medcommunity.dto.LoginRequest;
import com.medcommunity.dto.LoginResponse;
import com.medcommunity.entity.SysUser;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    SysUser getUserInfo(Long userId);
}
