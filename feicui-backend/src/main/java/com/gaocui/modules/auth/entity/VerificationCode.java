package com.gaocui.modules.auth.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 邮箱验证码实体 (无 updated_time / deleted, 不继承 BaseEntity).
 */
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    public Integer getUsed() {
        return used;
    }

    public void setUsed(Integer used) {
        this.used = used;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
}
