package com.thelastcodebenders.follower.assembler;

import com.thelastcodebenders.follower.dto.RegisterFormDTO;
import com.thelastcodebenders.follower.model.Role;
import com.thelastcodebenders.follower.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserAssembler {
    private static final Logger log = LoggerFactory.getLogger(UserAssembler.class);

    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserAssembler(BCryptPasswordEncoder bCryptPasswordEncoder){
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public User convertFormDtoToUser(RegisterFormDTO registerFormDTO, Role role){
        return User.builder()
                .balance(0)
                .mail(registerFormDTO.getMail())
                .name(registerFormDTO.getName())
                .number(registerFormDTO.getNumber())
                .password(bCryptPasswordEncoder.encode(registerFormDTO.getPassword()))
                .state(true)
                .role(role)
                .surname(registerFormDTO.getSurname())
                .build();
    }
}
