package com.exjobb.backend.repository;

import com.exjobb.backend.entity.FacebookToken;
import com.exjobb.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FacebookTokenRepository extends JpaRepository<FacebookToken, Long> {
    Optional<FacebookToken> findByUser(User user);
}
