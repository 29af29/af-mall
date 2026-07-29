package com.afei.mall.user.controller;

import com.afei.common.exception.BusinessException;
import com.afei.common.result.Result;
import com.afei.mall.user.domain.dto.AddressSaveDTO;
import com.afei.mall.user.domain.dto.UserUpdateDTO;
import com.afei.mall.user.domain.vo.UserAddressVO;
import com.afei.mall.user.domain.vo.UserVO;
import com.afei.mall.user.service.UserAddressService;
import com.afei.mall.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理")
@AllArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserAddressService userAddressService;

    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息")
    public Result<UserVO> info(@RequestHeader("Authorization") String authHeader) {
        return Result.success(userService.getUserInfo(extractToken(authHeader)));
    }

    @PutMapping("/info")
    @Operation(summary = "修改当前用户信息")
    public Result<UserVO> updateInfo(@RequestBody @Valid UserUpdateDTO dto,
                                      @RequestHeader("Authorization") String authHeader) {
        return Result.success(userService.updateUserInfo(extractToken(authHeader), dto));
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传用户头像")
    public Result<String> uploadAvatar(@RequestPart("file") MultipartFile file,
                                        @RequestHeader("Authorization") String authHeader) {
        return Result.success(userService.uploadAvatar(extractToken(authHeader), file));
    }

    @GetMapping("/address/list")
    @Operation(summary = "获取用户地址列表")
    public Result<List<UserAddressVO>> addressList(@RequestHeader("Authorization") String authHeader) {
        return Result.success(userAddressService.getAddressList(extractToken(authHeader)));
    }

    @PostMapping("/address")
    @Operation(summary = "新增用户地址")
    public Result<Void> addAddress(@RequestBody @Valid AddressSaveDTO dto,
                             @RequestHeader("Authorization") String authHeader) {
        userAddressService.addAddress(extractToken(authHeader), dto);
        return Result.success();
    }

    @PutMapping("/address/{id}")
    @Operation(summary = "修改用户地址")
    public Result<Void> updateAddress(@PathVariable Long id,
                                      @RequestBody @Valid AddressSaveDTO dto,
                                      @RequestHeader("Authorization") String authHeader) {
        userAddressService.updateAddress(extractToken(authHeader), id, dto);
        return Result.success();
    }

    @DeleteMapping("/address/{id}")
    @Operation(summary = "删除用户地址")
    public Result<Void> deleteAddress(@PathVariable Long id,
                                       @RequestHeader("Authorization") String authHeader) {
        userAddressService.deleteAddress(extractToken(authHeader), id);
        return Result.success();
    }

    @PutMapping("/address/default/{id}")
    @Operation(summary = "设置默认地址")
    public Result<Void> setDefaultAddress(@PathVariable Long id,
                                           @RequestHeader("Authorization") String authHeader) {
        userAddressService.setDefaultAddress(extractToken(authHeader), id);
        return Result.success();
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException("未登录");
        }
        return authHeader.substring(7);
    }
}