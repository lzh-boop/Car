package com.example.car.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录响应VO
 */
@Data
@Schema(description = "登录响应数据")
public class LoginVO {

    @Schema(description = "访问令牌")
    private String token;

    @Schema(description = "令牌类型")
    private String tokenType = "Bearer";

    @Schema(description = "过期时间（毫秒）")
    private Long expiresIn;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "用户头像")
    private String avatar;

    /**
     * User role: ADMIN or USER.
     * Returned on login so the frontend can make role-based UI decisions
     * without a separate /me request.  The authoritative check is always
     * performed server-side via Spring Security.
     */
    @Schema(description = "User role: ADMIN / USER")
    private String role;
}
