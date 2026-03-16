package app.barbman.core.model.sales.services;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "service_header")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class ServiceHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private int id;

    @Column(name = "user_id", nullable = false)
    private int userId;

    @Column(name = "sale_id", nullable = false)
    private int saleId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private double subtotal;
}
