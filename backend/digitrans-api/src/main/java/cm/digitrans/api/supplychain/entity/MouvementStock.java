package cm.digitrans.api.supplychain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Table(name = "mouvements_stock")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MouvementStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "marchandise_id", nullable = false)
    private Marchandise marchandise;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeAction typeAction;

    @Column(nullable = false)
    private Double quantite;

    @Column(nullable = false)
    private String localisation;

    private LocalDateTime dateHeure;

    @Column(nullable = false)
    private String agentTerrain;

    @Column(length = 256)
    private String txHash;

    @PrePersist
    public void prePersist() {
        this.dateHeure = LocalDateTime.now();
    }

    public enum TypeAction {
        ENTREE,
        SORTIE,
        TRANSFERT
    }
}
