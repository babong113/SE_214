package com.bteam.badmintonmanagement.controller;

import com.bteam.badmintonmanagement.dto.request.RequestLogin;
import com.bteam.badmintonmanagement.dto.request.RequestRegister;
import com.bteam.badmintonmanagement.dto.response.ApiResponse;
import com.bteam.badmintonmanagement.dto.response.ResponseLogin;
import com.bteam.badmintonmanagement.dto.response.ResponseRegister;
import com.bteam.badmintonmanagement.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(
            @Valid
            @RequestBody
            RequestRegister request)
    {
        ResponseRegister response=authService.register(request);
        ApiResponse<ResponseRegister>
                apiResponse = ApiResponse.<ResponseRegister>builder()
                        .success(true)
                        .message("Đăng ký thành công")
                        .data(response)
                        .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login (
            @Valid
            @RequestBody
            RequestLogin request
    )
    {
        ResponseLogin respone=authService.login(request);
        ApiResponse<ResponseLogin>
                apiResponse=ApiResponse.<ResponseLogin>builder()
                .success(true)
                .message("Đăng nhập thành công ")
                .data(respone)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);

    }

}
