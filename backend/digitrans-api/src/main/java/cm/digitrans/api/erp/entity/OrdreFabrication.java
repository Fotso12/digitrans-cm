package cm.digitrans.api.erp.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "erp_ordres_fabrication")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdreFabrication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String produit; // ex: "CACAO_POUDRE", "CAFE_TORREFIE"

    @Column(nullable = false)
    private Double quantite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutTransformation statutTransformation;

    public enum StatutTransformation {
        A_COMMENCER,
        EN_COURS,
        TERMINE
    }
}
