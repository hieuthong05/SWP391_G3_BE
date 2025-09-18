package BE.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class CustomerDTO {
    public long id;

    @NotEmpty(message = "Full name is required !")
    private String fullName;

    @Email
    @NotEmpty(message = "Email is required !")
    private String email;

    @NotEmpty(message = "Password is required !")
    private String password;

    private String gender;

    private String address;

    @Pattern(
            regexp = "^(03|05|07|08|09|012|016|018|019)[0-9]{8}$",
            message = "Phone invalid!"
    )
    private String phone;

    private boolean status;

}

