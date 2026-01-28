package com.poly.ASM.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {

    private String username;
    private String fullname;
    private String email;
    private String photo;
    private Boolean activated;
}
