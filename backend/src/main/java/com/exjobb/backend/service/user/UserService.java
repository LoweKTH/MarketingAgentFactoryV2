package com.exjobb.backend.service.user;

import com.exjobb.backend.entity.User;
import com.exjobb.backend.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void createDefaultUser() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User adminUser = new User("admin", passwordEncoder.encode("password"), "ROLE_ADMIN,ROLE_USER");
            userRepository.save(adminUser);
            System.out.println("Default admin user created!");
        }

        if (userRepository.findByUsername("user").isEmpty()) {
            User normalUser = new User("user", passwordEncoder.encode("password"), "ROLE_USER");
            userRepository.save(normalUser);
            System.out.println("Default normal user created!");
        }
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<SimpleGrantedAuthority> authorities = Arrays.stream(user.getRoles().split(","))
                .map(role -> new SimpleGrantedAuthority(role.trim()))
                .collect(Collectors.toList());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .build();
    }

    public boolean authenticateUser(String username, String password) {
        Optional<User> userOpt = findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return passwordEncoder.matches(password, user.getPassword());
        }
        return false;
    }

}