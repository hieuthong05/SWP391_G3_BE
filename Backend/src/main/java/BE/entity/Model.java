package BE.entity;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "model_ID")
    private Long modelID;

    @Column(name="model_name", nullable = false)
    private String modelName;
}
