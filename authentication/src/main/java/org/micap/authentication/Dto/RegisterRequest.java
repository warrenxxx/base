package org.micap.authentication.Dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


import java.util.Date;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)

public class RegisterRequest {
    private String userName;
    private String password;
    private String email;
    private String photo;
    private String name;
    private String lastname;
    private Date bithDate;
}
