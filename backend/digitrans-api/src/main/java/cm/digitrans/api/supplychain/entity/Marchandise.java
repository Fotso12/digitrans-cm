package cm.digitrans.api.supplychain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Table(name = "marchandises")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Marchandise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String codeTracabilite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeMarchandise type;

    @Column(nullable = false)
    private Double quantite;

    @Column(nullable = false)
    private String unite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutMarchandise statut;

    private String origine;
    private String destination;

    @Column(length = 256)
    private String hashBlockchain;

    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;

    @PrePersist
    public void prePersist() {
        this.dateCreation = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.dateModification = LocalDateTime.now();
    }

    public enum TypeMarchandise {
        CACAO,
        CAFE,
        PRODUIT_FINI
    }

    public enum StatutMarchandise {
        EN_PLANTATION,
        EN_TRANSIT,
        EN_TRANSFORMATION,
        LIVREE
    }
}
