package org.micap.account_management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Account {
    private String _id;
    @Indexed(unique=true)
    private UserName userName;
    private String password;
    @Indexed(unique=true)
    private String email;
    private Person person;
    private String photo;

    private List<String> roles;
}