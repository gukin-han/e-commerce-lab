package com.loopers.domain.user;

import com.loopers.common.error.CoreException;
import com.loopers.common.error.ErrorType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User signUp(User user) {
        // 유저 정보 검증
        userRepository.findByLoginId(user.getLoginId())
                .ifPresent(existingUser -> {
                    throw new CoreException(ErrorType.BAD_REQUEST, "이미 가입된 ID 입니다.");
                });

        // 유저 저장
        return userRepository.save(user);
    }

    public User getMe(String loginId) {
        return userRepository.findByLoginId(loginId).orElse(null);
    }

    @Transactional
    public User findByUserId(Long userId) {
        return userRepository.findByUserId(userId).orElseThrow(EntityNotFoundException::new);
    }

    @Transactional
    public User getByLoginId(String loginId) {
        return userRepository.findByLoginId(loginId).orElseThrow(EntityNotFoundException::new);
    }
}
