package cm.digitrans.api.supplychain.controller;

import cm.digitrans.api.supplychain.entity.Marchandise;
import cm.digitrans.api.supplychain.entity.MouvementStock;
import cm.digitrans.api.supplychain.service.MarchandiseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/supply-chain/marchandises")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MarchandiseController {

    private final MarchandiseService marchandiseService;

    @GetMapping
    public ResponseEntity<List<Marchandise>> getAll() {
        return ResponseEntity.ok(marchandiseService.findAll());
    }

    @GetMapping("/tracabilite/{code}")
    public ResponseEntity<Marchandise> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(marchandiseService.findByCode(code));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT_TERRAIN')")
    public ResponseEntity<Marchandise> create(
            @RequestBody Marchandise marchandise,
            Authentication authentication
    ) {
        String agentTerrain = authentication.getName();
        return ResponseEntity.ok(marchandiseService.save(marchandise, agentTerrain));
    }

    @PutMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT_TERRAIN')")
    public ResponseEntity<Marchandise> updateStatut(
            @PathVariable Long id,
            @RequestParam Marchandise.StatutMarchandise statut,
            @RequestParam String localisation,
            @RequestParam(required = false, defaultValue = "Mise à jour de statut") String remarques,
            Authentication authentication
    ) {
        String agentTerrain = authentication.getName();
        return ResponseEntity.ok(marchandiseService.updateStatut(id, statut, localisation, agentTerrain, remarques));
    }

    @GetMapping("/tracabilite/{code}/historique")
    public ResponseEntity<List<MouvementStock>> getHistorique(@PathVariable String code) {
        return ResponseEntity.ok(marchandiseService.getHistorique(code));
    }
}
