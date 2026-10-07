package org.example.taskmanager;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequest request) {
        return createUser(request.username(), request.email(), request.password(), Role.USER);
    }

    public User create(CreateUserRequest request) {
        return createUser(request.username(), request.email(), request.password(), request.role());
    }

    public User findById(Long id) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new UserNotFoundException(id);
        }
        return user;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public void delete(Long id, AppUserDetails currentUser) {
        if (id.equals(currentUser.getId())) {
            throw new OperationNotAllowedException("You cannot delete your own account");
        }
        int deleted = userRepository.deleteById(id);
        if (deleted == 0) {
            throw new UserNotFoundException(id);
        }
    }

    private User createUser(String rawUsername, String rawEmail, String password, Role role) {
        String username = rawUsername.trim();
        String email = rawEmail.trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("Username already taken: " + username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email already registered: " + email);
        }

        String passwordHash = passwordEncoder.encode(password);
        Long id = userRepository.insert(username, email, passwordHash, role);
        return findById(id);
    }
}
