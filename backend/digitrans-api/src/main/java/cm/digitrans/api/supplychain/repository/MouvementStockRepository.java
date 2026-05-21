package cm.digitrans.api.supplychain.repository;

import cm.digitrans.api.supplychain.entity.MouvementStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MouvementStockRepository extends JpaRepository<MouvementStock, Long> {
    List<MouvementStock> findByMarchandiseIdOrderByDateHeureDesc(Long marchandiseId);
    List<MouvementStock> findByMarchandiseCodeTracabiliteOrderByDateHeureDesc(String codeTracabilite);
}
