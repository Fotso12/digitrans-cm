package cm.digitrans.api.supplychain.service;

import cm.digitrans.api.supplychain.entity.Marchandise;
import cm.digitrans.api.supplychain.entity.MouvementStock;
import cm.digitrans.api.supplychain.repository.MarchandiseRepository;
import cm.digitrans.api.supplychain.repository.MouvementStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarchandiseService {

    private final MarchandiseRepository marchandiseRepository;
    private final MouvementStockRepository mouvementStockRepository;
    private final BlockchainService blockchainService;

    public List<Marchandise> findAll() {
        return marchandiseRepository.findAll();
    }

    public Marchandise findByCode(String codeTracabilite) {
        return marchandiseRepository.findByCodeTracabilite(codeTracabilite)
                .orElseThrow(() -> new RuntimeException("Marchandise non trouvée avec le code : " + codeTracabilite));
    }

    @Transactional
    public Marchandise save(Marchandise marchandise, String agentTerrain) {
        if (marchandiseRepository.existsByCodeTracabilite(marchandise.getCodeTracabilite())) {
            throw new RuntimeException("Le code de traçabilité '" + marchandise.getCodeTracabilite() + "' existe déjà.");
        }

        if (marchandise.getStatut() == null) {
            marchandise.setStatut(Marchandise.StatutMarchandise.EN_PLANTATION);
        }

        Marchandise saved = marchandiseRepository.save(marchandise);

        String txHash = blockchainService.enregistrerMouvement(
                saved.getCodeTracabilite(),
                "N/A",
                saved.getStatut().name(),
                saved.getOrigine() != null ? saved.getOrigine() : "Plantation",
                agentTerrain,
                "Création initiale du lot de marchandise"
        );

        saved.setHashBlockchain(txHash);
        saved = marchandiseRepository.save(saved);

        MouvementStock mouvement = MouvementStock.builder()
                .marchandise(saved)
                .typeAction(MouvementStock.TypeAction.ENTREE)
                .quantite(saved.getQuantite())
                .localisation(saved.getOrigine() != null ? saved.getOrigine() : "Plantation")
                .agentTerrain(agentTerrain)
                .txHash(txHash)
                .build();
        mouvementStockRepository.save(mouvement);

        log.info("Nouveau lot créé avec succès : Code={}, Hash={}", saved.getCodeTracabilite(), txHash);
        return saved;
    }

    @Transactional
    public Marchandise updateStatut(
            Long id,
            Marchandise.StatutMarchandise nouveauStatut,
            String localisation,
            String agentTerrain,
            String remarques
    ) {
        Marchandise marchandise = marchandiseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Marchandise non trouvée avec l'ID : " + id));

        Marchandise.StatutMarchandise ancienStatut = marchandise.getStatut();

        if (nouveauStatut.ordinal() < ancienStatut.ordinal()) {
            throw new RuntimeException("Rétrogradation de statut non autorisée. Transition de " 
                    + ancienStatut + " vers " + nouveauStatut + " refusée.");
        }

        marchandise.setStatut(nouveauStatut);
        if (nouveauStatut == Marchandise.StatutMarchandise.LIVREE) {
            marchandise.setDestination(localisation);
        }

        String txHash = blockchainService.enregistrerMouvement(
                marchandise.getCodeTracabilite(),
                ancienStatut.name(),
                nouveauStatut.name(),
                localisation,
                agentTerrain,
                remarques
        );

        marchandise.setHashBlockchain(txHash);
        Marchandise updated = marchandiseRepository.save(marchandise);

        MouvementStock.TypeAction action = MouvementStock.TypeAction.TRANSFERT;
        if (nouveauStatut == Marchandise.StatutMarchandise.LIVREE) {
            action = MouvementStock.TypeAction.SORTIE;
        }

        MouvementStock mouvement = MouvementStock.builder()
                .marchandise(updated)
                .typeAction(action)
                .quantite(updated.getQuantite())
                .localisation(localisation)
                .agentTerrain(agentTerrain)
                .txHash(txHash)
                .build();
        mouvementStockRepository.save(mouvement);

        log.info("Statut mis à jour pour le lot {} : {} -> {}, Hash={}", 
                updated.getCodeTracabilite(), ancienStatut, nouveauStatut, txHash);
        return updated;
    }

    public List<MouvementStock> getHistorique(String codeTracabilite) {
        return mouvementStockRepository.findByMarchandiseCodeTracabiliteOrderByDateHeureDesc(codeTracabilite);
    }

    /**
     * Récupérer les statistiques du module pour le service BI
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        List<Marchandise> list = marchandiseRepository.findAll();
        stats.put("totalLots", list.size());
        
        long cacaoCount = list.stream().filter(m -> m.getType() == Marchandise.TypeMarchandise.CACAO).count();
        long cafeCount = list.stream().filter(m -> m.getType() == Marchandise.TypeMarchandise.CAFE).count();
        stats.put("cacaoLotsCount", cacaoCount);
        stats.put("cafeLotsCount", cafeCount);
        
        long enPlantation = list.stream().filter(m -> m.getStatut() == Marchandise.StatutMarchandise.EN_PLANTATION).count();
        long enTransit = list.stream().filter(m -> m.getStatut() == Marchandise.StatutMarchandise.EN_TRANSIT).count();
        long enTransformation = list.stream().filter(m -> m.getStatut() == Marchandise.StatutMarchandise.EN_TRANSFORMATION).count();
        long livree = list.stream().filter(m -> m.getStatut() == Marchandise.StatutMarchandise.LIVREE).count();
        
        stats.put("statutEnPlantation", enPlantation);
        stats.put("statutEnTransit", enTransit);
        stats.put("statutEnTransformation", enTransformation);
        stats.put("statutLivree", livree);

        return stats;
    }
}
