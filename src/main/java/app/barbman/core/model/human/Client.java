package app.barbman.core.model.human;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private int id;

    @Column(name = "displayName", nullable = false)
    private String name;

    private String document;
    private String phone;
    private String email;
    private String notes;

    @Column(nullable = false)
    private boolean active = true;
}
