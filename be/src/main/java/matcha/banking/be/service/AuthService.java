package matcha.banking.be.service;

import lombok.RequiredArgsConstructor;
import matcha.banking.be.config.SecurityConfigurer;
import matcha.banking.be.dao.UserDao;
import matcha.banking.be.dto.LoginReponseBodyDto;
import matcha.banking.be.dto.RegisterDto;
import matcha.banking.be.entity.UserEntity;
import matcha.banking.be.mapper.UserMapper;
import matcha.banking.be.util.JwtUtil;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserDao userDao;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    public UserEntity createUser(RegisterDto registerDto) {

        String error = inputCheck(registerDto);

        // check if there is any error
        if (!error.isEmpty()) {
            throw new IllegalArgumentException(error);
        }

        if (userDao.findByEmail(registerDto.getEmail()).isPresent()) {
            throw new DuplicateKeyException("Email already exists");
        }

        String cardNumber;
        do {
            cardNumber = generateRandomCardNumber();
        } while (userDao.findByCardNumber(cardNumber).isPresent());

        UserEntity userEntity = new UserEntity();
        userEntity.setName(registerDto.getName());
        userEntity.setEmail(registerDto.getEmail());
        userEntity.setCardNumber(cardNumber);
        userEntity.setBalance(0.0);
        userEntity.setBank("Matcha Bank");
        userEntity.setPassword(registerDto.getPassword());
        userEntity.setCreated(LocalDateTime.now());
        userEntity.setUpdated(LocalDateTime.now());
        return userDao.save(userEntity);
    }

    public LoginReponseBodyDto login(String email, String password) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        } else if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        UserEntity userEntity = userDao.findByEmail(email).orElseThrow(
                () -> new EmptyResultDataAccessException("User not found", 1)
        );

        if (userEntity.getPassword().equals(password)) {
            LoginReponseBodyDto loginReponseBodyDto = new LoginReponseBodyDto();
            loginReponseBodyDto.setToken(jwtUtil.generateToken(userEntity));
            loginReponseBodyDto.setUser(userMapper.entityToDto(userEntity));
            return loginReponseBodyDto;
        } else {
            throw new IllegalArgumentException("Invalid password");
        }
    }

    private String generateRandomCardNumber() {
        Random random = new Random();
        StringBuilder cardNumber = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            int digit = random.nextInt(10); // generates a random digit from 0 to 9
            cardNumber.append(digit);
        }
        return cardNumber.toString();
    }

    private String inputCheck(RegisterDto registerDto) {
        StringBuilder error = new StringBuilder();
        if (registerDto.getName() == null || registerDto.getName().isEmpty()) {
            error.append("Name is required");
        } else if (registerDto.getPassword() == null || registerDto.getPassword().isEmpty()) {
            error.append("Password is required");
        } else if (registerDto.getEmail() == null || registerDto.getEmail().isEmpty()) {
            error.append("Email is required");
        }
        return error.toString();
    }
}
