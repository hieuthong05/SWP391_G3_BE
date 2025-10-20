package BE.model;

import lombok.Data;

@Data
public class EmailDetail {

    String recipient;
    String subject;
    String fullName;
}
