package com.loopers.domain.user;

import com.loopers.domain.BaseEntity;
import com.loopers.common.constant.Gender;
import com.loopers.common.error.CoreException;
import com.loopers.common.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
@Entity
public class User extends BaseEntity {

    private String loginId;
    private String email;
    private String dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Builder
    private User(String loginId, String email, String dateOfBirth, Gender gender) {
        if (loginId == null || !loginId.matches("^[a-zA-Z0-9]{1,10}$")) {
            throw new CoreException(ErrorType.BAD_REQUEST, "아이디는 영문 및 숫자 10자 이내로 입력해야 합니다.");
        }

        if (email == null || !email.matches("^[\\w.%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이메일은 `xx@yy.zz` 형식으로 입력해야 합니다.");
        }

        if (dateOfBirth == null || !dateOfBirth.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            throw new CoreException(ErrorType.BAD_REQUEST, "생년월일은 `yyyy-MM-dd` 형식으로 입력해야 합니다.");
        }

        if (gender == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "성별은 필수 입력 항목입니다.");
        }

        this.loginId = loginId;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
    }

    public static User create(String loginId, String email, String dateOfBirth, Gender gender) {
        return User.builder()
                .loginId(loginId)
                .email(email)
                .dateOfBirth(dateOfBirth)
                .gender(Gender.MALE)
                .build();
    }
}
