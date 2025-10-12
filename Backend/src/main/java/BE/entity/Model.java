package BE.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "model_ID")
    private Long modelID;

    @Column(name="model_name", nullable = false)
    private String modelName;

    @OneToMany(mappedBy = "model", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Vehicle> vehicles = new ArrayList<>();
}
