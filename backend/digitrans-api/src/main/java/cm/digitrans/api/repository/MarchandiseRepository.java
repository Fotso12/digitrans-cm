package cm.digitrans.api.repository;

import cm.digitrans.api.entity.Marchandise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MarchandiseRepository extends JpaRepository<Marchandise, Long> {
    Optional<Marchandise> findByCodeTracabilite(String codeTracabilite);
    Boolean existsByCodeTracabilite(String codeTracabilite);
}
