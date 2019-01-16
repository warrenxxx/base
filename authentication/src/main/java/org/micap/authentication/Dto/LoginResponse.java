package org.micap.authentication.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class LoginResponse {
    private String token;
    private String _id;
    private String email;
    private Person person;
    private String photo;

    public LoginResponse(Account account) {
        this._id=account.get_id();
        this.email=account.getEmail();
        this.person=account.getPerson();
        this.photo=account.getPhoto();
    }
}
