package com.exjobb.backend.repository;

import com.exjobb.backend.entity.TwitterToken;
import com.exjobb.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface TwitterTokenRepository extends JpaRepository<TwitterToken, Long> {
    Optional<TwitterToken> findByUser(User user);
    List<TwitterToken> findAllByUser(User user);
    Optional<TwitterToken> findById(Long id);
}
