package io.renren.modules.app.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录表单
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
@Schema(description = "登录表单")
public class LoginForm {

    @Schema(description = "手机号", required = true)
    @NotBlank(message = "手机号不能为空")
    private String mobile;

    @Schema(description = "密码", required = true)
    @NotBlank(message = "密码不能为空")
    private String password;
}
