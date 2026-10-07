package org.example.taskmanager;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User create(UserRequest request) {
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("Username already taken: " + username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email already registered: " + email);
        }

        Long id = userRepository.insert(username, email);
        return findById(id);
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

    public void delete(Long id) {
        int deleted = userRepository.deleteById(id);
        if (deleted == 0) {
            throw new UserNotFoundException(id);
        }
    }
}
