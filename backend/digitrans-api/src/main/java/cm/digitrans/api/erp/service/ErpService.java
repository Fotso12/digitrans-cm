package cm.digitrans.api.erp.service;

import cm.digitrans.api.erp.entity.OrdreFabrication;
import cm.digitrans.api.erp.repository.OrdreFabricationRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ErpService {

    private final OrdreFabricationRepository orderRepository;

    @PostConstruct
    public void seedOrders() {
        if (orderRepository.count() == 0) {
            log.info("ERP vide. Insertion d'ordres de fabrication de test...");
            orderRepository.save(OrdreFabrication.builder().code("OF-2026-001").produit("CACAO_POUDRE").quantite(500.0).statutTransformation(OrdreFabrication.StatutTransformation.TERMINE).build());
            orderRepository.save(OrdreFabrication.builder().code("OF-2026-002").produit("CAFE_TORREFIE").quantite(250.0).statutTransformation(OrdreFabrication.StatutTransformation.EN_COURS).build());
            orderRepository.save(OrdreFabrication.builder().code("OF-2026-003").produit("CHOCOLAT_NOIR_80").quantite(120.0).statutTransformation(OrdreFabrication.StatutTransformation.A_COMMENCER).build());
            log.info("Ordres de fabrication de test insérés avec succès !");
        }
    }

    public List<OrdreFabrication> findAll() {
        return orderRepository.findAll();
    }

    public OrdreFabrication save(OrdreFabrication order) {
        if (order.getStatutTransformation() == null) {
            order.setStatutTransformation(OrdreFabrication.StatutTransformation.A_COMMENCER);
        }
        return orderRepository.save(order);
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        List<OrdreFabrication> orders = orderRepository.findAll();
        stats.put("totalOrders", orders.size());

        long pending = orders.stream()
                .filter(o -> o.getStatutTransformation() != OrdreFabrication.StatutTransformation.TERMINE)
                .count();
        long completed = orders.stream()
                .filter(o -> o.getStatutTransformation() == OrdreFabrication.StatutTransformation.TERMINE)
                .count();

        stats.put("pendingOrders", pending);
        stats.put("completedOrders", completed);

        return stats;
    }
}
