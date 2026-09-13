package com.fawry.routing.service;

import com.fawry.routing.config.security.utils.MyUserDetails;
import com.fawry.routing.dto.request.SignupRequest;
import com.fawry.routing.entity.User;
import com.fawry.routing.exception.EmailAlreadyExistsException;
import com.fawry.routing.exception.ResourceNotFoundException;
import com.fawry.routing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.fawry.routing.enums.Role.USER;


@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var theUser = userRepository.findUserByEmail(email);
        if (theUser == null) {
            throw new UsernameNotFoundException("Invalid email or password.");
        }
        return new MyUserDetails(theUser);
    }

    @Transactional
    public User register(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("An account already exists for " + request.getEmail());
        }
        var user = new User();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(USER);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No biller found with id " + id));
    }
}
