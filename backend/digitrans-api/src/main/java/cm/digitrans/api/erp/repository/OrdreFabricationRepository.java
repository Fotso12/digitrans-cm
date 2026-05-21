package cm.digitrans.api.erp.repository;

import cm.digitrans.api.erp.entity.OrdreFabrication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdreFabricationRepository extends JpaRepository<OrdreFabrication, Long> {
}
