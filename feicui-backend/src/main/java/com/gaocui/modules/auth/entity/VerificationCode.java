package com.gaocui.modules.auth.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 邮箱验证码实体 (无 updated_time / deleted, 不继承 BaseEntity).
 */
@Getter
@Setter
@TableName("t_verification_code")
public class VerificationCode {

    public static final String SCENE_LOGIN = "LOGIN";
    public static final String SCENE_REGISTER = "REGISTER";
    public static final String SCENE_CHANGE_EMAIL = "CHANGE_EMAIL";

    @TableId(type = IdType.AUTO)
    private Long id;
    private String email;
    private String code;
    private String scene;
    private LocalDateTime expireTime;
    /** 是否已使用: 0 否 1 是 */
    private Integer used;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}
