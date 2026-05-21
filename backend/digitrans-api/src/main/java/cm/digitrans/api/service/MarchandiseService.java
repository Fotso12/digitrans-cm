package cm.digitrans.api.service;

import cm.digitrans.api.entity.Marchandise;
import cm.digitrans.api.entity.MouvementStock;
import cm.digitrans.api.repository.MarchandiseRepository;
import cm.digitrans.api.repository.MouvementStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

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

        // 1. Sauvegarder la marchandise en base locale
        Marchandise saved = marchandiseRepository.save(marchandise);

        // 2. Journaliser l'action initiale sur la blockchain
        String txHash = blockchainService.enregistrerMouvement(
                saved.getCodeTracabilite(),
                "N/A",
                saved.getStatut().name(),
                saved.getOrigine() != null ? saved.getOrigine() : "Plantation",
                agentTerrain,
                "Création initiale du lot de marchandise"
        );

        // Mettre à jour le hash blockchain sur l'entité principale
        saved.setHashBlockchain(txHash);
        saved = marchandiseRepository.save(saved);

        // 3. Créer un log d'historique de mouvement de stock
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

        // Règle de sécurité du Smart Contract : Rétrogradation de statut non autorisée
        if (nouveauStatut.ordinal() < ancienStatut.ordinal()) {
            throw new RuntimeException("Rétrogradation de statut non autorisée. Transition de " 
                    + ancienStatut + " vers " + nouveauStatut + " refusée.");
        }

        // 1. Mettre à jour le statut dans la base locale
        marchandise.setStatut(nouveauStatut);
        if (nouveauStatut == Marchandise.StatutMarchandise.LIVREE) {
            marchandise.setDestination(localisation);
        }

        // 2. Enregistrer le mouvement sur la blockchain
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

        // 3. Créer un log d'historique de mouvement de stock
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
}
