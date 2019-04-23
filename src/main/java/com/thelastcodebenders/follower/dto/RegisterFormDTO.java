package com.thelastcodebenders.follower.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterFormDTO {
    private String name;
    private String surname;
    private String mail;
    private String number;
    private String password;
}
