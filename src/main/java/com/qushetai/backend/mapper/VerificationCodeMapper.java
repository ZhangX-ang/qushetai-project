package com.qushetai.backend.mapper;

import com.qushetai.backend.entity.VerificationCode;
import org.apache.ibatis.annotations.*;

@Mapper
public interface VerificationCodeMapper {

    @Insert("INSERT INTO verification_code (contact, code, type, expired_at) " +
            "VALUES (#{contact}, #{code}, #{type}, #{expiredAt})")
    int insert(VerificationCode verificationCode);

    // 查找有效的验证码 - 统一使用 type 列
    @Select("SELECT * FROM verification_code WHERE contact = #{contact} AND code = #{code} AND type = #{type} AND expired_at > NOW() AND is_used = 0 ORDER BY created_at DESC LIMIT 1")
    VerificationCode findValidCode(@Param("contact") String contact, @Param("code") String code, @Param("type") Integer type);

    // 标记验证码为已使用
    @Update("UPDATE verification_code SET is_used = 1 WHERE id = #{id}")
    int markAsUsed(Long id);
}