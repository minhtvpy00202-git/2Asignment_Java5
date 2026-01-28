package com.poly.ASM.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequestDTO {

    private String username;
    private String password;
    private String fullname;
    private String email;
    private String photo;
    private Boolean activated;
}
